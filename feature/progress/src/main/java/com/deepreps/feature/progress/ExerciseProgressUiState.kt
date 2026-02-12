package com.deepreps.feature.progress

import com.deepreps.core.domain.model.enums.WeightUnit

/**
 * UI state for the exercise progress (weight chart) screen.
 */
data class ExerciseProgressUiState(
    val exerciseName: String = "",
    val chartData: List<ChartDataPoint> = emptyList(),
    val selectedTimeRange: TimeRange = TimeRange.TWELVE_WEEKS,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val currentBestKg: Double? = null,
    val allTimeBestKg: Double? = null,
    val isLoading: Boolean = true,
    val errorType: ExerciseProgressError? = null,
)

/**
 * A single data point for the weight progression chart.
 *
 * [weightKg] is the best (heaviest) weight for the exercise in this session.
 * [dateEpochMs] is the session start time.
 */
data class ChartDataPoint(
    val dateEpochMs: Long,
    val weightKg: Double,
    val isPersonalRecord: Boolean = false,
)

/**
 * Typed errors for the exercise progress screen.
 */
sealed interface ExerciseProgressError {
    data object LoadFailed : ExerciseProgressError
}
