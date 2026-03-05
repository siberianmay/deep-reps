package com.deepreps.feature.progress

import com.deepreps.core.domain.model.enums.WeightUnit
import com.deepreps.core.domain.util.Estimated1rmCalculator

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
    val currentBestEstimated1rmKg: Double? = null,
    val allTimeBestEstimated1rmKg: Double? = null,
    val isBodyweightMissingProfile: Boolean = false,
    val isLoading: Boolean = true,
    val errorType: ExerciseProgressError? = null,
)

/**
 * A single data point for the weight progression chart.
 *
 * [weightKg] is the best (heaviest) weight for the exercise in this session.
 * [dateEpochMs] is the session start time.
 * [estimated1rmKg] is the best estimated 1RM from any completed set in this session.
 * [confidence] is the confidence level of the [estimated1rmKg] estimate.
 */
data class ChartDataPoint(
    val dateEpochMs: Long,
    val weightKg: Double,
    val isPersonalRecord: Boolean = false,
    val estimated1rmKg: Double? = null,
    val confidence: Estimated1rmCalculator.Confidence? = null,
)

/**
 * Typed errors for the exercise progress screen.
 */
sealed interface ExerciseProgressError {
    data object LoadFailed : ExerciseProgressError
}
