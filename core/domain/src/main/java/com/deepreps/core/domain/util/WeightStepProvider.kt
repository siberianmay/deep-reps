package com.deepreps.core.domain.util

import com.deepreps.core.domain.model.enums.Equipment

/**
 * Provides equipment-based weight increments.
 *
 * Per architecture.md Section 4.13:
 * - Barbell: 2.5 kg
 * - Dumbbell: 2.5 kg
 * - Cable/Machine: 5.0 kg (weight stacks use larger increments)
 * - Bodyweight: 0.0 (progression via reps, not weight)
 * - Kettlebell: 4.0 kg (standard KB increments)
 * - Band: 0.0 (no weight increment)
 * - EZ Bar: 2.5 kg
 * - Trap Bar: 2.5 kg
 *
 * All values are in kg. The UI layer converts to lbs if needed.
 * Pure Kotlin, no dependencies.
 */
object WeightStepProvider {

    /**
     * Returns the minimum weight increment in kg for the given equipment type.
     */
    fun getIncrementKg(equipment: Equipment): Double = when (equipment) {
        Equipment.BARBELL -> 2.5
        Equipment.DUMBBELL -> 2.5
        Equipment.CABLE -> 5.0
        Equipment.MACHINE -> 5.0
        Equipment.BODYWEIGHT -> 0.0
        Equipment.KETTLEBELL -> 4.0
        Equipment.BAND -> 0.0
        Equipment.EZ_BAR -> 2.5
        Equipment.TRAP_BAR -> 2.5
    }

    /**
     * Rounds a weight DOWN to the nearest increment for the given equipment.
     *
     * Per exercise-science.md Section 8.7: always round DOWN.
     * If increment is 0 (bodyweight/band), returns the original weight unchanged.
     */
    fun roundDown(weightKg: Double, equipment: Equipment): Double {
        val increment = getIncrementKg(equipment)
        if (increment <= 0.0) return weightKg
        return kotlin.math.floor(weightKg / increment) * increment
    }
}
