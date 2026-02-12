package com.deepreps.feature.templates

/**
 * One-shot side effects emitted by the create/edit template ViewModel.
 */
sealed interface CreateTemplateSideEffect {

    /** Template saved successfully; navigate back. */
    data class TemplateSaved(val message: String) : CreateTemplateSideEffect

    /** Show error message as snackbar. */
    data class ShowError(val message: String) : CreateTemplateSideEffect

    /** Navigate back (close without saving). */
    data object NavigateBack : CreateTemplateSideEffect
}
