package com.deepreps.core.domain.model.enums

/**
 * Equipment required for an exercise.
 *
 * 9 equipment types per the exercise-science spec.
 */
enum class Equipment(val value: String) {
    BARBELL("barbell"),
    DUMBBELL("dumbbell"),
    CABLE("cable"),
    MACHINE("machine"),
    BODYWEIGHT("bodyweight"),
    KETTLEBELL("kettlebell"),
    BAND("band"),
    EZ_BAR("ez_bar"),
    TRAP_BAR("trap_bar");

    companion object {
        fun fromValue(value: String): Equipment =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown equipment: $value")
    }
}
