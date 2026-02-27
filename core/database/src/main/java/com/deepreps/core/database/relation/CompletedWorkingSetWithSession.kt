package com.deepreps.core.database.relation

import androidx.room.ColumnInfo

/**
 * Flat projection of a completed working set joined with its session's start time.
 *
 * Used by [WorkoutSetDao.getCompletedWorkingSetsByExercise] for exercise history queries.
 * Room maps the column aliases from the raw SQL query to these fields.
 */
@Suppress("ForbiddenPublicDataClass")
data class CompletedWorkingSetWithSession(
    @ColumnInfo(name = "actual_weight")
    val actualWeight: Double?,
    @ColumnInfo(name = "actual_reps")
    val actualReps: Int?,
    @ColumnInfo(name = "session_date")
    val sessionDate: Long,
)
