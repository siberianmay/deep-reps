package com.deepreps.core.domain.usecase

import com.deepreps.core.domain.model.DetectedPr
import com.deepreps.core.domain.model.PersonalRecord
import com.deepreps.core.domain.model.enums.RecordType
import com.deepreps.core.domain.model.enums.SetStatus
import com.deepreps.core.domain.model.enums.SetType
import com.deepreps.core.domain.repository.ExerciseRepository
import com.deepreps.core.domain.repository.PersonalRecordRepository
import com.deepreps.core.domain.repository.WorkoutSessionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Detects and persists personal records from a completed workout session.
 *
 * For each exercise in the session, compares completed working sets against
 * historical best records for:
 * - [RecordType.MAX_WEIGHT]: Heaviest weight lifted in any single set.
 *
 * Per exercise-science.md Section 7.4.2, only weight PRs are detected at MVP.
 * Estimated 1RM PRs require >= HIGH confidence (1-5 reps) and are Phase 2.
 *
 * New records are inserted into [PersonalRecordRepository] and returned for UI display.
 */
class CalculatePersonalRecordsUseCase @Inject constructor(
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val exerciseRepository: ExerciseRepository,
    private val personalRecordRepository: PersonalRecordRepository,
) {

    /**
     * Detects and persists PRs for the given completed session.
     *
     * @param sessionId The completed session to scan for PRs.
     * @return List of detected PRs, empty if none.
     */
    @Suppress("LongMethod", "CyclomaticComplexMethod", "LoopWithTooManyJumpStatements")
    suspend operator fun invoke(sessionId: Long): List<DetectedPr> {
        val workoutExercises = workoutSessionRepository
            .getExercisesForSession(sessionId)
            .first()

        if (workoutExercises.isEmpty()) return emptyList()

        val detectedPrs = mutableListOf<DetectedPr>()
        val recordsToInsert = mutableListOf<PersonalRecord>()
        val now = System.currentTimeMillis()

        for (we in workoutExercises) {
            val sets = workoutSessionRepository.getSetsForExercise(we.id).first()

            // Only completed working sets
            val completedWorkingSets = sets.filter {
                it.type == SetType.WORKING &&
                    it.status == SetStatus.COMPLETED &&
                    it.actualWeightKg != null &&
                    it.actualReps != null
            }

            if (completedWorkingSets.isEmpty()) continue

            // Find the heaviest weight in this session for this exercise
            val bestSet = completedWorkingSets.maxByOrNull { it.actualWeightKg!! } ?: continue
            val bestWeight = bestSet.actualWeightKg ?: continue
            val bestReps = bestSet.actualReps ?: continue

            // Compare against historical best
            val existingBest = personalRecordRepository.getBestByType(
                exerciseId = we.exerciseId,
                recordType = RecordType.MAX_WEIGHT.value,
            )

            val isNewRecord = existingBest == null || bestWeight > (existingBest.weightValue ?: 0.0)

            if (isNewRecord) {
                val exerciseDetail = exerciseRepository.getExerciseById(we.exerciseId)
                val exerciseName = exerciseDetail?.name ?: "Unknown Exercise"

                detectedPrs.add(
                    DetectedPr(
                        exerciseId = we.exerciseId,
                        exerciseName = exerciseName,
                        weightKg = bestWeight,
                        reps = bestReps,
                        recordType = RecordType.MAX_WEIGHT,
                    ),
                )

                recordsToInsert.add(
                    PersonalRecord(
                        id = 0,
                        exerciseId = we.exerciseId,
                        recordType = RecordType.MAX_WEIGHT,
                        weightValue = bestWeight,
                        reps = bestReps,
                        estimated1rm = null, // Phase 2
                        achievedAt = now,
                        sessionId = sessionId,
                    ),
                )
            }
        }

        if (recordsToInsert.isNotEmpty()) {
            personalRecordRepository.insertAll(recordsToInsert)
        }

        return detectedPrs
    }
}
