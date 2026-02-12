package com.deepreps.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Historical body weight measurement.
 *
 * Weight is ALWAYS stored in kilograms. Conversion to display unit happens at the
 * repository layer.
 */
@Entity(tableName = "body_weight_entries")
@Suppress("ForbiddenPublicDataClass")
data class BodyWeightEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** Always in kg. */
    @ColumnInfo(name = "weight_value")
    val weightValue: Double,
    /** Epoch millis. */
    @ColumnInfo(name = "recorded_at")
    val recordedAt: Long,
)
