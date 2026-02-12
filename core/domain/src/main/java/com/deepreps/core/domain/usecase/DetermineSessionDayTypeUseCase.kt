package com.deepreps.core.domain.usecase

import com.deepreps.core.domain.model.ExerciseHistory
import com.deepreps.core.domain.model.SessionDayType
import javax.inject.Inject

/**
 * Determines the DUP day type for intermediate+ users.
 *
 * Per exercise-science.md Section 3.2 and architecture.md Section 4.9:
 *
 * - Beginners use linear progression; day type is always HYPERTROPHY (high reps, low intensity).
 * - Intermediates rotate: HYPERTROPHY -> STRENGTH -> POWER -> HYPERTROPHY.
 *   The use case examines the most recent session's rep range to infer what day type was last used.
 * - Advanced users: block periodization. The day type within a block is determined by the
 *   current block phase (accumulation = hypertrophy, intensification = strength, realization = power).
 *
 * Returns [PeriodizationResult] containing both the periodization model label
 * (for PlanRequest.periodizationModel) and the day type / block phase info.
 */
class DetermineSessionDayTypeUseCase @Inject constructor() {

    /**
     * @param experienceLevel 1=beginner, 2=intermediate, 3=advanced
     * @param exerciseHistories recent training history for the selected muscle groups
     */
    fun execute(
        experienceLevel: Int,
        exerciseHistories: List<ExerciseHistory>,
    ): PeriodizationResult = when (experienceLevel) {
        1 -> PeriodizationResult(
            periodizationModel = "linear",
            dayType = SessionDayType.HYPERTROPHY,
            blockPhase = null,
            blockWeek = null,
        )
        2 -> determineDupDayType(exerciseHistories)
        3 -> determineBlockPhase(exerciseHistories)
        else -> PeriodizationResult(
            periodizationModel = "linear",
            dayType = SessionDayType.HYPERTROPHY,
            blockPhase = null,
            blockWeek = null,
        )
    }

    /**
     * DUP cycling for intermediates.
     *
     * Examines the most recent session's average working set reps to infer the last day type:
     * - avg reps >= 8 -> last was HYPERTROPHY -> next is STRENGTH
     * - avg reps <= 5 -> last was STRENGTH -> next is POWER
     * - avg reps 6-7 -> last was POWER -> next is HYPERTROPHY
     *
     * If no history, default to HYPERTROPHY.
     */
    private fun determineDupDayType(histories: List<ExerciseHistory>): PeriodizationResult {
        val lastDayType = inferLastDayType(histories)

        val nextDayType = when (lastDayType) {
            SessionDayType.HYPERTROPHY -> SessionDayType.STRENGTH
            SessionDayType.STRENGTH -> SessionDayType.POWER
            SessionDayType.POWER -> SessionDayType.HYPERTROPHY
            null -> SessionDayType.HYPERTROPHY
        }

        return PeriodizationResult(
            periodizationModel = "dup",
            dayType = nextDayType,
            blockPhase = null,
            blockWeek = null,
        )
    }

    /**
     * Block periodization for advanced users.
     *
     * Per architecture.md Section 4.9: phase is inferred heuristically from training history.
     * Examines the last 4-6 weeks of data to infer current phase based on average rep range
     * and volume trends. If insufficient history (< 8 sessions), defaults to Accumulation.
     */
    private fun determineBlockPhase(histories: List<ExerciseHistory>): PeriodizationResult {
        val allSessions = histories.flatMap { it.sessions }.sortedBy { it.date }

        if (allSessions.size < 8) {
            return PeriodizationResult(
                periodizationModel = "block",
                dayType = SessionDayType.HYPERTROPHY,
                blockPhase = "accumulation",
                blockWeek = 1,
            )
        }

        // Analyze the last ~4 weeks of sessions (roughly 12-16 sessions)
        val recentSessions = allSessions.takeLast(16)
        val avgReps = recentSessions.flatMap { it.sets }
            .filter { it.setType == "working" && it.reps > 0 }
            .map { it.reps }
            .average()

        val avgWorkingSetsPerSession = recentSessions.map { session ->
            session.sets.count { it.setType == "working" }
        }.average()

        // Infer phase from rep range and volume patterns
        val (phase, dayType) = when {
            avgReps >= 8 && avgWorkingSetsPerSession >= 16 -> "accumulation" to SessionDayType.HYPERTROPHY
            avgReps in 4.0..7.0 && avgWorkingSetsPerSession in 12.0..16.0 -> "intensification" to SessionDayType.STRENGTH
            avgReps < 4 && avgWorkingSetsPerSession < 12 -> "realization" to SessionDayType.POWER
            else -> "accumulation" to SessionDayType.HYPERTROPHY
        }

        // Estimate week within block based on number of sessions at this phase pattern
        val blockWeek = estimateBlockWeek(recentSessions, avgReps)

        return PeriodizationResult(
            periodizationModel = "block",
            dayType = dayType,
            blockPhase = phase,
            blockWeek = blockWeek,
        )
    }

    private fun inferLastDayType(histories: List<ExerciseHistory>): SessionDayType? {
        val lastSession = histories
            .flatMap { it.sessions }
            .maxByOrNull { it.date }
            ?: return null

        val workingSets = lastSession.sets.filter { it.setType == "working" && it.reps > 0 }
        if (workingSets.isEmpty()) return null

        val avgReps = workingSets.map { it.reps }.average()

        return when {
            avgReps >= 8 -> SessionDayType.HYPERTROPHY
            avgReps <= 5 -> SessionDayType.STRENGTH
            else -> SessionDayType.POWER
        }
    }

    private fun estimateBlockWeek(
        recentSessions: List<com.deepreps.core.domain.model.HistoricalSession>,
        avgReps: Double,
    ): Int {
        // Simple heuristic: count how many recent sessions share similar rep patterns
        val threshold = 0.20 // 20% variation in avg reps
        var count = 0
        for (session in recentSessions.reversed()) {
            val sessionAvg = session.sets
                .filter { it.setType == "working" && it.reps > 0 }
                .map { it.reps }
                .average()
            if (kotlin.math.abs(sessionAvg - avgReps) / avgReps <= threshold) {
                count++
            } else {
                break
            }
        }
        // Rough estimate: 3-4 sessions per week
        return (count / 3).coerceIn(1, 4)
    }
}

/**
 * Result of periodization determination.
 */
data class PeriodizationResult(
    val periodizationModel: String,
    val dayType: SessionDayType,
    val blockPhase: String?,
    val blockWeek: Int?,
)
