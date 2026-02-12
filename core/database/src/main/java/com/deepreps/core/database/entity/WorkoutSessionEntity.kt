package com.deepreps.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single workout session.
 *
 * Status semantics:
 * - "active"    -- workout currently in progress
 * - "paused"    -- user explicitly paused
 * - "completed" -- user finished normally
 * - "discarded" -- user explicitly chose to discard (UI confirmation required)
 * - "abandoned" -- user never returned; detected after 24-hour timeout
 * - "crashed"   -- app crashed during session; detected on next startup
 */
@Entity(
    tableName = "workout_sessions",
    indices = [
        Index("status"),
        Index("started_at"),
        Index(value = ["status", "started_at"]),
        Index("template_id"),
    ],
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** Epoch millis when the session started. */
    @ColumnInfo(name = "started_at")
    val startedAt: Long,
    /** Epoch millis when completed. Null if in progress. */
    @ColumnInfo(name = "completed_at")
    val completedAt: Long?,
    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Long?,
    @ColumnInfo(name = "paused_duration_seconds", defaultValue = "0")
    val pausedDurationSeconds: Long = 0,
    /** One of: active, paused, completed, discarded, abandoned, crashed */
    @ColumnInfo(name = "status")
    val status: String,
    @ColumnInfo(name = "notes")
    val notes: String?,
    @ColumnInfo(name = "template_id")
    val templateId: Long?,
)
