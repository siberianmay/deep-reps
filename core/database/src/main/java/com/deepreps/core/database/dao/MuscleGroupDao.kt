package com.deepreps.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deepreps.core.database.entity.MuscleGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MuscleGroupDao {

    @Query("SELECT * FROM muscle_groups ORDER BY display_order ASC")
    fun getAll(): Flow<List<MuscleGroupEntity>>

    @Query("SELECT * FROM muscle_groups WHERE id = :id")
    suspend fun getById(id: Long): MuscleGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(groups: List<MuscleGroupEntity>)
}
