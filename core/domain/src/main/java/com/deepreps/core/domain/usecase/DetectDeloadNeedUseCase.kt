package com.deepreps.core.domain.usecase

import com.deepreps.core.domain.model.DeloadStatus
import com.deepreps.core.domain.model.ExerciseHistory
import com.deepreps.core.domain.util.Estimated1rmCalculator
import javax.inject.Inject

/**
 * Checks deload triggers per exercise-science.md Section 3.4.
 *
 * Triggers (unified across experience levels):
 * - Scheduled (proactive): beginner 6wk, intermediate 4wk, advanced 4-5wk
 * - Performance-triggered (reactive): 2+ consecutive sessions with regression
 * - User-requested: any time
 *
 * Regression definition (intermediate/advanced): estimated 1RM decreases across
 * consecutive sessions (Epley formula, Section 7.1).
 *
 * Stall definition (beginners): same working weight for 3 consecutive sessions
 * without completing all target reps. Two stalls within a 6-week period trigger deload.
 */
class DetectDeloadNeedUseCase @Inject constructor() {

    /**
     * @param experienceLevel 1=beginner, 2=intermediate, 3=advanced
     * @param weeksSinceLastDeload null if never deloaded
     * @param exerciseHistories training history for exercises being programmed
     * @param userRequestedDeload true if user manually asked for deload
     */
    @Suppress("ReturnCount")
    fun execute(
        experienceLevel: Int,
        weeksSinceLastDeload: Int?,
        exerciseHistories: List<ExerciseHistory>,
        userRequestedDeload: Boolean = false,
    ): DeloadStatus {
        if (userRequestedDeload) return DeloadStatus.USER_REQUESTED

        // Check scheduled deload
        if (weeksSinceLastDeload != null) {
            val threshold = getScheduledDeloadWeeks(experienceLevel)
            if (weeksSinceLastDeload >= threshold) {
                return DeloadStatus.PROACTIVE_RECOMMENDED
            }
        }

        // Check performance regression
        if (hasRegressionPattern(exerciseHistories, experienceLevel)) {
            return DeloadStatus.REACTIVE_RECOMMENDED
        }

        return DeloadStatus.NOT_NEEDED
    }

    private fun getScheduledDeloadWeeks(experienceLevel: Int): Int = when (experienceLevel) {
        1 -> 6
        2 -> 4
        3 -> 5
        else -> 6
    }

    /**
     * Checks for regression patterns in exercise history.
     *
     * Beginner: 2+ stalls on same exercise within 6-week window.
     * A stall = same weight for 3 consecutive sessions without completing all target reps.
     *
     * Intermediate/Advanced: 2+ consecutive sessions where estimated 1RM decreases.
     * For advanced, requires regression on 2+ exercises simultaneously.
     */
    private fun hasRegressionPattern(
        histories: List<ExerciseHistory>,
        experienceLevel: Int,
    ): Boolean {
        if (histories.isEmpty()) return false

        return when (experienceLevel) {
            1 -> histories.any { hasBeginnerStalls(it) }
            2 -> histories.any { hasConsecutiveRegression(it) }
            3 -> {
                val regressingExercises = histories.count { hasConsecutiveRegression(it) }
                regressingExercises >= 2
            }
            else -> false
        }
    }

    /**
     * Beginner stall: 2+ stalls on same exercise within history window.
     * A stall = same working weight for 3+ consecutive sessions.
     */
    private fun hasBeginnerStalls(history: ExerciseHistory): Boolean {
        val sessions = history.sessions
        if (sessions.size < 3) return false

        var stallCount = 0
        var consecutiveSameWeight = 1
        var lastWeight: Double? = null

        for (session in sessions) {
            val workingSets = session.sets.filter { it.setType == "working" }
            if (workingSets.isEmpty()) continue

            val maxWeight = workingSets.maxOf { it.weight }

            if (lastWeight != null && maxWeight == lastWeight) {
                consecutiveSameWeight++
                if (consecutiveSameWeight >= 3) {
                    stallCount++
                    consecutiveSameWeight = 1 // Reset after counting stall
                }
            } else {
                consecutiveSameWeight = 1
            }

            lastWeight = maxWeight
        }

        return stallCount >= 2
    }

    /**
     * Intermediate/Advanced regression: 2+ consecutive sessions with decreasing
     * estimated 1RM (Epley formula).
     */
    @Suppress("ReturnCount")
    private fun hasConsecutiveRegression(history: ExerciseHistory): Boolean {
        val sessions = history.sessions
        if (sessions.size < 3) return false

        val estimated1rms = sessions.mapNotNull { session ->
            val workingSets = session.sets.filter { it.setType == "working" }
            if (workingSets.isEmpty()) return@mapNotNull null

            // Use the best set to calculate estimated 1RM
            workingSets.mapNotNull { set ->
                if (set.weight > 0 && set.reps > 0) {
                    Estimated1rmCalculator.epley(set.weight, set.reps)
                } else {
                    null
                }
            }.maxOrNull()
        }

        if (estimated1rms.size < 3) return false

        // Check for 2+ consecutive decreases
        var consecutiveDecreases = 0
        for (i in 1 until estimated1rms.size) {
            if (estimated1rms[i] < estimated1rms[i - 1]) {
                consecutiveDecreases++
                if (consecutiveDecreases >= 2) return true
            } else {
                consecutiveDecreases = 0
            }
        }

        return false
    }
}
