package com.deepreps.core.domain.model.enums

/**
 * Type of an individual set within a workout exercise.
 *
 * - [WARMUP]: Preparatory set at reduced intensity.
 * - [WORKING]: Full-intensity training set that counts toward volume targets.
 */
enum class SetType(val value: String) {
    WARMUP("warmup"),
    WORKING("working");

    companion object {
        fun fromValue(value: String): SetType =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown set type: $value")
    }
}
