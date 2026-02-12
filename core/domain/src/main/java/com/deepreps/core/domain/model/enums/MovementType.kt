package com.deepreps.core.domain.model.enums

/**
 * Movement classification for exercises.
 *
 * - [COMPOUND]: Multi-joint movements (e.g., squat, bench press).
 * - [ISOLATION]: Single-joint movements (e.g., leg extension, bicep curl).
 */
enum class MovementType(val value: String) {
    COMPOUND("compound"),
    ISOLATION("isolation");

    companion object {
        fun fromValue(value: String): MovementType =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown movement type: $value")
    }
}
