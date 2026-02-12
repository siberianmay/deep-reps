package com.deepreps.core.domain.usecase

import com.deepreps.core.domain.model.GroupVolume
import com.deepreps.core.domain.model.WorkoutSet
import com.deepreps.core.domain.model.WorkoutSummary
import com.deepreps.core.domain.model.enums.MuscleGroup
import com.deepreps.core.domain.repository.ExerciseRepository
import com.deepreps.core.domain.repository.WorkoutSessionRepository
import com.deepreps.core.domain.util.VolumeCalculator
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Calculates a summary for a completed workout session.
 *
 * Computes:
 * - Duration (from session entity)
 * - Exercise count
 * - Total working sets (warmup excluded)
 * - Total tonnage (weight * reps for working sets only)
 * - Per-group volume breakdown
 *
 * PRs are calculated separately by [CalculatePersonalRecordsUseCase] and merged at the
 * presentation layer.
 */
class GetWorkoutSummaryUseCase @Inject constructor(
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val exerciseRepository: ExerciseRepository,
) {

    /**
     * @param sessionId The completed session to summarize.
     * @return [WorkoutSummary] with volume stats, or null if session not found.
     */
    @Suppress("LongMethod")
    suspend operator fun invoke(sessionId: Long): WorkoutSummary? {
        val session = workoutSessionRepository.getSession(sessionId) ?: return null

        val workoutExercises = workoutSessionRepository
            .getExercisesForSession(sessionId)
            .first()

        if (workoutExercises.isEmpty()) {
            return WorkoutSummary(
                sessionId = sessionId,
                durationSeconds = session.durationSeconds ?: 0L,
                exerciseCount = 0,
                totalWorkingSets = 0,
                totalTonnageKg = 0.0,
                perGroupVolume = emptyList(),
                personalRecords = emptyList(),
            )
        }

        // Build map: exerciseId -> list of sets
        val exerciseSets = mutableMapOf<Long, List<WorkoutSet>>()
        for (we in workoutExercises) {
            val sets = workoutSessionRepository.getSetsForExercise(we.id).first()
            exerciseSets[we.exerciseId] = sets
        }

        val totalWorkingSets = VolumeCalculator.workingSetsForSession(exerciseSets)
        val totalTonnage = VolumeCalculator.sessionTonnage(exerciseSets)

        // Per-group volume
        val exerciseIds = workoutExercises.map { it.exerciseId }
        val exerciseDetails = exerciseRepository.getExercisesByIds(exerciseIds)
        val exerciseToGroup = exerciseDetails.associate { it.id to it.primaryGroupId }

        val groupedSets = mutableMapOf<Long, MutableMap<Long, List<WorkoutSet>>>()
        for ((exerciseId, sets) in exerciseSets) {
            val groupId = exerciseToGroup[exerciseId] ?: continue
            val groupMap = groupedSets.getOrPut(groupId) { mutableMapOf() }
            groupMap[exerciseId] = sets
        }

        val perGroupVolume = groupedSets.mapNotNull { (groupId, setsMap) ->
            val groupName = muscleGroupNameFromId(groupId) ?: return@mapNotNull null
            GroupVolume(
                groupName = groupName,
                workingSets = VolumeCalculator.workingSetsForGroup(setsMap),
                tonnageKg = VolumeCalculator.sessionTonnage(setsMap),
            )
        }.sortedBy { it.groupName }

        return WorkoutSummary(
            sessionId = sessionId,
            durationSeconds = session.durationSeconds ?: 0L,
            exerciseCount = workoutExercises.size,
            totalWorkingSets = totalWorkingSets,
            totalTonnageKg = totalTonnage,
            perGroupVolume = perGroupVolume,
            personalRecords = emptyList(), // Filled by presentation layer
        )
    }

    companion object {

        internal fun muscleGroupNameFromId(groupId: Long): String? {
            val index = (groupId - 1).toInt()
            return MuscleGroup.entries.getOrNull(index)?.let { group ->
                when (group) {
                    MuscleGroup.LEGS -> "Legs"
                    MuscleGroup.LOWER_BACK -> "Lower Back"
                    MuscleGroup.CHEST -> "Chest"
                    MuscleGroup.BACK -> "Back"
                    MuscleGroup.SHOULDERS -> "Shoulders"
                    MuscleGroup.ARMS -> "Arms"
                    MuscleGroup.CORE -> "Core"
                }
            }
        }
    }
}
