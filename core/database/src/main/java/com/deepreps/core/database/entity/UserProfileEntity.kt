package com.deepreps.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Singleton user profile row. PK is always 1.
 *
 * Experience level: 1=beginner, 2=intermediate, 3=advanced.
 * All height/weight values stored in metric (cm, kg).
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Long = 1,
    /** 1=beginner, 2=intermediate, 3=advanced */
    @ColumnInfo(name = "experience_level")
    val experienceLevel: Int,
    /** "kg" or "lbs" */
    @ColumnInfo(name = "preferred_unit")
    val preferredUnit: String,
    @ColumnInfo(name = "age")
    val age: Int?,
    @ColumnInfo(name = "height_cm")
    val heightCm: Double?,
    /** "male", "female", or null */
    @ColumnInfo(name = "gender")
    val gender: String?,
    /** Body weight in kg. */
    @ColumnInfo(name = "body_weight_kg")
    val bodyWeightKg: Double?,
    /** Epoch millis. */
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    /** Epoch millis. */
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
