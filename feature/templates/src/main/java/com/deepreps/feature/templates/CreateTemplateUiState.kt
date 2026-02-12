package com.deepreps.feature.templates

/**
 * UI state for the template creation/edit screen.
 */
data class CreateTemplateUiState(
    val templateId: Long? = null,
    val name: String = "",
    val exercises: List<TemplateExerciseUi> = emptyList(),
    val muscleGroupNames: List<String> = emptyList(),
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val nameError: String? = null,
    val exerciseError: String? = null,
) {
    val canSave: Boolean
        get() = name.trim().isNotEmpty() &&
            name.trim().length <= MAX_NAME_LENGTH &&
            exercises.isNotEmpty() &&
            exercises.size <= MAX_EXERCISES &&
            !isSaving

    companion object {
        const val MAX_NAME_LENGTH = 60
        const val MAX_EXERCISES = 15
    }
}

/**
 * Lightweight model for an exercise row in the template editor.
 */
data class TemplateExerciseUi(
    val exerciseId: Long,
    val name: String,
    val orderIndex: Int,
)
