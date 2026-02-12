package com.deepreps.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single set within a workout exercise.
 *
 * All weight values are stored in kilograms.
 * Planned values come from AI plan or manual entry.
 * Actual values are filled in as the user completes sets.
 */
@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["workout_exercise_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("workout_exercise_id"),
        Index(value = ["workout_exercise_id", "is_completed"]),
    ],
)
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "workout_exercise_id")
    val workoutExerciseId: Long,
    @ColumnInfo(name = "set_index")
    val setIndex: Int,
    /** "warmup" or "working" */
    @ColumnInfo(name = "set_type")
    val setType: String,
    /** Planned weight in kg. Null for bodyweight exercises. */
    @ColumnInfo(name = "planned_weight")
    val plannedWeight: Double?,
    @ColumnInfo(name = "planned_reps")
    val plannedReps: Int?,
    /** Actual weight in kg. Filled when set is completed. */
    @ColumnInfo(name = "actual_weight")
    val actualWeight: Double?,
    @ColumnInfo(name = "actual_reps")
    val actualReps: Int?,
    @ColumnInfo(name = "is_completed", defaultValue = "0")
    val isCompleted: Boolean = false,
    /** Epoch millis when set was completed. Null until completion. */
    @ColumnInfo(name = "completed_at")
    val completedAt: Long?,
)
