package com.deepreps.core.domain.repository

import com.deepreps.core.domain.model.WorkoutExercise
import com.deepreps.core.domain.model.WorkoutSession
import com.deepreps.core.domain.model.WorkoutSet
import kotlinx.coroutines.flow.Flow

/**
 * Repository for workout session data.
 *
 * Handles the full lifecycle: create, update status, complete sets, query history.
 * Flow-based queries for reactive UI. Suspend functions for one-shot writes.
 * All writes are immediate (defensive persistence -- see architecture.md Section 3.7).
 */
interface WorkoutSessionRepository {

    /** Creates a new session and returns its generated ID. */
    suspend fun createSession(session: WorkoutSession): Long

    /** Returns a session by ID. Null if not found. */
    suspend fun getSession(id: Long): WorkoutSession?

    /** Observe a session by ID for reactive updates. */
    fun observeSession(id: Long): Flow<WorkoutSession?>

    /** Returns the currently active or paused session. At most one exists at a time. */
    suspend fun getActiveSession(): WorkoutSession?

    /** Observe the active/paused session reactively. */
    fun observeActiveSession(): Flow<WorkoutSession?>

    /** Full update of session fields. */
    suspend fun updateSession(session: WorkoutSession)

    /** Updates only the status and completedAt fields. */
    suspend fun updateStatus(id: Long, status: String, completedAt: Long?)

    /** Returns all completed sessions, newest first. */
    fun getCompletedSessions(): Flow<List<WorkoutSession>>

    /** Returns sessions started within the given epoch millis range. */
    suspend fun getSessionsInRange(startEpoch: Long, endEpoch: Long): List<WorkoutSession>

    /** Returns workout exercises for a session, ordered by orderIndex. */
    fun getExercisesForSession(sessionId: Long): Flow<List<WorkoutExercise>>

    /** Returns all sets for a given workout exercise. */
    fun getSetsForExercise(workoutExerciseId: Long): Flow<List<WorkoutSet>>

    /** Inserts workout exercises for a session. */
    suspend fun insertExercises(exercises: List<WorkoutExercise>)

    /** Inserts sets for a workout exercise. */
    suspend fun insertSets(sets: List<WorkoutSet>)

    /**
     * Inserts a single set for a specific workout exercise.
     * The [workoutExerciseId] is used to associate the set with its parent exercise
     * in the entity layer. Returns the Room-generated set ID.
     */
    suspend fun insertSet(workoutExerciseId: Long, set: WorkoutSet): Long

    /** Deletes a set by ID. Only non-completed sets should be deleted. */
    suspend fun deleteSet(setId: Long)

    /**
     * Marks a set as completed with actual weight and reps.
     * Writes immediately to Room (defensive persistence).
     */
    suspend fun completeSet(
        workoutExerciseId: Long,
        setIndex: Int,
        weight: Double,
        reps: Int,
    )

    /**
     * Returns sessions with status "active" that were started before [cutoffMillis].
     * Used by [CleanupStaleSessionsUseCase] to find sessions older than 24 hours.
     */
    suspend fun getStaleActiveSessions(cutoffMillis: Long): List<WorkoutSession>

    /**
     * Atomically inserts a workout exercise and all its sets in a single transaction.
     * Either all rows are written or none. Required for data integrity when adding
     * exercises mid-workout.
     */
    suspend fun insertExerciseWithSets(exercise: WorkoutExercise, sets: List<WorkoutSet>)

    /**
     * Updates the notes for a specific workout exercise.
     * Used for per-exercise notes during active workout logging.
     *
     * @param workoutExerciseId The Room PK of the workout exercise.
     * @param notes The notes text, or null to clear.
     */
    suspend fun updateExerciseNotes(workoutExerciseId: Long, notes: String?)
}
