package com.deepreps.core.domain.statemachine

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WorkoutStateMachineTest {

    private lateinit var stateMachine: WorkoutStateMachine

    @BeforeEach
    fun setup() {
        stateMachine = WorkoutStateMachine()
    }

    @Nested
    @DisplayName("Idle state")
    inner class IdleState {

        @Test
        fun `SelectExercises transitions to Setup`() {
            val result = stateMachine.transition(
                WorkoutPhase.Idle,
                WorkoutEvent.SelectExercises(listOf(1L, 2L, 3L)),
            )
            assertThat(result).isInstanceOf(WorkoutPhase.Setup::class.java)
            assertThat((result as WorkoutPhase.Setup).exerciseIds).containsExactly(1L, 2L, 3L)
        }

        @Test
        fun `PauseWorkout from Idle returns null`() {
            val result = stateMachine.transition(
                WorkoutPhase.Idle,
                WorkoutEvent.PauseWorkout(pausedAtMillis = 1000L),
            )
            assertThat(result).isNull()
        }

        @Test
        fun `FinishWorkout from Idle returns null`() {
            val result = stateMachine.transition(
                WorkoutPhase.Idle,
                WorkoutEvent.FinishWorkout(sessionId = 1L),
            )
            assertThat(result).isNull()
        }

        @Test
        fun `ResumeWorkout from Idle returns null`() {
            val result = stateMachine.transition(
                WorkoutPhase.Idle,
                WorkoutEvent.ResumeWorkout(resumedAtMillis = 1000L),
            )
            assertThat(result).isNull()
        }
    }

    @Nested
    @DisplayName("Setup state")
    inner class SetupState {

        private val setup = WorkoutPhase.Setup(exerciseIds = listOf(1L, 2L))

        @Test
        fun `RequestPlanGeneration transitions to GeneratingPlan`() {
            val result = stateMachine.transition(setup, WorkoutEvent.RequestPlanGeneration)
            assertThat(result).isEqualTo(WorkoutPhase.GeneratingPlan)
        }

        @Test
        fun `StartWithoutPlan transitions to Active`() {
            val result = stateMachine.transition(
                setup,
                WorkoutEvent.StartWithoutPlan(startedAtMillis = 5000L),
            )
            assertThat(result).isInstanceOf(WorkoutPhase.Active::class.java)
            assertThat((result as WorkoutPhase.Active).startedAtMillis).isEqualTo(5000L)
        }

        @Test
        fun `PauseWorkout from Setup returns null`() {
            val result = stateMachine.transition(
                setup,
                WorkoutEvent.PauseWorkout(pausedAtMillis = 1000L),
            )
            assertThat(result).isNull()
        }
    }

    @Nested
    @DisplayName("GeneratingPlan state")
    inner class GeneratingPlanState {

        @Test
        fun `PlanReceived transitions to Active`() {
            val result = stateMachine.transition(
                WorkoutPhase.GeneratingPlan,
                WorkoutEvent.PlanReceived(startedAtMillis = 10000L),
            )
            assertThat(result).isInstanceOf(WorkoutPhase.Active::class.java)
            assertThat((result as WorkoutPhase.Active).startedAtMillis).isEqualTo(10000L)
        }

        @Test
        fun `PlanFailed transitions to Active (fallback path)`() {
            val result = stateMachine.transition(
                WorkoutPhase.GeneratingPlan,
                WorkoutEvent.PlanFailed(startedAtMillis = 10000L),
            )
            assertThat(result).isInstanceOf(WorkoutPhase.Active::class.java)
        }

        @Test
        fun `SelectExercises from GeneratingPlan returns null`() {
            val result = stateMachine.transition(
                WorkoutPhase.GeneratingPlan,
                WorkoutEvent.SelectExercises(listOf(1L)),
            )
            assertThat(result).isNull()
        }
    }

    @Nested
    @DisplayName("Active state")
    inner class ActiveState {

        private val active = WorkoutPhase.Active(startedAtMillis = 10000L)

        @Test
        fun `PauseWorkout transitions to Paused`() {
            val result = stateMachine.transition(
                active,
                WorkoutEvent.PauseWorkout(pausedAtMillis = 20000L),
            )
            assertThat(result).isInstanceOf(WorkoutPhase.Paused::class.java)
            assertThat((result as WorkoutPhase.Paused).pausedAtMillis).isEqualTo(20000L)
        }

        @Test
        fun `FinishWorkout transitions to Completed`() {
            val result = stateMachine.transition(
                active,
                WorkoutEvent.FinishWorkout(sessionId = 42L),
            )
            assertThat(result).isInstanceOf(WorkoutPhase.Completed::class.java)
            assertThat((result as WorkoutPhase.Completed).sessionId).isEqualTo(42L)
        }

        @Test
        fun `DiscardWorkout transitions to Idle`() {
            val result = stateMachine.transition(active, WorkoutEvent.DiscardWorkout)
            assertThat(result).isEqualTo(WorkoutPhase.Idle)
        }

        @Test
        fun `SelectExercises from Active returns null`() {
            val result = stateMachine.transition(
                active,
                WorkoutEvent.SelectExercises(listOf(1L)),
            )
            assertThat(result).isNull()
        }

        @Test
        fun `ResumeWorkout from Active returns null`() {
            val result = stateMachine.transition(
                active,
                WorkoutEvent.ResumeWorkout(resumedAtMillis = 20000L),
            )
            assertThat(result).isNull()
        }
    }

    @Nested
    @DisplayName("Paused state")
    inner class PausedState {

        private val paused = WorkoutPhase.Paused(
            pausedAtMillis = 20000L,
            startedAtMillis = 10000L,
            accumulatedPauseSeconds = 5L,
        )

        @Test
        fun `ResumeWorkout transitions to Active with accumulated pause time`() {
            val result = stateMachine.transition(
                paused,
                WorkoutEvent.ResumeWorkout(resumedAtMillis = 30000L),
            )
            assertThat(result).isInstanceOf(WorkoutPhase.Active::class.java)
            val activeResult = result as WorkoutPhase.Active
            // Previous 5s + (30000 - 20000) / 1000 = 5 + 10 = 15s
            assertThat(activeResult.accumulatedPauseSeconds).isEqualTo(15L)
            assertThat(activeResult.startedAtMillis).isEqualTo(10000L)
        }

        @Test
        fun `DiscardWorkout from Paused transitions to Idle`() {
            val result = stateMachine.transition(paused, WorkoutEvent.DiscardWorkout)
            assertThat(result).isEqualTo(WorkoutPhase.Idle)
        }

        @Test
        fun `FinishWorkout from Paused returns null`() {
            val result = stateMachine.transition(
                paused,
                WorkoutEvent.FinishWorkout(sessionId = 1L),
            )
            assertThat(result).isNull()
        }

        @Test
        fun `PauseWorkout from Paused returns null`() {
            val result = stateMachine.transition(
                paused,
                WorkoutEvent.PauseWorkout(pausedAtMillis = 25000L),
            )
            assertThat(result).isNull()
        }
    }

    @Nested
    @DisplayName("Completed state (terminal)")
    inner class CompletedState {

        private val completed = WorkoutPhase.Completed(sessionId = 42L)

        @Test
        fun `SelectExercises from Completed returns null`() {
            val result = stateMachine.transition(
                completed,
                WorkoutEvent.SelectExercises(listOf(1L)),
            )
            assertThat(result).isNull()
        }

        @Test
        fun `ResumeWorkout from Completed returns null`() {
            val result = stateMachine.transition(
                completed,
                WorkoutEvent.ResumeWorkout(resumedAtMillis = 50000L),
            )
            assertThat(result).isNull()
        }

        @Test
        fun `PauseWorkout from Completed returns null`() {
            val result = stateMachine.transition(
                completed,
                WorkoutEvent.PauseWorkout(pausedAtMillis = 50000L),
            )
            assertThat(result).isNull()
        }

        @Test
        fun `FinishWorkout from Completed returns null`() {
            val result = stateMachine.transition(
                completed,
                WorkoutEvent.FinishWorkout(sessionId = 99L),
            )
            assertThat(result).isNull()
        }
    }
}
