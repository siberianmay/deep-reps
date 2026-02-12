package com.deepreps.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.deepreps.core.database.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSetDao {

    @Insert
    suspend fun insert(set: WorkoutSetEntity): Long

    @Insert
    suspend fun insertAll(sets: List<WorkoutSetEntity>)

    @Update
    suspend fun update(set: WorkoutSetEntity)

    @Query("SELECT * FROM workout_sets WHERE workout_exercise_id = :workoutExerciseId ORDER BY set_index ASC")
    fun getByWorkoutExerciseId(workoutExerciseId: Long): Flow<List<WorkoutSetEntity>>

    /**
     * Returns all completed sets for a given exercise (across all sessions).
     * Requires joining through workout_exercises to find the exercise_id.
     * Used for progress tracking and PR detection.
     */
    @Query(
        """
        SELECT ws.* FROM workout_sets ws
        INNER JOIN workout_exercises we ON ws.workout_exercise_id = we.id
        WHERE we.exercise_id = :exerciseId AND ws.is_completed = 1
        ORDER BY ws.completed_at DESC
        """
    )
    suspend fun getCompletedSetsByExercise(exerciseId: Long): List<WorkoutSetEntity>

    @Query(
        """
        UPDATE workout_sets
        SET actual_weight = :actualWeight, actual_reps = :actualReps,
            is_completed = 1, completed_at = :completedAt
        WHERE id = :id
        """
    )
    suspend fun markCompleted(id: Long, actualWeight: Double, actualReps: Int, completedAt: Long)

    /** Deletes a set by its primary key. Only non-completed sets should be deleted. */
    @Query("DELETE FROM workout_sets WHERE id = :setId")
    suspend fun deleteById(setId: Long)

    /**
     * Bulk update for defensive persistence: updates actuals by workout_exercise_id and set_index.
     * Used by the repository's completeSet() method.
     */
    @Query(
        """
        UPDATE workout_sets
        SET actual_weight = :actualWeight, actual_reps = :actualReps,
            is_completed = :isCompleted, completed_at = :completedAt
        WHERE workout_exercise_id = :workoutExerciseId AND set_index = :setIndex
        """
    )
    @Suppress("LongParameterList")
    suspend fun updateActuals(
        workoutExerciseId: Long,
        setIndex: Int,
        actualWeight: Double,
        actualReps: Int,
        isCompleted: Boolean,
        completedAt: Long,
    )
}
