package com.deepreps.feature.workout.active

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.deepreps.core.data.timer.RestTimerManager
import com.deepreps.core.data.timer.RestTimerState
import com.deepreps.core.domain.model.Exercise
import com.deepreps.core.domain.model.WorkoutExercise
import com.deepreps.core.domain.model.WorkoutSession
import com.deepreps.core.domain.model.WorkoutSet
import com.deepreps.core.domain.model.enums.Difficulty
import com.deepreps.core.domain.model.enums.Equipment
import com.deepreps.core.domain.model.enums.MovementType
import com.deepreps.core.domain.model.enums.SessionStatus
import com.deepreps.core.domain.model.enums.SetStatus
import com.deepreps.core.domain.model.enums.SetType
import com.deepreps.core.domain.repository.ExerciseRepository
import com.deepreps.core.domain.repository.WorkoutSessionRepository
import com.deepreps.core.domain.statemachine.WorkoutStateMachine
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Tests for crash recovery and process death survival in WorkoutViewModel.
 *
 * Scenarios covered:
 * - Process death with sessionId in SavedStateHandle -> full state restoration from Room
 * - No saved session -> error state (no active session)
 * - SavedStateHandle has sessionId but session deleted from Room -> error state
 * - Paused session restored correctly with paused state
 * - Elapsed time calculation after process death
 * - Partially completed exercises restored with correct set statuses
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CrashRecoveryTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var workoutSessionRepository: WorkoutSessionRepository
    private lateinit var exerciseRepository: ExerciseRepository
    private lateinit var restTimerManager: RestTimerManager
    private lateinit var stateMachine: WorkoutStateMachine

    private val restTimerStateFlow = MutableStateFlow(RestTimerState.IDLE)

    // --- Test fixtures ---

    private val fiveMinutesAgo = System.currentTimeMillis() - 300_000L

    private val activeSession = WorkoutSession(
        id = 42L,
        startedAt = fiveMinutesAgo,
        completedAt = null,
        durationSeconds = null,
        pausedDurationSeconds = 0L,
        status = SessionStatus.ACTIVE,
        notes = null,
        templateId = null,
    )

    private val pausedSession = WorkoutSession(
        id = 42L,
        startedAt = fiveMinutesAgo,
        completedAt = null,
        durationSeconds = null,
        pausedDurationSeconds = 60L, // 1 min paused
        status = SessionStatus.PAUSED,
        notes = null,
        templateId = null,
    )

    private val exercises = listOf(
        WorkoutExercise(
            id = 100L, sessionId = 42L, exerciseId = 1L,
            orderIndex = 0, supersetGroupId = null, restTimerSeconds = 120, notes = null,
        ),
        WorkoutExercise(
            id = 101L, sessionId = 42L, exerciseId = 2L,
            orderIndex = 1, supersetGroupId = null, restTimerSeconds = 90, notes = null,
        ),
    )

    private val setsForExercise1 = listOf(
        WorkoutSet(
            id = 1L, setNumber = 1, type = SetType.WORKING, status = SetStatus.COMPLETED,
            plannedWeightKg = 100.0, plannedReps = 5, actualWeightKg = 100.0, actualReps = 5,
            completedAt = fiveMinutesAgo + 60_000,
        ),
        WorkoutSet(
            id = 2L, setNumber = 2, type = SetType.WORKING, status = SetStatus.COMPLETED,
            plannedWeightKg = 100.0, plannedReps = 5, actualWeightKg = 100.0, actualReps = 5,
            completedAt = fiveMinutesAgo + 180_000,
        ),
        WorkoutSet(
            id = 3L, setNumber = 3, type = SetType.WORKING, status = SetStatus.PLANNED,
            plannedWeightKg = 100.0, plannedReps = 5, actualWeightKg = null, actualReps = null,
        ),
    )

    private val setsForExercise2 = listOf(
        WorkoutSet(
            id = 4L, setNumber = 1, type = SetType.WORKING, status = SetStatus.PLANNED,
            plannedWeightKg = 40.0, plannedReps = 12, actualWeightKg = null, actualReps = null,
        ),
        WorkoutSet(
            id = 5L, setNumber = 2, type = SetType.WORKING, status = SetStatus.PLANNED,
            plannedWeightKg = 40.0, plannedReps = 12, actualWeightKg = null, actualReps = null,
        ),
    )

    private val exerciseLibrary1 = Exercise(
        id = 1L, stableId = "chest_barbell_bench_press", name = "Bench Press",
        description = "Barbell bench press", equipment = Equipment.BARBELL,
        movementType = MovementType.COMPOUND, difficulty = Difficulty.INTERMEDIATE,
        primaryGroupId = 3L, secondaryMuscles = emptyList(), tips = emptyList(),
        pros = emptyList(), displayOrder = 1, orderPriority = 1,
        supersetTags = emptyList(), autoProgramMinLevel = 1,
    )

    private val exerciseLibrary2 = Exercise(
        id = 2L, stableId = "arms_dumbbell_curl", name = "Dumbbell Curl",
        description = "Bicep curl", equipment = Equipment.DUMBBELL,
        movementType = MovementType.ISOLATION, difficulty = Difficulty.BEGINNER,
        primaryGroupId = 6L, secondaryMuscles = emptyList(), tips = emptyList(),
        pros = emptyList(), displayOrder = 2, orderPriority = 2,
        supersetTags = emptyList(), autoProgramMinLevel = 1,
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        workoutSessionRepository = mockk(relaxed = true)
        exerciseRepository = mockk(relaxed = true)
        restTimerManager = mockk(relaxed = true)
        stateMachine = WorkoutStateMachine()

        every { restTimerManager.state } returns restTimerStateFlow

        coEvery { exerciseRepository.getExerciseById(1L) } returns exerciseLibrary1
        coEvery { exerciseRepository.getExerciseById(2L) } returns exerciseLibrary2

        every { workoutSessionRepository.getExercisesForSession(42L) } returns flowOf(exercises)
        every { workoutSessionRepository.getSetsForExercise(100L) } returns flowOf(setsForExercise1)
        every { workoutSessionRepository.getSetsForExercise(101L) } returns flowOf(setsForExercise2)

        coEvery { workoutSessionRepository.completeSet(any(), any(), any(), any()) } just Runs
        coEvery { workoutSessionRepository.updateSession(any()) } just Runs
        coEvery { workoutSessionRepository.updateStatus(any(), any(), any()) } just Runs
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        sessionIdInSavedState: Long? = null,
        activeSessionInDb: WorkoutSession? = null,
    ): WorkoutViewModel {
        savedStateHandle = if (sessionIdInSavedState != null) {
            SavedStateHandle(mapOf(WorkoutViewModel.SESSION_ID_KEY to sessionIdInSavedState))
        } else {
            SavedStateHandle()
        }

        coEvery { workoutSessionRepository.getActiveSession() } returns activeSessionInDb
        coEvery { workoutSessionRepository.getSession(42L) } returns (activeSessionInDb ?: activeSession)

        return WorkoutViewModel(
            savedStateHandle = savedStateHandle,
            workoutSessionRepository = workoutSessionRepository,
            exerciseRepository = exerciseRepository,
            restTimerManager = restTimerManager,
            stateMachine = stateMachine,
        )
    }

    // -------------------------------------------------------------------------
    // Process death: sessionId in SavedStateHandle -> full state restoration
    // -------------------------------------------------------------------------

    @Nested
    inner class ProcessDeathRecovery {

        @Test
        fun `restores session from Room when sessionId is in SavedStateHandle`() = runTest {
            val viewModel = createViewModel(
                sessionIdInSavedState = 42L,
                activeSessionInDb = activeSession,
            )

            viewModel.state.test {
                val state = awaitItem()
                assertThat(state.sessionId).isEqualTo(42L)
                assertThat(state.phase).isEqualTo(WorkoutPhaseUi.Active)
            }
        }

        @Test
        fun `restores all exercises with correct names`() = runTest {
            val viewModel = createViewModel(
                sessionIdInSavedState = 42L,
                activeSessionInDb = activeSession,
            )

            viewModel.state.test {
                val state = awaitItem()
                assertThat(state.exercises).hasSize(2)
                assertThat(state.exercises[0].name).isEqualTo("Bench Press")
                assertThat(state.exercises[1].name).isEqualTo("Dumbbell Curl")
            }
        }

        @Test
        fun `restores completed sets with actual values`() = runTest {
            val viewModel = createViewModel(
                sessionIdInSavedState = 42L,
                activeSessionInDb = activeSession,
            )

            viewModel.state.test {
                val state = awaitItem()
                val firstExerciseSets = state.exercises[0].sets

                // First two sets should be COMPLETED
                assertThat(firstExerciseSets[0].status).isEqualTo(SetStatus.COMPLETED)
                assertThat(firstExerciseSets[0].actualWeightKg).isEqualTo(100.0)
                assertThat(firstExerciseSets[0].actualReps).isEqualTo(5)

                assertThat(firstExerciseSets[1].status).isEqualTo(SetStatus.COMPLETED)
                assertThat(firstExerciseSets[1].actualWeightKg).isEqualTo(100.0)
            }
        }

        @Test
        fun `first planned set is marked IN_PROGRESS after restoration`() = runTest {
            val viewModel = createViewModel(
                sessionIdInSavedState = 42L,
                activeSessionInDb = activeSession,
            )

            viewModel.state.test {
                val state = awaitItem()
                val firstExerciseSets = state.exercises[0].sets

                // Third set (first non-completed) should be IN_PROGRESS
                assertThat(firstExerciseSets[2].status).isEqualTo(SetStatus.IN_PROGRESS)
            }
        }

        @Test
        fun `elapsed time is calculated from startedAt minus paused duration`() = runTest {
            val viewModel = createViewModel(
                sessionIdInSavedState = 42L,
                activeSessionInDb = activeSession,
            )

            viewModel.state.test {
                val state = awaitItem()
                // Session started 5 minutes ago, no paused duration
                // Elapsed should be approximately 300 seconds (allow margin for test execution)
                assertThat(state.elapsedSeconds).isAtLeast(295)
                assertThat(state.elapsedSeconds).isAtMost(310)
            }
        }

        @Test
        fun `sessionId is persisted to SavedStateHandle after loading`() = runTest {
            createViewModel(
                sessionIdInSavedState = null,
                activeSessionInDb = activeSession,
            )

            val savedId = savedStateHandle.get<Long>(WorkoutViewModel.SESSION_ID_KEY)
            assertThat(savedId).isEqualTo(42L)
        }
    }

    // -------------------------------------------------------------------------
    // No saved session -> starts fresh or error
    // -------------------------------------------------------------------------

    @Nested
    inner class NoSavedSession {

        @Test
        fun `shows error when no sessionId and no active session in Room`() = runTest {
            val viewModel = createViewModel(
                sessionIdInSavedState = null,
                activeSessionInDb = null,
            )

            viewModel.state.test {
                val state = awaitItem()
                assertThat(state.phase).isInstanceOf(WorkoutPhaseUi.Error::class.java)
            }
        }

        @Test
        fun `finds active session from Room when no sessionId in SavedStateHandle`() = runTest {
            val viewModel = createViewModel(
                sessionIdInSavedState = null,
                activeSessionInDb = activeSession,
            )

            viewModel.state.test {
                val state = awaitItem()
                assertThat(state.sessionId).isEqualTo(42L)
                assertThat(state.phase).isEqualTo(WorkoutPhaseUi.Active)
            }
        }

        @Test
        fun `shows error when sessionId exists but session deleted from Room`() = runTest {
            savedStateHandle = SavedStateHandle(
                mapOf(WorkoutViewModel.SESSION_ID_KEY to 999L),
            )
            coEvery { workoutSessionRepository.getSession(999L) } returns null

            val viewModel = WorkoutViewModel(
                savedStateHandle = savedStateHandle,
                workoutSessionRepository = workoutSessionRepository,
                exerciseRepository = exerciseRepository,
                restTimerManager = restTimerManager,
                stateMachine = stateMachine,
            )

            viewModel.state.test {
                val state = awaitItem()
                assertThat(state.phase).isInstanceOf(WorkoutPhaseUi.Error::class.java)
            }
        }
    }

    // -------------------------------------------------------------------------
    // Paused session restoration
    // -------------------------------------------------------------------------

    @Nested
    inner class PausedSessionRecovery {

        @Test
        fun `restores paused session with paused UI state`() = runTest {
            val viewModel = createViewModel(
                sessionIdInSavedState = 42L,
                activeSessionInDb = pausedSession,
            )

            viewModel.state.test {
                val state = awaitItem()
                assertThat(state.phase).isEqualTo(WorkoutPhaseUi.Paused)
                assertThat(state.isPaused).isTrue()
            }
        }

        @Test
        fun `accounts for paused duration in elapsed time`() = runTest {
            val viewModel = createViewModel(
                sessionIdInSavedState = 42L,
                activeSessionInDb = pausedSession, // 60 seconds paused
            )

            viewModel.state.test {
                val state = awaitItem()
                // 300 seconds total - 60 seconds paused = ~240 seconds active
                assertThat(state.elapsedSeconds).isAtLeast(235)
                assertThat(state.elapsedSeconds).isAtMost(250)
            }
        }
    }

    // -------------------------------------------------------------------------
    // Data integrity: auto-save verification
    // -------------------------------------------------------------------------

    @Nested
    inner class AutoSaveVerification {

        @Test
        fun `completing a set writes to Room before updating in-memory state`() = runTest {
            val viewModel = createViewModel(
                sessionIdInSavedState = 42L,
                activeSessionInDb = activeSession,
            )

            viewModel.state.test { awaitItem() }

            viewModel.onIntent(
                WorkoutIntent.CompleteSet(
                    workoutExerciseId = 100L,
                    setId = 3L,
                    setIndex = 3,
                    weight = 100.0,
                    reps = 5,
                ),
            )

            coVerify(exactly = 1) {
                workoutSessionRepository.completeSet(
                    workoutExerciseId = 100L,
                    setIndex = 3,
                    weight = 100.0,
                    reps = 5,
                )
            }
        }

        @Test
        fun `each set completion is an individual Room write, not batched`() = runTest {
            val viewModel = createViewModel(
                sessionIdInSavedState = 42L,
                activeSessionInDb = activeSession,
            )

            viewModel.state.test { awaitItem() }

            // Complete set 3 of exercise 1
            viewModel.onIntent(
                WorkoutIntent.CompleteSet(
                    workoutExerciseId = 100L, setId = 3L, setIndex = 3,
                    weight = 100.0, reps = 5,
                ),
            )

            // Complete set 1 of exercise 2
            viewModel.onIntent(
                WorkoutIntent.CompleteSet(
                    workoutExerciseId = 101L, setId = 4L, setIndex = 1,
                    weight = 40.0, reps = 12,
                ),
            )

            // Two separate writes, not batched
            coVerify(exactly = 1) {
                workoutSessionRepository.completeSet(
                    workoutExerciseId = 100L, setIndex = 3,
                    weight = 100.0, reps = 5,
                )
            }
            coVerify(exactly = 1) {
                workoutSessionRepository.completeSet(
                    workoutExerciseId = 101L, setIndex = 1,
                    weight = 40.0, reps = 12,
                )
            }
        }
    }
}
