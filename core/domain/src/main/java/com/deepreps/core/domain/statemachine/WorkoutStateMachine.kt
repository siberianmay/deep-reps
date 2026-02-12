package com.deepreps.core.domain.statemachine

/**
 * Pure Kotlin state machine for the active workout lifecycle.
 *
 * States: Idle -> Setup -> GeneratingPlan -> Active -> Paused -> Completed
 * Valid transitions are enforced. Invalid transitions return null (silently ignored by ViewModel).
 * This class has zero Android dependencies and is fully unit-testable.
 *
 * Per architecture.md Section 5.1.
 */
class WorkoutStateMachine {

    /**
     * Attempts a state transition.
     *
     * @return the new [WorkoutPhase] if the transition is valid, or null if
     *   the [event] is not allowed from the [currentPhase].
     */
    fun transition(currentPhase: WorkoutPhase, event: WorkoutEvent): WorkoutPhase? =
        when (currentPhase) {
            is WorkoutPhase.Idle -> when (event) {
                is WorkoutEvent.SelectExercises -> WorkoutPhase.Setup(event.exerciseIds)
                else -> null
            }

            is WorkoutPhase.Setup -> when (event) {
                is WorkoutEvent.RequestPlanGeneration -> WorkoutPhase.GeneratingPlan
                is WorkoutEvent.StartWithoutPlan -> WorkoutPhase.Active(
                    startedAtMillis = event.startedAtMillis,
                )
                else -> null
            }

            is WorkoutPhase.GeneratingPlan -> when (event) {
                is WorkoutEvent.PlanReceived -> WorkoutPhase.Active(
                    startedAtMillis = event.startedAtMillis,
                )
                is WorkoutEvent.PlanFailed -> WorkoutPhase.Active(
                    startedAtMillis = event.startedAtMillis,
                )
                else -> null
            }

            is WorkoutPhase.Active -> when (event) {
                is WorkoutEvent.PauseWorkout -> WorkoutPhase.Paused(
                    pausedAtMillis = event.pausedAtMillis,
                    accumulatedPauseSeconds = currentPhase.accumulatedPauseSeconds,
                )
                is WorkoutEvent.FinishWorkout -> WorkoutPhase.Completed(
                    sessionId = event.sessionId,
                )
                is WorkoutEvent.DiscardWorkout -> WorkoutPhase.Idle
                else -> null
            }

            is WorkoutPhase.Paused -> when (event) {
                is WorkoutEvent.ResumeWorkout -> {
                    val additionalPause = (event.resumedAtMillis - currentPhase.pausedAtMillis) / 1000
                    WorkoutPhase.Active(
                        startedAtMillis = currentPhase.startedAtMillis,
                        accumulatedPauseSeconds = currentPhase.accumulatedPauseSeconds + additionalPause,
                    )
                }
                is WorkoutEvent.DiscardWorkout -> WorkoutPhase.Idle
                else -> null
            }

            is WorkoutPhase.Completed -> null // terminal state
        }
}

/**
 * Workout lifecycle states.
 *
 * Modeled as a sealed interface per architecture.md Section 5.1.
 */
sealed interface WorkoutPhase {

    /** No active workout. Initial state. */
    data object Idle : WorkoutPhase

    /** Exercises selected, ready for plan generation or manual start. */
    data class Setup(val exerciseIds: List<Long>) : WorkoutPhase

    /** AI plan generation in flight. */
    data object GeneratingPlan : WorkoutPhase

    /** Workout in progress. Timer running. */
    data class Active(
        val startedAtMillis: Long = 0L,
        val accumulatedPauseSeconds: Long = 0L,
    ) : WorkoutPhase

    /** User explicitly paused the workout. */
    data class Paused(
        val pausedAtMillis: Long,
        val startedAtMillis: Long = 0L,
        val accumulatedPauseSeconds: Long = 0L,
    ) : WorkoutPhase

    /** Workout finished. Terminal state. */
    data class Completed(val sessionId: Long) : WorkoutPhase
}

/**
 * Events that trigger state transitions in the workout state machine.
 */
sealed interface WorkoutEvent {
    data class SelectExercises(val exerciseIds: List<Long>) : WorkoutEvent
    data object RequestPlanGeneration : WorkoutEvent
    data class StartWithoutPlan(val startedAtMillis: Long) : WorkoutEvent
    data class PlanReceived(val startedAtMillis: Long) : WorkoutEvent
    data class PlanFailed(val startedAtMillis: Long) : WorkoutEvent
    data class PauseWorkout(val pausedAtMillis: Long) : WorkoutEvent
    data class ResumeWorkout(val resumedAtMillis: Long) : WorkoutEvent
    data class FinishWorkout(val sessionId: Long) : WorkoutEvent
    data object DiscardWorkout : WorkoutEvent
}
