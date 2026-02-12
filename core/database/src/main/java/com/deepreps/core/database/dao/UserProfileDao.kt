package com.deepreps.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.deepreps.core.database.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    /** Insert or update the singleton user profile row (id=1). */
    @Upsert
    suspend fun upsert(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun get(): UserProfileEntity?

    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun observe(): Flow<UserProfileEntity?>
}
