package com.deepreps.feature.progress

/**
 * User intents for the exercise progress screen.
 */
sealed interface ExerciseProgressIntent {

    /** User selected a different time range filter. */
    data class SelectTimeRange(val timeRange: TimeRange) : ExerciseProgressIntent

    /** User requested a retry after an error. */
    data object Retry : ExerciseProgressIntent
}
