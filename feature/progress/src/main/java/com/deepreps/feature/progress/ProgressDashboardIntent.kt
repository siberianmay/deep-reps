package com.deepreps.feature.progress

/**
 * User intents for the progress dashboard screen.
 */
sealed interface ProgressDashboardIntent {

    /** User selected a time range filter. */
    data class SelectTimeRange(val timeRange: TimeRange) : ProgressDashboardIntent

    /** User tapped a session to view its detail. */
    data class ViewSession(val sessionId: Long) : ProgressDashboardIntent

    /** User tapped an exercise to view its progress. */
    data class ViewExerciseProgress(val exerciseId: Long) : ProgressDashboardIntent

    /** User requested a retry after an error. */
    data object Retry : ProgressDashboardIntent
}
