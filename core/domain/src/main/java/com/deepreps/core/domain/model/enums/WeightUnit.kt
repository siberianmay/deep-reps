package com.deepreps.core.domain.model.enums

/**
 * Preferred weight display unit.
 *
 * All weights are stored in kg internally. This enum controls UI display only.
 * Conversion factor: 1 kg = 2.20462 lbs.
 */
enum class WeightUnit(val value: String) {
    KG("kg"),
    LBS("lbs");

    companion object {
        fun fromValue(value: String): WeightUnit =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown weight unit: $value")
    }
}
