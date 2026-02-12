package com.deepreps.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.deepreps.core.database.entity.CachedAiPlanEntity

@Dao
interface CachedAiPlanDao {

    @Insert
    suspend fun insert(plan: CachedAiPlanEntity): Long

    @Query(
        """
        SELECT * FROM cached_ai_plans
        WHERE exercise_ids_hash = :hash AND experience_level = :experienceLevel
        ORDER BY created_at DESC LIMIT 1
        """
    )
    suspend fun getByHash(hash: String, experienceLevel: Int): CachedAiPlanEntity?

    /** Delete cached plans created before the given epoch millis. */
    @Query("DELETE FROM cached_ai_plans WHERE created_at < :before")
    suspend fun deleteExpired(before: Long)
}
