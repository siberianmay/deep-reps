package com.deepreps.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.deepreps.core.database.entity.WorkoutSetEntity
import com.deepreps.core.database.relation.CompletedWorkingSetWithSession
import kotlinx.coroutines.flow.Flow

@Dao
@Suppress("TooManyFunctions")
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
            is_completed = 1, completed_at = :completedAt, status = 'completed'
        WHERE id = :id
        """
    )
    suspend fun markCompleted(id: Long, actualWeight: Double, actualReps: Int, completedAt: Long)

    /** Updates only the status column for a set (e.g. skip/unskip). */
    @Query("UPDATE workout_sets SET status = :status WHERE id = :setId")
    suspend fun updateStatus(setId: Long, status: String)

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
            is_completed = :isCompleted, completed_at = :completedAt,
            status = CASE WHEN :isCompleted THEN 'completed' ELSE 'planned' END
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

    /**
     * Returns completed working sets for a given exercise, joined with the session start time.
     *
     * Filters to working sets only (excludes warm-up) and completed status only.
     * Only includes sets from completed sessions. Ordered by session date descending
     * so the caller can group and limit by session count in Kotlin.
     */
    @Query(
        """
        SELECT ws.actual_weight, ws.actual_reps, wse.started_at AS session_date
        FROM workout_sets ws
        INNER JOIN workout_exercises we ON ws.workout_exercise_id = we.id
        INNER JOIN workout_sessions wse ON we.session_id = wse.id
        WHERE we.exercise_id = :exerciseId
          AND ws.set_type = 'working'
          AND ws.status = 'completed'
          AND wse.status = 'completed'
        ORDER BY wse.started_at DESC, ws.set_index ASC
        """
    )
    suspend fun getCompletedWorkingSetsByExercise(
        exerciseId: Long,
    ): List<CompletedWorkingSetWithSession>

    @Query("SELECT * FROM workout_sets ORDER BY workout_exercise_id, set_index ASC")
    suspend fun getAllOnce(): List<WorkoutSetEntity>

    @Query("DELETE FROM workout_sets")
    suspend fun deleteAll()
}
