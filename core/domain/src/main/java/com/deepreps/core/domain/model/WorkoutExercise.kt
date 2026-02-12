package com.deepreps.core.domain.model

/**
 * Domain representation of an exercise within a workout session.
 *
 * [supersetGroupId] links exercises in the same superset. Null means standalone.
 * [restTimerSeconds] is the per-exercise rest duration from the AI plan or user override.
 */
data class WorkoutExercise(
    val id: Long,
    val sessionId: Long,
    val exerciseId: Long,
    val orderIndex: Int,
    val supersetGroupId: Int?,
    val restTimerSeconds: Int?,
    val notes: String?,
)
