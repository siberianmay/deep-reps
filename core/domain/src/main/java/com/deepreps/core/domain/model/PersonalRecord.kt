package com.deepreps.core.domain.model

import com.deepreps.core.domain.model.enums.RecordType

/**
 * Domain representation of a personal record for a specific exercise.
 *
 * All weight values are in kg.
 * [estimated1rm] is computed via the Epley formula and stored for historical tracking.
 * [sessionId] links back to the workout where this PR was achieved. Nullable if the session
 * was deleted but the PR was preserved.
 */
@Suppress("ForbiddenPublicDataClass")
data class PersonalRecord(
    val id: Long,
    val exerciseId: Long,
    val recordType: RecordType,
    val weightValue: Double?,
    val reps: Int?,
    val estimated1rm: Double?,
    val achievedAt: Long,
    val sessionId: Long?,
)
