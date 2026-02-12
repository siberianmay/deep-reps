package com.deepreps.feature.exerciselibrary

/**
 * One-shot side effects emitted by the exercise selection ViewModel.
 */
sealed interface ExerciseSelectionSideEffect {
    /** User confirmed selection. Carries the set of selected exercise IDs. */
    data class SelectionConfirmed(val exerciseIds: Set<Long>) : ExerciseSelectionSideEffect

    /** Navigate to exercise detail (bottom sheet). */
    data class NavigateToDetail(val exerciseId: Long) : ExerciseSelectionSideEffect
}
