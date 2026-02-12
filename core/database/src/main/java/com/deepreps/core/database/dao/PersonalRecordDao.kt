package com.deepreps.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.deepreps.core.database.entity.PersonalRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalRecordDao {

    @Insert
    suspend fun insert(record: PersonalRecordEntity): Long

    @Insert
    suspend fun insertAll(records: List<PersonalRecordEntity>)

    @Query("SELECT * FROM personal_records WHERE exercise_id = :exerciseId ORDER BY achieved_at DESC")
    suspend fun getByExercise(exerciseId: Long): List<PersonalRecordEntity>

    @Query(
        """
        SELECT * FROM personal_records
        WHERE exercise_id = :exerciseId AND record_type = :recordType
        ORDER BY
            CASE record_type
                WHEN 'weight' THEN weight_value
                WHEN 'reps' THEN reps
                WHEN 'estimated_1rm' THEN estimated_1rm
                WHEN 'volume' THEN (COALESCE(weight_value, 0) * COALESCE(reps, 0))
            END DESC
        LIMIT 1
        """
    )
    suspend fun getBestByType(exerciseId: Long, recordType: String): PersonalRecordEntity?

    @Query("SELECT * FROM personal_records ORDER BY achieved_at DESC")
    fun observeAll(): Flow<List<PersonalRecordEntity>>

    @Query("SELECT * FROM personal_records WHERE exercise_id = :exerciseId ORDER BY achieved_at DESC")
    fun observeByExercise(exerciseId: Long): Flow<List<PersonalRecordEntity>>
}
