package com.deepreps.core.domain.model.enums

/**
 * Personal record categories.
 *
 * - [MAX_WEIGHT]: Heaviest weight lifted for any rep count.
 * - [MAX_REPS]: Most reps completed at any weight.
 * - [MAX_VOLUME]: Highest single-set volume (weight x reps).
 * - [MAX_ESTIMATED_1RM]: Highest estimated 1RM (Epley formula).
 */
enum class RecordType(val value: String) {
    MAX_WEIGHT("weight"),
    MAX_REPS("reps"),
    MAX_VOLUME("volume"),
    MAX_ESTIMATED_1RM("estimated_1rm");

    companion object {
        fun fromValue(value: String): RecordType =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown record type: $value")
    }
}
