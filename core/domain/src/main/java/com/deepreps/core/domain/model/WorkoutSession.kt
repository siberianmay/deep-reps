package com.deepreps.core.domain.model

import com.deepreps.core.domain.model.enums.SessionStatus

/**
 * Domain representation of a workout session.
 *
 * All time values are epoch millis. Duration values are in seconds.
 * [status] governs the session lifecycle; valid transitions are enforced by [WorkoutStateMachine].
 */
@Suppress("ForbiddenPublicDataClass")
data class WorkoutSession(
    val id: Long,
    val startedAt: Long,
    val completedAt: Long?,
    val durationSeconds: Long?,
    val pausedDurationSeconds: Long,
    val status: SessionStatus,
    val notes: String?,
    val templateId: Long?,
)
