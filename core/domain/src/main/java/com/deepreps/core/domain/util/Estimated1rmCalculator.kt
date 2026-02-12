package com.deepreps.core.domain.util

/**
 * Estimated 1RM calculator using the Epley and Brzycki formulas.
 *
 * Per exercise-science.md Section 7:
 * - Epley (primary): weight * (1 + reps / 30)
 * - Brzycki (validation): weight * (36 / (37 - reps))
 *
 * Guards:
 * - Reps must be in 1..30.
 * - Weight must be > 0.
 * - For 1 rep, estimated 1RM = actual weight lifted.
 *
 * Pure Kotlin, no dependencies.
 */
object Estimated1rmCalculator {

    /**
     * Calculates estimated 1RM using the Epley formula (primary).
     *
     * @param weightKg weight lifted in kg. Must be > 0.
     * @param reps number of repetitions. Must be in 1..30.
     * @return estimated 1RM in kg, or null if inputs are invalid.
     */
    fun epley(weightKg: Double, reps: Int): Double? {
        if (!isValid(weightKg, reps)) return null
        if (reps == 1) return weightKg
        return weightKg * (1.0 + reps.toDouble() / 30.0)
    }

    /**
     * Calculates estimated 1RM using the Brzycki formula (validation/secondary).
     *
     * Accurate for reps <= 10. Diverges significantly above that.
     * Undefined at 37 reps (division by zero); input capped at 36.
     *
     * @param weightKg weight lifted in kg. Must be > 0.
     * @param reps number of repetitions. Must be in 1..36.
     * @return estimated 1RM in kg, or null if inputs are invalid.
     */
    fun brzycki(weightKg: Double, reps: Int): Double? {
        if (weightKg <= 0.0 || reps < 1 || reps > 36) return null
        if (reps == 1) return weightKg
        return weightKg * (36.0 / (37.0 - reps.toDouble()))
    }

    /**
     * Returns the primary estimated 1RM (Epley) with a confidence level.
     *
     * Per exercise-science.md Section 7.4.2:
     * - 1-5 reps: High confidence
     * - 6-10 reps: Moderate confidence
     * - 11-20 reps: Low confidence (display with warning, do not use for PR detection)
     * - 21+ reps: Invalid (do not calculate)
     */
    fun calculateWithConfidence(weightKg: Double, reps: Int): Estimated1rmResult? {
        if (weightKg <= 0.0 || reps < 1) return null
        if (reps > 20) return null // Endurance set -- not valid for 1RM estimation

        val estimated = epley(weightKg, reps) ?: return null
        val confidence = when {
            reps <= 5 -> Confidence.HIGH
            reps <= 10 -> Confidence.MODERATE
            else -> Confidence.LOW
        }
        return Estimated1rmResult(
            estimatedKg = estimated,
            confidence = confidence,
            usableForPr = confidence != Confidence.LOW,
        )
    }

    private fun isValid(weightKg: Double, reps: Int): Boolean =
        weightKg > 0.0 && reps in 1..30

    data class Estimated1rmResult(
        val estimatedKg: Double,
        val confidence: Confidence,
        val usableForPr: Boolean,
    )

    enum class Confidence {
        HIGH,
        MODERATE,
        LOW,
    }
}
