package com.deepreps.core.database.entity

import androidx.room.ColumnInfo

/**
 * Projection for a template with its exercise count.
 * Used by Room queries that aggregate exercise counts per template.
 */
data class TemplateWithExerciseCountProjection(
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "muscle_groups_json")
    val muscleGroupsJson: String,
    @ColumnInfo(name = "exercise_count")
    val exerciseCount: Int,
)
