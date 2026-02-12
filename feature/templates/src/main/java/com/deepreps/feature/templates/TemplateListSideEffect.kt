package com.deepreps.feature.templates

/**
 * One-shot side effects emitted by the template list ViewModel.
 */
sealed interface TemplateListSideEffect {

    /** Navigate to workout setup with pre-populated exercises from a template. */
    data class NavigateToWorkoutSetup(val templateId: Long) : TemplateListSideEffect

    /** Navigate to create template screen. */
    data object NavigateToCreateTemplate : TemplateListSideEffect

    /** Navigate to edit template screen with template ID. */
    data class NavigateToEditTemplate(val templateId: Long) : TemplateListSideEffect

    /** Show a snackbar message (e.g., "Template deleted"). */
    data class ShowSnackbar(val message: String) : TemplateListSideEffect
}
