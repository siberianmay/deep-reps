package com.deepreps.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.deepreps.core.database.entity.ExerciseEntity
import com.deepreps.core.database.entity.ExerciseMuscleEntity
import com.deepreps.core.database.relation.ExerciseWithMuscles
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercises ORDER BY display_order ASC")
    fun getAll(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: Long): ExerciseEntity?

    @Query("SELECT * FROM exercises WHERE primary_group_id = :groupId ORDER BY display_order ASC")
    fun getByGroupId(groupId: Long): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' ORDER BY display_order ASC")
    fun searchByName(query: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercise_muscles WHERE exercise_id = :exerciseId")
    suspend fun getMusclesForExercise(exerciseId: Long): List<ExerciseMuscleEntity>

    @Transaction
    @Query("SELECT * FROM exercises WHERE primary_group_id = :groupId ORDER BY display_order ASC")
    fun getExercisesWithMuscles(groupId: Long): Flow<List<ExerciseWithMuscles>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMuscleLinks(links: List<ExerciseMuscleEntity>)
}
