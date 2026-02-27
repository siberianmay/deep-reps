package com.deepreps.feature.workout.setup

import com.deepreps.core.domain.model.enums.MuscleGroup

/**
 * UI state for the multi-step workout setup flow.
 *
 * Flow: MuscleGroupSelector -> ExercisePicker -> ExerciseOrder -> PlanReview
 * Alternative: TemplateList -> ExerciseOrder -> PlanReview
 */
data class WorkoutSetupUiState(
    val selectedGroups: Set<MuscleGroup> = emptySet(),
    val selectedExercises: List<ExerciseOrderItem> = emptyList(),
    val isFromTemplate: Boolean = false,
    val templateName: String? = null,
    val isGeneratingPlan: Boolean = false,
    val error: WorkoutSetupError? = null,
) {
    val selectedGroupCount: Int get() = selectedGroups.size
    val canProceedFromGroups: Boolean get() = selectedGroups.isNotEmpty()
    val hasExercises: Boolean get() = selectedExercises.isNotEmpty()
}

/**
 * Lightweight model for an exercise in the ordering list.
 */
data class ExerciseOrderItem(
    val exerciseId: Long,
    val name: String,
    val equipment: String,
    val difficulty: String,
    val primaryGroupId: Long,
    val orderIndex: Int,
)

/**
 * Typed errors for the workout setup flow.
 */
sealed interface WorkoutSetupError {
    data object ExerciseLoadFailed : WorkoutSetupError
    data object PlanGenerationFailed : WorkoutSetupError
}
