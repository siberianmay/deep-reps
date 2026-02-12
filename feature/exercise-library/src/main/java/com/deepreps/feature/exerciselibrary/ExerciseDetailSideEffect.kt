package com.deepreps.feature.exerciselibrary

/**
 * One-shot side effects emitted by the exercise detail ViewModel.
 */
sealed interface ExerciseDetailSideEffect {
    /** Navigate back to the previous screen. */
    data object NavigateBack : ExerciseDetailSideEffect
}
