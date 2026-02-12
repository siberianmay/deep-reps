package com.deepreps.feature.workout.active

/**
 * User intents for the active workout screen.
 *
 * Each intent maps to exactly one user action. Processed sequentially by the ViewModel
 * to guarantee predictable state transitions (MVI pattern).
 */
sealed interface WorkoutIntent {

    // --- Set operations ---

    /** User tapped "done" on a set. Writes to Room immediately. */
    data class CompleteSet(
        val workoutExerciseId: Long,
        val setId: Long,
        val setIndex: Int,
        val weight: Double,
        val reps: Int,
    ) : WorkoutIntent

    /** User changed the weight value on a set (before completing it). */
    data class UpdateSetWeight(
        val workoutExerciseId: Long,
        val setId: Long,
        val weight: Double,
    ) : WorkoutIntent

    /** User changed the reps value on a set (before completing it). */
    data class UpdateSetReps(
        val workoutExerciseId: Long,
        val setId: Long,
        val reps: Int,
    ) : WorkoutIntent

    /** User tapped "Add Set" at the bottom of an ExerciseCard. */
    data class AddSet(val workoutExerciseId: Long) : WorkoutIntent

    /** User chose to delete a set (only allowed for non-completed sets). */
    data class DeleteSet(val setId: Long, val workoutExerciseId: Long) : WorkoutIntent

    // --- Rest timer ---

    /** User tapped "Skip" on the rest timer bottom sheet. */
    data object SkipRestTimer : WorkoutIntent

    /** User tapped "+30s" on the rest timer bottom sheet. */
    data object ExtendRestTimer : WorkoutIntent

    // --- Workout lifecycle ---

    /** User tapped Pause in the toolbar. */
    data object PauseWorkout : WorkoutIntent

    /** User tapped Resume from the paused overlay. */
    data object ResumeWorkout : WorkoutIntent

    /** User tapped "End Workout" / "Finish Workout". Shows confirmation dialog. */
    data object RequestFinishWorkout : WorkoutIntent

    /** User confirmed finish in the dialog. */
    data object ConfirmFinishWorkout : WorkoutIntent

    /** User dismissed the finish confirmation dialog. */
    data object DismissFinishDialog : WorkoutIntent

    // --- Card interaction ---

    /** User toggled expand/collapse on an exercise card. */
    data class ToggleExerciseExpanded(val workoutExerciseId: Long) : WorkoutIntent

    // --- Notes ---

    /** User tapped the notes icon to show/hide the notes text field. */
    data class ToggleNotes(val workoutExerciseId: Long) : WorkoutIntent

    /** User edited the notes text for an exercise. */
    data class UpdateNotes(val workoutExerciseId: Long, val text: String) : WorkoutIntent
}
