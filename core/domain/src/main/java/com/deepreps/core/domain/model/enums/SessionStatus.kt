package com.deepreps.core.domain.model.enums

/**
 * Workout session lifecycle status.
 *
 * - [ACTIVE]: Workout currently in progress.
 * - [PAUSED]: User explicitly paused.
 * - [COMPLETED]: User finished normally.
 * - [DISCARDED]: User explicitly chose to discard (UI confirmation required).
 * - [ABANDONED]: User never returned; detected after 24-hour timeout by cleanup logic.
 * - [CRASHED]: App crashed during session; detected on next startup via active session recovery.
 */
enum class SessionStatus(val value: String) {
    ACTIVE("active"),
    PAUSED("paused"),
    COMPLETED("completed"),
    DISCARDED("discarded"),
    ABANDONED("abandoned"),
    CRASHED("crashed");

    companion object {
        fun fromValue(value: String): SessionStatus =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown session status: $value")
    }
}
