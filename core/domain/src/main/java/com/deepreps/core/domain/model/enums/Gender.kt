package com.deepreps.core.domain.model.enums

/**
 * User gender for baseline weight ratio calculations.
 *
 * Optional field. When null/unknown, male ratios are reduced by 15%.
 */
enum class Gender(val value: String) {
    MALE("male"),
    FEMALE("female");

    companion object {
        fun fromValue(value: String): Gender? =
            entries.firstOrNull { it.value == value }
    }
}
