package com.deepreps.core.domain.model.enums

/**
 * The 7 primary muscle groups used for workout categorization.
 * Each exercise belongs to exactly one primary group.
 */
enum class MuscleGroup(val value: String) {
    LEGS("legs"),
    LOWER_BACK("lower_back"),
    CHEST("chest"),
    BACK("back"),
    SHOULDERS("shoulders"),
    ARMS("arms"),
    CORE("core");

    companion object {
        fun fromValue(value: String): MuscleGroup =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown muscle group: $value")
    }
}
