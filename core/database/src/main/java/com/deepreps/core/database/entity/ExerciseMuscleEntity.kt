package com.deepreps.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Junction table linking exercises to muscle groups.
 *
 * Each exercise has exactly one primary muscle group ([isPrimary] = true)
 * and zero or more secondary groups ([isPrimary] = false).
 */
@Entity(
    tableName = "exercise_muscles",
    primaryKeys = ["exercise_id", "muscle_group_id"],
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = MuscleGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["muscle_group_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("exercise_id"),
        Index("muscle_group_id"),
    ],
)
@Suppress("ForbiddenPublicDataClass")
data class ExerciseMuscleEntity(
    @ColumnInfo(name = "exercise_id")
    val exerciseId: Long,
    @ColumnInfo(name = "muscle_group_id")
    val muscleGroupId: Long,
    @ColumnInfo(name = "is_primary")
    val isPrimary: Boolean,
)
