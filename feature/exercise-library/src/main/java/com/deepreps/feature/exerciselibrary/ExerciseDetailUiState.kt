package com.deepreps.feature.exerciselibrary

import com.deepreps.core.domain.model.enums.Difficulty
import com.deepreps.core.domain.model.enums.Equipment
import com.deepreps.core.domain.model.enums.MovementType

/**
 * UI state for the exercise detail screen / bottom sheet.
 */
data class ExerciseDetailUiState(
    val isLoading: Boolean = true,
    val exercise: ExerciseDetailUi? = null,
    val errorType: ExerciseDetailError? = null,
)

/**
 * Full detail UI model for a single exercise.
 * Includes all fields needed by the detail card per design-system.md Section 4.5.
 */
data class ExerciseDetailUi(
    val id: Long,
    val name: String,
    val description: String,
    val equipment: Equipment,
    val movementType: MovementType,
    val difficulty: Difficulty,
    val primaryGroupId: Long,
    val secondaryMuscles: List<String>,
    val tips: List<String>,
    val pros: List<String>,
)

/**
 * Typed errors for the exercise detail screen.
 */
sealed interface ExerciseDetailError {
    data object NotFound : ExerciseDetailError
    data object LoadFailed : ExerciseDetailError
}
