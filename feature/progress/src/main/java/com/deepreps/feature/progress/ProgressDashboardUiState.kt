package com.deepreps.feature.progress

import com.deepreps.core.domain.model.enums.MuscleGroup
import com.deepreps.core.domain.model.enums.WeightUnit

/**
 * UI state for the progress dashboard screen.
 */
@Suppress("ForbiddenPublicDataClass")
data class ProgressDashboardUiState(
    val recentSessions: List<SessionSummaryUi> = emptyList(),
    val selectedTab: DashboardTab = DashboardTab.HISTORY,
    val selectedTimeRange: TimeRange = TimeRange.TWELVE_WEEKS,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val personalRecords: Map<MuscleGroup, List<PrSummaryUi>> = emptyMap(),
    val isRecordsLoading: Boolean = false,
    val isLoading: Boolean = true,
    val errorType: ProgressDashboardError? = null,
)

/**
 * Tabs for the progress dashboard: Records (personal records) or History (session list).
 */
enum class DashboardTab { RECORDS, HISTORY }

/**
 * UI model for a personal record summary displayed in the Records tab.
 */
@Suppress("ForbiddenPublicDataClass")
data class PrSummaryUi(
    val exerciseId: Long,
    val exerciseName: String,
    val muscleGroup: MuscleGroup,
    val bestWeightKg: Double,
    val bestReps: Int?,
    val achievedAtText: String,
)

/**
 * Lightweight UI model for a session in the history list.
 * Grouped by date at the screen level.
 */
data class SessionSummaryUi(
    val sessionId: Long,
    val dateText: String,
    val durationText: String,
    val exerciseCount: Int,
    val totalVolumeKg: Double,
    val muscleGroupNames: String,
    val setCount: Int,
)

/**
 * Time range filter options for charts and history.
 */
enum class TimeRange(val label: String, val weeks: Int?) {
    FOUR_WEEKS("4W", 4),
    TWELVE_WEEKS("12W", 12),
    SIX_MONTHS("6M", 26),
    ALL("All", null),
}

/**
 * Typed errors for the progress dashboard screen.
 */
sealed interface ProgressDashboardError {
    data object LoadFailed : ProgressDashboardError
}
