package com.deepreps.feature.exerciselibrary

import com.deepreps.core.domain.model.enums.MuscleGroup

/**
 * User intents for the exercise list screen.
 */
sealed interface ExerciseListIntent {
    /** User tapped a muscle group tab/chip. */
    data class SelectGroup(val group: MuscleGroup) : ExerciseListIntent

    /** User typed in the search field. */
    data class Search(val query: String) : ExerciseListIntent

    /** User cleared the search field. */
    data object ClearSearch : ExerciseListIntent

    /** User tapped an exercise row to view its detail. */
    data class NavigateToDetail(val exerciseId: Long) : ExerciseListIntent

    /** User requested a retry after an error. */
    data object Retry : ExerciseListIntent
}
