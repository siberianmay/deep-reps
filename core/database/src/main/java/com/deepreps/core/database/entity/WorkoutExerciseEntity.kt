package com.deepreps.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * An exercise within a workout session, with its position and configuration.
 */
@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index("session_id"),
        Index("exercise_id"),
    ],
)
data class WorkoutExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "session_id")
    val sessionId: Long,
    @ColumnInfo(name = "exercise_id")
    val exerciseId: Long,
    @ColumnInfo(name = "order_index")
    val orderIndex: Int,
    /** Null = not in a superset. Same int value = same superset group. */
    @ColumnInfo(name = "superset_group_id")
    val supersetGroupId: Int?,
    @ColumnInfo(name = "rest_timer_seconds")
    val restTimerSeconds: Int?,
    @ColumnInfo(name = "notes")
    val notes: String?,
)
