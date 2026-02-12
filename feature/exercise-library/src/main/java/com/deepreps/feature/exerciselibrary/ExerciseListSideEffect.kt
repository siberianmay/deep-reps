package com.deepreps.feature.exerciselibrary

/**
 * One-shot side effects emitted by the exercise list ViewModel.
 */
sealed interface ExerciseListSideEffect {
    /** Navigate to the exercise detail screen. */
    data class NavigateToDetail(val exerciseId: Long) : ExerciseListSideEffect
}
