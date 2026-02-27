package com.deepreps.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.deepreps.core.database.entity.BodyWeightEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyWeightDao {

    @Insert
    suspend fun insert(entry: BodyWeightEntryEntity): Long

    @Query("SELECT * FROM body_weight_entries ORDER BY recorded_at DESC")
    fun getAll(): Flow<List<BodyWeightEntryEntity>>

    @Query("SELECT * FROM body_weight_entries ORDER BY recorded_at DESC LIMIT 1")
    suspend fun getLatest(): BodyWeightEntryEntity?

    @Query("SELECT * FROM body_weight_entries ORDER BY recorded_at DESC LIMIT 1")
    fun observeLatest(): Flow<BodyWeightEntryEntity?>

    @Query("SELECT * FROM body_weight_entries ORDER BY recorded_at DESC")
    suspend fun getAllOnce(): List<BodyWeightEntryEntity>

    @Insert
    suspend fun insertAll(entries: List<BodyWeightEntryEntity>)

    @Query("DELETE FROM body_weight_entries")
    suspend fun deleteAll()
}
