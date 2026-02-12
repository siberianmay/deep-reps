package com.deepreps.feature.templates

/**
 * User intents for the template creation/edit screen.
 */
sealed interface CreateTemplateIntent {

    /** User typed in the name field. */
    data class UpdateName(val name: String) : CreateTemplateIntent

    /** User tapped save. */
    data object Save : CreateTemplateIntent

    /** User removed an exercise from the list. */
    data class RemoveExercise(val exerciseId: Long) : CreateTemplateIntent

    /** User reordered exercises via drag-and-drop. */
    data class MoveExercise(val fromIndex: Int, val toIndex: Int) : CreateTemplateIntent

    /** User tapped close/back. */
    data object Close : CreateTemplateIntent
}
