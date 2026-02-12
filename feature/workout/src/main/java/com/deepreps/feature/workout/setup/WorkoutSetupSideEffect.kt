package com.deepreps.feature.workout.setup

/**
 * One-shot side effects emitted by the workout setup ViewModel.
 */
sealed interface WorkoutSetupSideEffect {

    /** Navigate to exercise selection with the selected muscle group IDs. */
    data class NavigateToExerciseSelection(val groupIds: List<Long>) : WorkoutSetupSideEffect

    /** Navigate to plan review screen after plan generation or ordering. */
    data class NavigateToPlanReview(val exerciseIds: List<Long>) : WorkoutSetupSideEffect

    /** Navigate to exercise order screen. */
    data object NavigateToExerciseOrder : WorkoutSetupSideEffect

    /** Show error message. */
    data class ShowError(val message: String) : WorkoutSetupSideEffect
}
