package com.deepreps.feature.exerciselibrary

import com.deepreps.core.domain.model.enums.Difficulty
import com.deepreps.core.domain.model.enums.Equipment
import com.deepreps.core.domain.model.enums.MuscleGroup
import com.deepreps.core.domain.model.enums.MovementType

/**
 * UI state for the exercise list (browse by muscle group) screen.
 */
data class ExerciseListUiState(
    val selectedGroup: MuscleGroup = MuscleGroup.CHEST,
    val exercises: List<ExerciseUi> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val errorType: ExerciseListError? = null,
)

/**
 * Lightweight UI model for an exercise row in a list.
 * Mapped from [com.deepreps.core.domain.model.Exercise] at the presentation layer.
 */
data class ExerciseUi(
    val id: Long,
    val name: String,
    val equipment: Equipment,
    val movementType: MovementType,
    val difficulty: Difficulty,
    val primaryGroupId: Long,
)

/**
 * Typed errors for the exercise list screen.
 * ViewModel emits these; the composable maps to string resources.
 */
sealed interface ExerciseListError {
    data object LoadFailed : ExerciseListError
}
