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
@Suppress("ForbiddenPublicDataClass")
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
    /** Minimum reps for compound exercises. */
    @ColumnInfo(name = "compound_rep_min", defaultValue = "6")
    val compoundRepMin: Int = 6,
    /** Maximum reps for compound exercises. */
    @ColumnInfo(name = "compound_rep_max", defaultValue = "10")
    val compoundRepMax: Int = 10,
    /** Minimum reps for isolation exercises. */
    @ColumnInfo(name = "isolation_rep_min", defaultValue = "10")
    val isolationRepMin: Int = 10,
    /** Maximum reps for isolation exercises. */
    @ColumnInfo(name = "isolation_rep_max", defaultValue = "15")
    val isolationRepMax: Int = 15,
    /** 0 = use experience-level default. 1-10 = override working sets per exercise. */
    @ColumnInfo(name = "default_working_sets", defaultValue = "0")
    val defaultWorkingSets: Int = 0,
    /** Epoch millis. */
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    /** Epoch millis. */
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
