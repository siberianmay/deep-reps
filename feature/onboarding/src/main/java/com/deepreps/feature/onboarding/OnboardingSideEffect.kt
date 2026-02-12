package com.deepreps.feature.onboarding

/**
 * One-shot side effects emitted by the onboarding ViewModel.
 */
sealed interface OnboardingSideEffect {

    /** Onboarding completed successfully. Navigate to the main app. */
    data object NavigateToMain : OnboardingSideEffect

    /** Show an error message. */
    data class ShowError(val message: String) : OnboardingSideEffect
}
