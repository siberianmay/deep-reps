package com.deepreps.core.data.repository

import com.deepreps.core.common.dispatcher.DispatcherProvider
import com.deepreps.core.data.mapper.toDomain
import com.deepreps.core.data.mapper.toEntity
import com.deepreps.core.database.dao.TemplateDao
import com.deepreps.core.domain.model.Template
import com.deepreps.core.domain.model.TemplateExercise
import com.deepreps.core.domain.model.TemplateWithCount
import com.deepreps.core.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TemplateRepositoryImpl @Inject constructor(
    private val templateDao: TemplateDao,
    private val dispatchers: DispatcherProvider,
) : TemplateRepository {

    override fun getAll(): Flow<List<Template>> =
        templateDao.getAll()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatchers.io)

    override fun getAllWithExerciseCount(): Flow<List<TemplateWithCount>> =
        templateDao.getAllWithExerciseCount()
            .map { projections -> projections.map { it.toDomain() } }
            .flowOn(dispatchers.io)

    override suspend fun getById(id: Long): Template? =
        withContext(dispatchers.io) {
            templateDao.getById(id)?.toDomain()
        }

    override suspend fun save(template: Template): Long =
        withContext(dispatchers.io) {
            templateDao.insert(template.toEntity())
        }

    override suspend fun update(template: Template) =
        withContext(dispatchers.io) {
            templateDao.update(template.toEntity())
        }

    override suspend fun delete(template: Template) =
        withContext(dispatchers.io) {
            templateDao.delete(template.toEntity())
        }

    override suspend fun getExercisesForTemplate(templateId: Long): List<TemplateExercise> =
        withContext(dispatchers.io) {
            templateDao.getTemplateExercises(templateId).map { it.toDomain() }
        }

    override fun observeExercisesForTemplate(templateId: Long): Flow<List<TemplateExercise>> =
        templateDao.observeTemplateExercises(templateId)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatchers.io)

    override suspend fun saveExercises(templateId: Long, exercises: List<TemplateExercise>) =
        withContext(dispatchers.io) {
            templateDao.deleteTemplateExercises(templateId)
            templateDao.insertTemplateExercises(exercises.map { it.toEntity() })
        }
}
