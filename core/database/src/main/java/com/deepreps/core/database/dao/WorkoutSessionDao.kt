package com.deepreps.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.deepreps.core.database.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
@Suppress("TooManyFunctions")
interface WorkoutSessionDao {

    @Insert
    suspend fun insert(session: WorkoutSessionEntity): Long

    @Update
    suspend fun update(session: WorkoutSessionEntity)

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getById(id: Long): WorkoutSessionEntity?

    /** Returns the active or paused session. There should be at most one. */
    @Query("SELECT * FROM workout_sessions WHERE status IN ('active', 'paused') LIMIT 1")
    suspend fun getActiveSession(): WorkoutSessionEntity?

    @Query(
        "SELECT * FROM workout_sessions WHERE status = 'completed' ORDER BY started_at DESC"
    )
    fun getCompletedSessions(): Flow<List<WorkoutSessionEntity>>

    @Query(
        "SELECT * FROM workout_sessions WHERE started_at >= :start AND started_at <= :end ORDER BY started_at ASC"
    )
    suspend fun getSessionsInRange(start: Long, end: Long): List<WorkoutSessionEntity>

    @Query("UPDATE workout_sessions SET status = :status, completed_at = :completedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, completedAt: Long?)

    /** Observe session by ID for reactive UI updates. */
    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    fun observeById(id: Long): Flow<WorkoutSessionEntity?>

    /** Observe active/paused session for the workout screen. */
    @Query("SELECT * FROM workout_sessions WHERE status IN ('active', 'paused') LIMIT 1")
    fun observeActiveSession(): Flow<WorkoutSessionEntity?>

    /**
     * Returns sessions with status "active" that were started before [cutoff] epoch millis.
     * Used for 24-hour stale session cleanup.
     */
    @Query("SELECT * FROM workout_sessions WHERE status = 'active' AND started_at < :cutoff")
    suspend fun getStaleActiveSessions(cutoff: Long): List<WorkoutSessionEntity>

    @Query("SELECT * FROM workout_sessions ORDER BY started_at DESC")
    suspend fun getAllOnce(): List<WorkoutSessionEntity>

    @Insert
    suspend fun insertAll(sessions: List<WorkoutSessionEntity>)

    @Query("DELETE FROM workout_sessions")
    suspend fun deleteAll()
}
