package com.deepreps.core.domain.model

/**
 * DUP day type for intermediate+ users.
 *
 * Per exercise-science.md Section 3.2:
 * - Hypertrophy: 8-12 reps, 65-75% 1RM
 * - Strength: 4-6 reps, 80-87.5% 1RM
 * - Power: 6-8 reps, 75-82.5% 1RM
 *
 * Cycling order: HYPERTROPHY -> STRENGTH -> POWER -> HYPERTROPHY.
 */
enum class SessionDayType(val value: String) {
    HYPERTROPHY("hypertrophy"),
    STRENGTH("strength"),
    POWER("power"),
}
