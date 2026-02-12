package com.deepreps.feature.aiplan

/**
 * MVI side effects for the Plan Review screen.
 *
 * One-shot events consumed by the UI (navigation, toasts).
 * Emitted via a Channel, not baked into the state.
 */
sealed interface PlanReviewSideEffect {

    /** Navigate to the active workout screen with the confirmed plan. */
    data class NavigateToWorkout(val sessionId: Long) : PlanReviewSideEffect

    /** Show an error toast/snackbar. */
    data class ShowError(val message: String) : PlanReviewSideEffect
}
