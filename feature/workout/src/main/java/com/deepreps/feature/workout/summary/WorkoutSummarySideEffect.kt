package com.deepreps.feature.workout.summary

/**
 * One-shot side effects emitted by the workout summary ViewModel.
 */
sealed interface WorkoutSummarySideEffect {

    /** Navigate back to the home/dashboard screen. */
    data object NavigateToHome : WorkoutSummarySideEffect

    /** Navigate to create template screen with pre-populated exercise IDs. */
    data class NavigateToCreateTemplate(val exerciseIds: List<Long>) : WorkoutSummarySideEffect
}
