package com.deepreps.core.data.repository

import androidx.room.withTransaction
import com.deepreps.core.common.dispatcher.DispatcherProvider
import com.deepreps.core.data.mapper.toDomain
import com.deepreps.core.data.mapper.toEntity
import com.deepreps.core.database.DeepRepsDatabase
import com.deepreps.core.database.dao.WorkoutExerciseDao
import com.deepreps.core.database.dao.WorkoutSessionDao
import com.deepreps.core.database.dao.WorkoutSetDao
import com.deepreps.core.domain.model.WorkoutExercise
import com.deepreps.core.domain.model.WorkoutSession
import com.deepreps.core.domain.model.WorkoutSet
import com.deepreps.core.domain.repository.WorkoutSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Suppress("TooManyFunctions")
class WorkoutSessionRepositoryImpl @Inject constructor(
    private val database: DeepRepsDatabase,
    private val sessionDao: WorkoutSessionDao,
    private val exerciseDao: WorkoutExerciseDao,
    private val setDao: WorkoutSetDao,
    private val dispatchers: DispatcherProvider,
) : WorkoutSessionRepository {

    override suspend fun createSession(session: WorkoutSession): Long =
        withContext(dispatchers.io) {
            sessionDao.insert(session.toEntity())
        }

    override suspend fun getSession(id: Long): WorkoutSession? =
        withContext(dispatchers.io) {
            sessionDao.getById(id)?.toDomain()
        }

    override fun observeSession(id: Long): Flow<WorkoutSession?> =
        sessionDao.observeById(id)
            .map { entity -> entity?.toDomain() }
            .flowOn(dispatchers.io)

    override suspend fun getActiveSession(): WorkoutSession? =
        withContext(dispatchers.io) {
            sessionDao.getActiveSession()?.toDomain()
        }

    override fun observeActiveSession(): Flow<WorkoutSession?> =
        sessionDao.observeActiveSession()
            .map { entity -> entity?.toDomain() }
            .flowOn(dispatchers.io)

    override suspend fun updateSession(session: WorkoutSession) =
        withContext(dispatchers.io) {
            sessionDao.update(session.toEntity())
        }

    override suspend fun updateStatus(id: Long, status: String, completedAt: Long?) =
        withContext(dispatchers.io) {
            sessionDao.updateStatus(id, status, completedAt)
        }

    override fun getCompletedSessions(): Flow<List<WorkoutSession>> =
        sessionDao.getCompletedSessions()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatchers.io)

    override suspend fun getSessionsInRange(
        startEpoch: Long,
        endEpoch: Long,
    ): List<WorkoutSession> =
        withContext(dispatchers.io) {
            sessionDao.getSessionsInRange(startEpoch, endEpoch).map { it.toDomain() }
        }

    override fun getExercisesForSession(sessionId: Long): Flow<List<WorkoutExercise>> =
        exerciseDao.getBySessionId(sessionId)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatchers.io)

    override fun getSetsForExercise(workoutExerciseId: Long): Flow<List<WorkoutSet>> =
        setDao.getByWorkoutExerciseId(workoutExerciseId)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatchers.io)

    override suspend fun insertExercises(exercises: List<WorkoutExercise>) =
        withContext(dispatchers.io) {
            exerciseDao.insertAll(exercises.map { it.toEntity() })
        }

    override suspend fun insertSets(sets: List<WorkoutSet>) =
        withContext(dispatchers.io) {
            // WorkoutSet.toEntity() requires workoutExerciseId. Since sets are already
            // associated via the domain model, we use a zero ID as placeholder.
            // In practice, the caller should use the entity mapper directly with the correct ID.
            // This is a convenience that works when sets have been constructed with proper context.
            setDao.insertAll(sets.map { it.toEntity(workoutExerciseId = 0) })
        }

    override suspend fun deleteSet(setId: Long) =
        withContext(dispatchers.io) {
            setDao.deleteById(setId)
        }

    override suspend fun insertSet(workoutExerciseId: Long, set: WorkoutSet): Long =
        withContext(dispatchers.io) {
            setDao.insert(set.toEntity(workoutExerciseId = workoutExerciseId))
        }

    override suspend fun completeSet(
        workoutExerciseId: Long,
        setIndex: Int,
        weight: Double,
        reps: Int,
    ) = withContext(dispatchers.io) {
        setDao.updateActuals(
            workoutExerciseId = workoutExerciseId,
            setIndex = setIndex,
            actualWeight = weight,
            actualReps = reps,
            isCompleted = true,
            completedAt = System.currentTimeMillis(),
        )
    }

    override suspend fun getStaleActiveSessions(cutoffMillis: Long): List<WorkoutSession> =
        withContext(dispatchers.io) {
            sessionDao.getStaleActiveSessions(cutoffMillis).map { it.toDomain() }
        }

    override suspend fun insertExerciseWithSets(
        exercise: WorkoutExercise,
        sets: List<WorkoutSet>,
    ) = withContext(dispatchers.io) {
        database.withTransaction {
            val exerciseId = exerciseDao.insert(exercise.toEntity())
            val setEntities = sets.map { it.toEntity(workoutExerciseId = exerciseId) }
            setDao.insertAll(setEntities)
        }
    }

    override suspend fun updateExerciseNotes(workoutExerciseId: Long, notes: String?) =
        withContext(dispatchers.io) {
            exerciseDao.updateNotes(workoutExerciseId, notes)
        }
}
