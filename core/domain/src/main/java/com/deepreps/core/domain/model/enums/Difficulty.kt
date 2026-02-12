package com.deepreps.core.domain.model.enums

/**
 * Exercise difficulty level.
 *
 * Used for safety guardrails and auto-ordering.
 * Advanced exercises must not appear in beginner auto-generated plans.
 */
enum class Difficulty(val value: String) {
    BEGINNER("beginner"),
    INTERMEDIATE("intermediate"),
    ADVANCED("advanced");

    companion object {
        fun fromValue(value: String): Difficulty =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown difficulty: $value")
    }
}
