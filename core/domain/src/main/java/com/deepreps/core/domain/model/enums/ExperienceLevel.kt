package com.deepreps.core.domain.model.enums

/**
 * User training experience level.
 *
 * Maps to integer values in the database (1, 2, 3).
 * Used for progression model selection, safety guardrails, and AI prompt context.
 */
enum class ExperienceLevel(val value: Int) {
    BEGINNER(1),
    INTERMEDIATE(2),
    ADVANCED(3);

    companion object {
        fun fromValue(value: Int): ExperienceLevel =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown experience level: $value")
    }
}
