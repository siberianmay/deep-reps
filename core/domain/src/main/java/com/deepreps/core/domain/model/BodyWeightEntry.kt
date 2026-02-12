package com.deepreps.core.domain.model

/**
 * Domain representation of a body weight measurement.
 *
 * [weightKg] is always in kilograms. Conversion to display unit happens in the UI layer.
 * [recordedAt] is epoch millis.
 */
data class BodyWeightEntry(
    val id: Long,
    val weightKg: Double,
    val recordedAt: Long,
)
