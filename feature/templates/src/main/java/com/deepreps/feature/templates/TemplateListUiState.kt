package com.deepreps.feature.templates

/**
 * UI state for the template list (template manager) screen.
 */
data class TemplateListUiState(
    val templates: List<TemplateUi> = emptyList(),
    val isLoading: Boolean = true,
    val errorType: TemplateListError? = null,
    val showDeleteConfirmation: DeleteConfirmation? = null,
)

/**
 * Lightweight UI model for a template card in the list.
 * Mapped from [com.deepreps.core.domain.model.Template] at the presentation layer.
 */
data class TemplateUi(
    val id: Long,
    val name: String,
    val muscleGroupNames: List<String>,
    val exerciseCount: Int,
    val exercisePreview: String,
    val lastUsedText: String,
)

/**
 * State for the delete confirmation dialog.
 */
data class DeleteConfirmation(
    val templateId: Long,
    val templateName: String,
)

/**
 * Typed errors for the template list screen.
 */
sealed interface TemplateListError {
    data object LoadFailed : TemplateListError
    data object DeleteFailed : TemplateListError
}
