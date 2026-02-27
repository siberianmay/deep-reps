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

        fun defaultRepRanges(level: ExperienceLevel): RepRangeDefaults =
            when (level) {
                BEGINNER -> RepRangeDefaults(
                    compoundRepMin = 8,
                    compoundRepMax = 12,
                    isolationRepMin = 12,
                    isolationRepMax = 15,
                )
                INTERMEDIATE -> RepRangeDefaults(
                    compoundRepMin = 6,
                    compoundRepMax = 10,
                    isolationRepMin = 10,
                    isolationRepMax = 15,
                )
                ADVANCED -> RepRangeDefaults(
                    compoundRepMin = 4,
                    compoundRepMax = 8,
                    isolationRepMin = 8,
                    isolationRepMax = 15,
                )
            }
    }
}

/**
 * Default rep range configuration for compound and isolation exercises.
 * Returned by [ExperienceLevel.defaultRepRanges].
 */
@Suppress("ForbiddenPublicDataClass")
data class RepRangeDefaults(
    val compoundRepMin: Int,
    val compoundRepMax: Int,
    val isolationRepMin: Int,
    val isolationRepMax: Int,
)
