package com.deepreps.core.data.repository

import com.deepreps.core.common.dispatcher.DispatcherProvider
import com.deepreps.core.data.mapper.toDomain
import com.deepreps.core.database.dao.ExerciseDao
import com.deepreps.core.database.dao.MuscleGroupDao
import com.deepreps.core.domain.model.Exercise
import com.deepreps.core.domain.model.MuscleGroupModel
import com.deepreps.core.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class ExerciseRepositoryImpl @Inject constructor(
    private val muscleGroupDao: MuscleGroupDao,
    private val exerciseDao: ExerciseDao,
    private val dispatchers: DispatcherProvider,
) : ExerciseRepository {

    override fun getMuscleGroups(): Flow<List<MuscleGroupModel>> =
        muscleGroupDao.getAll()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatchers.io)

    override fun getAllExercises(): Flow<List<Exercise>> =
        exerciseDao.getAll()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatchers.io)

    override fun getExercisesByGroup(groupId: Long): Flow<List<Exercise>> =
        exerciseDao.getByGroupId(groupId)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatchers.io)

    override suspend fun getExerciseById(id: Long): Exercise? =
        withContext(dispatchers.io) {
            exerciseDao.getById(id)?.toDomain()
        }

    override suspend fun getExercisesByIds(ids: List<Long>): List<Exercise> =
        withContext(dispatchers.io) {
            exerciseDao.getByIds(ids).map { it.toDomain() }
        }

    override fun searchExercises(query: String): Flow<List<Exercise>> =
        exerciseDao.searchByName(query)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatchers.io)

    override fun getExercisesWithMuscles(groupId: Long): Flow<List<Exercise>> =
        exerciseDao.getExercisesWithMuscles(groupId)
            .map { relations -> relations.map { it.exercise.toDomain() } }
            .flowOn(dispatchers.io)
}
