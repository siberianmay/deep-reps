package com.deepreps.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.deepreps.core.database.entity.WorkoutExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutExerciseDao {

    @Insert
    suspend fun insertAll(exercises: List<WorkoutExerciseEntity>)

    @Insert
    suspend fun insert(exercise: WorkoutExerciseEntity): Long

    @Query("SELECT * FROM workout_exercises WHERE session_id = :sessionId ORDER BY order_index ASC")
    fun getBySessionId(sessionId: Long): Flow<List<WorkoutExerciseEntity>>

    @Query("UPDATE workout_exercises SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String?)

    @Query("SELECT * FROM workout_exercises WHERE session_id = :sessionId ORDER BY order_index ASC")
    suspend fun getBySessionIdOnce(sessionId: Long): List<WorkoutExerciseEntity>

    @Query("DELETE FROM workout_exercises WHERE id = :id")
    suspend fun deleteById(id: Long)
}
