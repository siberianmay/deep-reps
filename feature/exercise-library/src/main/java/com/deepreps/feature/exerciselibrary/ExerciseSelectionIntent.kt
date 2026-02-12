package com.deepreps.feature.exerciselibrary

import com.deepreps.core.domain.model.enums.MuscleGroup

/**
 * User intents for the exercise selection screen.
 */
sealed interface ExerciseSelectionIntent {
    /** User toggled the checkbox for an exercise. */
    data class ToggleExercise(val exerciseId: Long) : ExerciseSelectionIntent

    /** User tapped a muscle group tab. */
    data class SelectGroup(val group: MuscleGroup) : ExerciseSelectionIntent

    /** User typed in the search field. */
    data class Search(val query: String) : ExerciseSelectionIntent

    /** User cleared the search field. */
    data object ClearSearch : ExerciseSelectionIntent

    /** User tapped the confirm/generate plan button. */
    data object ConfirmSelection : ExerciseSelectionIntent

    /** User tapped an exercise row body (not checkbox) to view detail. */
    data class ViewDetail(val exerciseId: Long) : ExerciseSelectionIntent

    /** User requested a retry after an error. */
    data object Retry : ExerciseSelectionIntent
}
