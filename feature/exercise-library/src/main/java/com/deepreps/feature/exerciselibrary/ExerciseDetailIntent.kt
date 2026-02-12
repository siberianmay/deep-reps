package com.deepreps.feature.exerciselibrary

/**
 * User intents for the exercise detail screen.
 */
sealed interface ExerciseDetailIntent {
    /** User requested a retry after an error. */
    data object Retry : ExerciseDetailIntent
}
