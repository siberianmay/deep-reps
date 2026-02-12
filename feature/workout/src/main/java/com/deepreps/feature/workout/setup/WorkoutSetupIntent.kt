package com.deepreps.feature.workout.setup

import com.deepreps.core.domain.model.enums.MuscleGroup

/**
 * User intents for the workout setup flow.
 */
sealed interface WorkoutSetupIntent {

    // Muscle Group Selection
    data class ToggleGroup(val group: MuscleGroup) : WorkoutSetupIntent

    // Exercise Selection (receives result from ExerciseSelectionScreen)
    data class SetExercises(val exerciseIds: Set<Long>) : WorkoutSetupIntent

    // Exercise Ordering
    data class MoveExercise(val fromIndex: Int, val toIndex: Int) : WorkoutSetupIntent

    // Generate Plan
    data object GeneratePlan : WorkoutSetupIntent

    // Template Loading
    data class LoadTemplate(val templateId: Long) : WorkoutSetupIntent

    // Error Recovery
    data object Retry : WorkoutSetupIntent

    // Clear state for fresh start
    data object Reset : WorkoutSetupIntent
}
