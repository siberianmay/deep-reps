package com.deepreps.feature.workout.summary

import com.deepreps.core.domain.model.enums.RecordType
import com.deepreps.core.domain.model.enums.WeightUnit

/**
 * UI state for the workout summary bottom sheet.
 */
data class WorkoutSummaryUiState(
    val isLoading: Boolean = true,
    val durationText: String = "",
    val exerciseCount: Int = 0,
    val totalWorkingSets: Int = 0,
    val totalTonnageKg: Double = 0.0,
    val perGroupVolume: List<GroupVolumeUi> = emptyList(),
    val personalRecords: List<PersonalRecordUi> = emptyList(),
    val weightUnit: WeightUnit = WeightUnit.KG,
    val errorMessage: String? = null,
)

/**
 * UI model for a muscle group volume row in the summary.
 */
data class GroupVolumeUi(
    val groupName: String,
    val workingSets: Int,
    val tonnageKg: Double,
)

/**
 * UI model for a personal record highlight in the summary.
 */
data class PersonalRecordUi(
    val exerciseName: String,
    val weightKg: Double,
    val reps: Int,
    val recordType: RecordType,
)
