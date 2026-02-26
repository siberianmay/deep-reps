package com.deepreps.feature.exerciselibrary

import com.deepreps.core.domain.model.enums.MuscleGroup

/**
 * UI state for the exercise selection (multi-select picker) screen.
 */
@Suppress("ForbiddenPublicDataClass")
data class ExerciseSelectionUiState(
    val allowedGroups: Set<MuscleGroup> = MuscleGroup.entries.toSet(),
    val activeGroup: MuscleGroup = MuscleGroup.CHEST,
    val exercises: List<ExerciseUi> = emptyList(),
    val selectedExerciseIds: Set<Long> = emptySet(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val errorType: ExerciseSelectionError? = null,
) {
    /** Derived: count of currently selected exercises. */
    val selectedCount: Int get() = selectedExerciseIds.size
}

/**
 * Typed errors for the exercise selection screen.
 */
sealed interface ExerciseSelectionError {
    data object LoadFailed : ExerciseSelectionError
}
