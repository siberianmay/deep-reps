package com.deepreps.feature.workout.summary

/**
 * User intents for the workout summary bottom sheet.
 */
sealed interface WorkoutSummaryIntent {

    /** User tapped "Done" / dismiss the summary sheet. */
    data object Dismiss : WorkoutSummaryIntent

    /** User tapped "Save as Template" to create a template from this workout. */
    data object SaveAsTemplate : WorkoutSummaryIntent

    /** User tapped retry after an error. */
    data object Retry : WorkoutSummaryIntent
}
