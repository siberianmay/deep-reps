@file:Suppress("ForbiddenPublicDataClass")

package com.deepreps.core.domain.calculator

import com.deepreps.core.domain.model.HistoricalSession
import com.deepreps.core.domain.model.HistoricalSet
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Result of a progression calculation for a single exercise.
 *
 * [weightKg] is the recommended working weight for the next session.
 * [targetReps] is the target rep count for working sets.
 * [isStalled] indicates the user has not increased weight for 3+ consecutive sessions.
 * [stallNote] provides a human-readable suggestion when stalled.
 */
data class ProgressionResult(
    val weightKg: Double,
    val targetReps: Int,
    val isStalled: Boolean,
    val stallNote: String? = null,
)

/**
 * Pure stateless calculator for progressive overload decisions.
 *
 * Uses the most recent session for weight/rep progression and the last 3 sessions
 * for stall detection. All weights are in kg and rounded to nearest 1.25 kg.
 *
 * Decision logic (from exercise-science.md progression model):
 * - Case 1: Worst set >= rangeMax -> increase weight, reset reps to rangeMin
 * - Case 2: Avg reps >= rangeMin -> hold weight, increment reps toward rangeMax
 * - Case 3: Avg < rangeMin AND worst < rangeMin-2 -> decrease weight 5%
 * - Case 4: Else (fatigue on last set only) -> hold weight and reps
 */
object ProgressionCalculator {

    private const val WEIGHT_STEP = 1.25
    private const val DELOAD_FACTOR = 0.95
    private const val STALL_SESSION_COUNT = 3
    private const val STALL_TOLERANCE = 0.01

    private const val LOWER_COMPOUND_INCREMENT = 2.5
    private const val UPPER_COMPOUND_INCREMENT = 1.25
    private const val ISOLATION_INCREMENT = 1.25

    private const val LOWER_COMPOUND_CAP = 10.0
    private const val UPPER_COMPOUND_CAP = 5.0
    private const val ISOLATION_CAP = 2.5

    private val LOWER_BODY_GROUPS = setOf("legs", "lower_back")

    /**
     * Computes the next session's weight and rep targets based on training history.
     *
     * @param lastSessions Historical sessions for this exercise, most recent first.
     *   Only WORKING sets (setType == "working") are considered.
     * @param rangeMin Minimum target reps (from user profile rep range).
     * @param rangeMax Maximum target reps (from user profile rep range).
     * @param isCompound Whether this is a compound (multi-joint) exercise.
     * @param isLowerBody Whether this exercise targets lower body (legs/lower_back).
     * @param fallbackWeightKg BW-ratio-based weight to use when no history exists.
     * @return [ProgressionResult] with the recommended weight, reps, and stall info.
     */
    @Suppress("LongParameterList")
    fun compute(
        lastSessions: List<HistoricalSession>,
        rangeMin: Int,
        rangeMax: Int,
        isCompound: Boolean,
        isLowerBody: Boolean,
        fallbackWeightKg: Double,
    ): ProgressionResult {
        val workingSets = extractWorkingSets(lastSessions)
            ?: return coldStartResult(fallbackWeightKg, rangeMin)

        val worstSetReps = workingSets.minOf { it.reps }
        val avgReps = workingSets.map { it.reps }.average()
        val lastWeight = workingSets.maxOf { it.weight }
        val stalled = detectStall(lastSessions)

        val result = computeProgression(
            worstSetReps,
            avgReps,
            lastWeight,
            rangeMin,
            rangeMax,
            isCompound,
            isLowerBody,
            stalled,
        )
        return if (stalled) {
            result.copy(stallNote = "Weight unchanged for $STALL_SESSION_COUNT sessions. Consider a deload.")
        } else {
            result
        }
    }

    private fun coldStartResult(fallbackWeightKg: Double, rangeMin: Int) = ProgressionResult(
        weightKg = fallbackWeightKg,
        targetReps = rangeMin,
        isStalled = false,
    )

    private fun extractWorkingSets(sessions: List<HistoricalSession>): List<HistoricalSet>? {
        if (sessions.isEmpty()) return null
        val sets = sessions.first().sets.filter { it.setType == "working" }
        return sets.ifEmpty { null }
    }

    @Suppress("LongParameterList")
    private fun computeProgression(
        worstSetReps: Int,
        avgReps: Double,
        lastWeight: Double,
        rangeMin: Int,
        rangeMax: Int,
        isCompound: Boolean,
        isLowerBody: Boolean,
        stalled: Boolean,
    ): ProgressionResult = when {
        worstSetReps >= rangeMax -> increaseWeight(lastWeight, rangeMin, isCompound, isLowerBody, stalled)
        avgReps >= rangeMin -> holdWeight(lastWeight, avgReps, rangeMax, stalled)
        worstSetReps < (rangeMin - 2) -> decreaseWeight(lastWeight, rangeMin, stalled)
        else -> holdWeight(lastWeight, avgReps, rangeMax, stalled)
    }

    private fun increaseWeight(
        lastWeight: Double,
        rangeMin: Int,
        isCompound: Boolean,
        isLowerBody: Boolean,
        stalled: Boolean,
    ): ProgressionResult {
        val cap = getCap(isCompound, isLowerBody)
        val rawNext = lastWeight + getIncrement(isCompound, isLowerBody)
        return ProgressionResult(
            weightKg = roundToNearestStep(rawNext.coerceAtMost(lastWeight + cap)),
            targetReps = rangeMin,
            isStalled = stalled,
        )
    }

    private fun holdWeight(lastWeight: Double, avgReps: Double, rangeMax: Int, stalled: Boolean) =
        ProgressionResult(
            weightKg = roundToNearestStep(lastWeight),
            targetReps = (floor(avgReps).toInt() + 1).coerceAtMost(rangeMax),
            isStalled = stalled,
        )

    private fun decreaseWeight(lastWeight: Double, rangeMin: Int, stalled: Boolean) = ProgressionResult(
        weightKg = roundToNearestStep(lastWeight * DELOAD_FACTOR),
        targetReps = rangeMin,
        isStalled = stalled,
    )

    /**
     * Determines whether the given primary muscle group is a lower body group.
     */
    fun isLowerBodyGroup(primaryGroup: String): Boolean =
        primaryGroup in LOWER_BODY_GROUPS

    private fun getIncrement(isCompound: Boolean, isLowerBody: Boolean): Double = when {
        isCompound && isLowerBody -> LOWER_COMPOUND_INCREMENT
        isCompound -> UPPER_COMPOUND_INCREMENT
        else -> ISOLATION_INCREMENT
    }

    private fun getCap(isCompound: Boolean, isLowerBody: Boolean): Double = when {
        isCompound && isLowerBody -> LOWER_COMPOUND_CAP
        isCompound -> UPPER_COMPOUND_CAP
        else -> ISOLATION_CAP
    }

    /**
     * Detects whether the user is stalled: same working weight across
     * the last [STALL_SESSION_COUNT] sessions.
     */
    private fun detectStall(sessions: List<HistoricalSession>): Boolean {
        if (sessions.size < STALL_SESSION_COUNT) return false

        val recentSessions = sessions.take(STALL_SESSION_COUNT)
        val weights = recentSessions.mapNotNull { session ->
            val working = session.sets.filter { it.setType == "working" }
            if (working.isEmpty()) null else working.maxOf { it.weight }
        }

        if (weights.size < STALL_SESSION_COUNT) return false

        val firstWeight = weights.first()
        return weights.all { kotlin.math.abs(it - firstWeight) < STALL_TOLERANCE }
    }

    /**
     * Rounds weight to nearest 1.25 kg step.
     */
    private fun roundToNearestStep(weight: Double): Double {
        val steps = (weight / WEIGHT_STEP).roundToInt()
        return steps * WEIGHT_STEP
    }
}
