package com.deepreps.feature.progress

/**
 * One-shot side effects emitted by the progress dashboard ViewModel.
 */
sealed interface ProgressDashboardSideEffect {

    /** Navigate to session detail screen. */
    data class NavigateToSessionDetail(val sessionId: Long) : ProgressDashboardSideEffect

    /** Navigate to exercise progress screen. */
    data class NavigateToExerciseProgress(val exerciseId: Long) : ProgressDashboardSideEffect
}
