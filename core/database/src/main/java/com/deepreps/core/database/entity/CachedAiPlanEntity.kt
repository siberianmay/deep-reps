package com.deepreps.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cached AI-generated workout plan for offline fallback.
 *
 * Keyed by SHA-256 hash of sorted exercise IDs + experience level.
 * Plans older than 7 days are eligible for cleanup.
 */
@Entity(
    tableName = "cached_ai_plans",
    indices = [
        Index("exercise_ids_hash"),
    ],
)
@Suppress("ForbiddenPublicDataClass")
data class CachedAiPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** SHA-256 hash of sorted exercise IDs. */
    @ColumnInfo(name = "exercise_ids_hash")
    val exerciseIdsHash: String,
    /** Full JSON of the generated plan. */
    @ColumnInfo(name = "plan_json")
    val planJson: String,
    /** Epoch millis when cached. */
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    /** 1, 2, or 3. */
    @ColumnInfo(name = "experience_level")
    val experienceLevel: Int,
)
