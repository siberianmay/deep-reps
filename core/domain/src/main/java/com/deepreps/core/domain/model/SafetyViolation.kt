package com.deepreps.core.domain.model

/**
 * A single safety violation detected during plan validation.
 *
 * Violations are warnings, not hard blocks. The user sees them on the
 * plan review screen and can override.
 */
data class SafetyViolation(
    val type: SafetyViolationType,
    val exerciseStableId: String?,
    val message: String,
    val severity: ViolationSeverity,
)

enum class SafetyViolationType {
    WEIGHT_JUMP_EXCEEDED,
    VOLUME_CEILING_EXCEEDED,
    AGE_INTENSITY_EXCEEDED,
    DIFFICULTY_GATING,
    REST_TOO_SHORT,
}

enum class ViolationSeverity {
    WARNING,
    HIGH,
}
