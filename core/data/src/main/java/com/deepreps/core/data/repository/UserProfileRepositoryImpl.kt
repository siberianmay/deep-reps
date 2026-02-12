package com.deepreps.core.data.repository

import com.deepreps.core.common.dispatcher.DispatcherProvider
import com.deepreps.core.data.mapper.toDomain
import com.deepreps.core.data.mapper.toEntity
import com.deepreps.core.database.dao.UserProfileDao
import com.deepreps.core.domain.model.UserProfile
import com.deepreps.core.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class UserProfileRepositoryImpl @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val dispatchers: DispatcherProvider,
) : UserProfileRepository {

    override suspend fun get(): UserProfile? =
        withContext(dispatchers.io) {
            userProfileDao.get()?.toDomain()
        }

    override fun observe(): Flow<UserProfile?> =
        userProfileDao.observe()
            .map { entity -> entity?.toDomain() }
            .flowOn(dispatchers.io)

    override suspend fun save(profile: UserProfile) =
        withContext(dispatchers.io) {
            userProfileDao.upsert(profile.toEntity())
        }
}
