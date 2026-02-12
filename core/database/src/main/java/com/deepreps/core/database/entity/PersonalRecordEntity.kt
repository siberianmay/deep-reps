package com.deepreps.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A personal record for a specific exercise.
 *
 * Record types: "weight", "reps", "estimated_1rm", "volume".
 * All weight values are stored in kg.
 */
@Entity(
    tableName = "personal_records",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("exercise_id"),
        Index("session_id"),
        Index(value = ["exercise_id", "record_type"]),
    ],
)
data class PersonalRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "exercise_id")
    val exerciseId: Long,
    /** "weight", "reps", "estimated_1rm", "volume" */
    @ColumnInfo(name = "record_type")
    val recordType: String,
    /** Weight in kg. Null for max_reps record type. */
    @ColumnInfo(name = "weight_value")
    val weightValue: Double?,
    @ColumnInfo(name = "reps")
    val reps: Int?,
    /** Estimated 1RM in kg via Epley formula. */
    @ColumnInfo(name = "estimated_1rm")
    val estimated1rm: Double?,
    /** Epoch millis when the record was achieved. */
    @ColumnInfo(name = "achieved_at")
    val achievedAt: Long,
    @ColumnInfo(name = "session_id")
    val sessionId: Long?,
)
