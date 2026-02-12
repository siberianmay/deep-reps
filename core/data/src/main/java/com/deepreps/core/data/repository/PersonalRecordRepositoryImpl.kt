package com.deepreps.core.data.repository

import com.deepreps.core.common.dispatcher.DispatcherProvider
import com.deepreps.core.data.mapper.toDomain
import com.deepreps.core.data.mapper.toEntity
import com.deepreps.core.database.dao.PersonalRecordDao
import com.deepreps.core.domain.model.PersonalRecord
import com.deepreps.core.domain.repository.PersonalRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PersonalRecordRepositoryImpl @Inject constructor(
    private val personalRecordDao: PersonalRecordDao,
    private val dispatchers: DispatcherProvider,
) : PersonalRecordRepository {

    override suspend fun getByExercise(exerciseId: Long): List<PersonalRecord> =
        withContext(dispatchers.io) {
            personalRecordDao.getByExercise(exerciseId).map { it.toDomain() }
        }

    override suspend fun getBestByType(
        exerciseId: Long,
        recordType: String,
    ): PersonalRecord? =
        withContext(dispatchers.io) {
            personalRecordDao.getBestByType(exerciseId, recordType)?.toDomain()
        }

    override suspend fun insertAll(records: List<PersonalRecord>) =
        withContext(dispatchers.io) {
            personalRecordDao.insertAll(records.map { it.toEntity() })
        }

    override fun observeAll(): Flow<List<PersonalRecord>> =
        personalRecordDao.observeAll()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatchers.io)

    override fun observeByExercise(exerciseId: Long): Flow<List<PersonalRecord>> =
        personalRecordDao.observeByExercise(exerciseId)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatchers.io)
}
