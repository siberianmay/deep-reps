package com.deepreps.core.data.repository

import com.deepreps.core.common.dispatcher.DispatcherProvider
import com.deepreps.core.data.mapper.toDomain
import com.deepreps.core.data.mapper.toEntity
import com.deepreps.core.database.dao.BodyWeightDao
import com.deepreps.core.domain.model.BodyWeightEntry
import com.deepreps.core.domain.repository.BodyWeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class BodyWeightRepositoryImpl @Inject constructor(
    private val bodyWeightDao: BodyWeightDao,
    private val dispatchers: DispatcherProvider,
) : BodyWeightRepository {

    override fun getAll(): Flow<List<BodyWeightEntry>> =
        bodyWeightDao.getAll()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatchers.io)

    override suspend fun getLatest(): BodyWeightEntry? =
        withContext(dispatchers.io) {
            bodyWeightDao.getLatest()?.toDomain()
        }

    override fun observeLatest(): Flow<BodyWeightEntry?> =
        bodyWeightDao.observeLatest()
            .map { entity -> entity?.toDomain() }
            .flowOn(dispatchers.io)

    override suspend fun insert(entry: BodyWeightEntry): Long =
        withContext(dispatchers.io) {
            bodyWeightDao.insert(entry.toEntity())
        }
}
