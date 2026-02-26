package com.deepreps.feature.workout.active

import com.deepreps.core.data.timer.RestTimerState
import com.deepreps.core.domain.model.Exercise
import com.deepreps.core.domain.model.WorkoutSet
import com.deepreps.core.domain.model.enums.SetStatus

/**
 * Single immutable state for the active workout screen.
 *
 * All UI rendering derives from this one object. No partial state updates.
 * Per architecture.md Section 1.3 (MVI pattern).
 */
data class WorkoutUiState(
    /** Current workout lifecycle phase. */
    val phase: WorkoutPhaseUi = WorkoutPhaseUi.Loading,
    /** Ordered list of exercises with their sets. */
    val exercises: List<WorkoutExerciseUi> = emptyList(),
    /** Active rest timer state, or null when no timer is running. */
    val activeRestTimer: RestTimerState? = null,
    /** Elapsed workout time in seconds (excludes paused duration). */
    val elapsedSeconds: Long = 0L,
    /** Whether the workout is currently paused. */
    val isPaused: Boolean = false,
    /** Session ID from Room (non-null once loaded). */
    val sessionId: Long = 0L,
    /** Whether the finish confirmation dialog is showing. */
    val showFinishDialog: Boolean = false,
    /** Set of workout exercise IDs whose notes text field is expanded. */
    val notesExpandedExerciseIds: Set<Long> = emptySet(),
    /** Active number input bottom sheet, or null when no sheet is showing. */
    val activeInputSheet: InputSheetState? = null,
    /** Exercise ID for which the info sheet is shown, or null if hidden. */
    val exerciseInfoId: Long? = null,
    /** Full exercise domain model for the info sheet, loaded from repository. */
    val exerciseInfoData: Exercise? = null,
) {
    /** Index of the first exercise that has incomplete sets, or -1 if all done. */
    val activeExerciseIndex: Int
        get() = exercises.indexOfFirst { exercise ->
            exercise.sets.any { it.status != SetStatus.COMPLETED && it.status != SetStatus.SKIPPED }
        }

    /** Whether every set of every exercise is completed. */
    val isAllCompleted: Boolean
        get() = exercises.isNotEmpty() && exercises.all { exercise ->
            exercise.sets.all { it.status == SetStatus.COMPLETED || it.status == SetStatus.SKIPPED }
        }

    /** Total completed sets across all exercises. */
    val completedSetCount: Int
        get() = exercises.sumOf { ex -> ex.sets.count { it.status == SetStatus.COMPLETED } }

    /** Total planned sets across all exercises. */
    val totalSetCount: Int
        get() = exercises.sumOf { it.sets.size }
}

/**
 * UI phase for the workout screen.
 */
sealed interface WorkoutPhaseUi {
    /** Loading session data from Room. */
    data object Loading : WorkoutPhaseUi

    /** Workout is actively in progress. */
    data object Active : WorkoutPhaseUi

    /** Workout is paused. */
    data object Paused : WorkoutPhaseUi

    /** Workout has been completed. Terminal state. */
    data class Completed(val sessionId: Long) : WorkoutPhaseUi

    /** Error loading session. */
    data class Error(val message: String) : WorkoutPhaseUi
}

/**
 * UI model for an exercise in the active workout.
 * Enriched with display information beyond what the domain model carries.
 */
data class WorkoutExerciseUi(
    /** WorkoutExercise Room PK. */
    val id: Long,
    /** Exercise library PK (for looking up metadata). */
    val exerciseId: Long,
    /** Display name of the exercise. */
    val name: String,
    /** Equipment label (e.g., "Barbell", "Dumbbell"). */
    val equipment: String,
    /** Ordered list of sets for this exercise. */
    val sets: List<WorkoutSet>,
    /** Order position in the session. */
    val orderIndex: Int,
    /** Whether this card is expanded in the UI. */
    val isExpanded: Boolean = false,
    /** Exercise-level notes from the user. */
    val notes: String? = null,
    /** Rest timer duration for this exercise (resolved by ResolveRestTimerUseCase). */
    val restTimerSeconds: Int = 120,
) {
    /** Whether all sets in this exercise are completed. */
    val isCompleted: Boolean
        get() = sets.isNotEmpty() && sets.all {
            it.status == SetStatus.COMPLETED || it.status == SetStatus.SKIPPED
        }

    /** Summary line for collapsed completed exercise: "4/4 sets". */
    val completionSummary: String
        get() {
            val completed = sets.count { it.status == SetStatus.COMPLETED }
            return "$completed/${sets.size} sets"
        }
}

/**
 * State for the number input bottom sheet.
 *
 * Determines whether the weight or reps sheet is currently visible,
 * and carries the context needed to apply the confirmed value.
 */
sealed interface InputSheetState {
    /** Weight editing sheet. */
    data class Weight(
        val workoutExerciseId: Long,
        val setId: Long,
        val currentValue: Double,
        val step: Double,
    ) : InputSheetState

    /** Reps editing sheet. */
    data class Reps(
        val workoutExerciseId: Long,
        val setId: Long,
        val currentValue: Int,
    ) : InputSheetState
}
