package com.deepreps.feature.workout.active

/**
 * One-shot side effects emitted by the workout ViewModel.
 *
 * These are consumed once by the UI layer (navigation, toasts, vibration triggers).
 * They are NOT part of the persisted state.
 */
sealed interface WorkoutSideEffect {

    /** Navigate to the workout summary screen after completion. */
    data class NavigateToSummary(val sessionId: Long) : WorkoutSideEffect

    /** Trigger a haptic feedback / vibration. */
    data object Vibrate : WorkoutSideEffect

    /** Show a transient error message (Snackbar). */
    data class ShowError(val message: String) : WorkoutSideEffect

    /** Scroll the exercise list to the given exercise index. */
    data class ScrollToExercise(val exerciseIndex: Int) : WorkoutSideEffect
}
