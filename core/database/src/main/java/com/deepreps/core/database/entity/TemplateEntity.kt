package com.deepreps.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A saved workout template. Stores muscle group IDs as a JSON array string.
 */
@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "name")
    val name: String,
    /** Epoch millis. */
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    /** Epoch millis. */
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    /** JSON array of muscle group IDs. */
    @ColumnInfo(name = "muscle_groups_json")
    val muscleGroupsJson: String,
)
