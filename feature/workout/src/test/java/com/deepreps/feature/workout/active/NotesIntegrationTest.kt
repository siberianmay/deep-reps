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
import com.deepreps.core.domain.provider.AnalyticsTracker
import com.deepreps.core.domain.statemachine.WorkoutStateMachine
import io.mockk.Runs
import io.mockk.coEvery
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Integration tests for per-exercise notes functionality (Epic 12).
 *
 * Tests cover:
 * - ToggleNotes intent expands/collapses notes field
 * - UpdateNotes intent updates in-memory state
 * - 1000 character limit enforcement
 * - Notes state survives across intent dispatches
 * - Initial notes loaded from exercise data
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NotesIntegrationTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var workoutSessionRepository: WorkoutSessionRepository
    private lateinit var exerciseRepository: ExerciseRepository
    private lateinit var restTimerManager: RestTimerManager
    private lateinit var stateMachine: WorkoutStateMachine
    private lateinit var analyticsTracker: AnalyticsTracker
    private lateinit var viewModel: WorkoutViewModel

    private val testSession = WorkoutSession(
        id = 1L,
        startedAt = System.currentTimeMillis() - 300_000,
        completedAt = null,
        durationSeconds = null,
        pausedDurationSeconds = 0L,
        status = SessionStatus.ACTIVE,
        notes = null,
        templateId = null,
    )

    private val testExercises = listOf(
        WorkoutExercise(
            id = 10L,
            sessionId = 1L,
            exerciseId = 100L,
            orderIndex = 0,
            supersetGroupId = null,
            restTimerSeconds = 120,
            notes = "Existing notes from session",
        ),
        WorkoutExercise(
            id = 11L,
            sessionId = 1L,
            exerciseId = 101L,
            orderIndex = 1,
            supersetGroupId = null,
            restTimerSeconds = 90,
            notes = null,
        ),
    )

    private val testSets = listOf(
        WorkoutSet(
            id = 1,
            setNumber = 1,
            type = SetType.WORKING,
            status = SetStatus.PLANNED,
            plannedWeightKg = 80.0,
            plannedReps = 8,
            actualWeightKg = null,
            actualReps = null,
        ),
    )

    private val testExerciseLibrary1 = Exercise(
        id = 100L, stableId = "chest_bench_press", name = "Bench Press",
        description = "", equipment = Equipment.BARBELL,
        movementType = MovementType.COMPOUND, difficulty = Difficulty.INTERMEDIATE,
        primaryGroupId = 3L, secondaryMuscles = emptyList(), tips = emptyList(),
        pros = emptyList(), displayOrder = 1, orderPriority = 1,
        supersetTags = emptyList(), autoProgramMinLevel = 1,
    )

    private val testExerciseLibrary2 = Exercise(
        id = 101L, stableId = "chest_fly", name = "Dumbbell Fly",
        description = "", equipment = Equipment.DUMBBELL,
        movementType = MovementType.ISOLATION, difficulty = Difficulty.BEGINNER,
        primaryGroupId = 3L, secondaryMuscles = emptyList(), tips = emptyList(),
        pros = emptyList(), displayOrder = 2, orderPriority = 2,
        supersetTags = emptyList(), autoProgramMinLevel = 1,
    )

    private val restTimerStateFlow = MutableStateFlow(RestTimerState.IDLE)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        savedStateHandle = SavedStateHandle(mapOf(WorkoutViewModel.SESSION_ID_KEY to 1L))
        workoutSessionRepository = mockk(relaxed = true)
        exerciseRepository = mockk(relaxed = true)
        restTimerManager = mockk(relaxed = true)
        stateMachine = WorkoutStateMachine()
        analyticsTracker = mockk(relaxed = true)

        coEvery { workoutSessionRepository.getSession(1L) } returns testSession
        coEvery { workoutSessionRepository.getActiveSession() } returns testSession
        every { workoutSessionRepository.getExercisesForSession(1L) } returns flowOf(testExercises)
        every { workoutSessionRepository.getSetsForExercise(10L) } returns flowOf(testSets)
        every { workoutSessionRepository.getSetsForExercise(11L) } returns flowOf(testSets)
        coEvery { exerciseRepository.getExerciseById(100L) } returns testExerciseLibrary1
        coEvery { exerciseRepository.getExerciseById(101L) } returns testExerciseLibrary2
        every { restTimerManager.state } returns restTimerStateFlow
        coEvery { workoutSessionRepository.completeSet(any(), any(), any(), any()) } just Runs
        coEvery { workoutSessionRepository.updateSession(any()) } just Runs
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): WorkoutViewModel = WorkoutViewModel(
        savedStateHandle = savedStateHandle,
        workoutSessionRepository = workoutSessionRepository,
        exerciseRepository = exerciseRepository,
        restTimerManager = restTimerManager,
        stateMachine = stateMachine,
        analyticsTracker = analyticsTracker,
    )

    // --- Initial Notes State ---

    @Test
    fun `loads existing notes from workout exercise`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(
                "Existing notes from session",
                state.exercises[0].notes,
            )
            assertNull(state.exercises[1].notes)
        }
    }

    @Test
    fun `notes expanded set is empty initially`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.notesExpandedExerciseIds.isEmpty())
        }
    }

    // --- Toggle Notes ---

    @Test
    fun `toggle notes expands notes field for exercise`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(WorkoutIntent.ToggleNotes(10L))

            val state = awaitItem()
            assertTrue(10L in state.notesExpandedExerciseIds)
        }
    }

    @Test
    fun `toggle notes again collapses notes field`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(WorkoutIntent.ToggleNotes(10L))
            awaitItem() // expanded

            viewModel.onIntent(WorkoutIntent.ToggleNotes(10L))
            val state = awaitItem()
            assertFalse(10L in state.notesExpandedExerciseIds)
        }
    }

    @Test
    fun `multiple exercises can have notes expanded simultaneously`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(WorkoutIntent.ToggleNotes(10L))
            awaitItem()

            viewModel.onIntent(WorkoutIntent.ToggleNotes(11L))
            val state = awaitItem()

            assertTrue(10L in state.notesExpandedExerciseIds)
            assertTrue(11L in state.notesExpandedExerciseIds)
        }
    }

    // --- Update Notes ---

    @Test
    fun `update notes changes in-memory state`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(WorkoutIntent.UpdateNotes(10L, "New notes text"))

            val state = awaitItem()
            assertEquals("New notes text", state.exercises[0].notes)
        }
    }

    @Test
    fun `update notes with empty string sets notes to null`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // initial (exercise 0 has "Existing notes from session")

            viewModel.onIntent(WorkoutIntent.UpdateNotes(10L, ""))

            val state = awaitItem()
            assertNull(state.exercises[0].notes)
        }
    }

    @Test
    fun `update notes on second exercise does not affect first`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(WorkoutIntent.UpdateNotes(11L, "Notes for fly"))

            val state = awaitItem()
            assertEquals("Existing notes from session", state.exercises[0].notes)
            assertEquals("Notes for fly", state.exercises[1].notes)
        }
    }

    // --- Character Limit ---

    @Test
    fun `notes are truncated to 1000 characters`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // initial

            val longText = "A".repeat(1500)
            viewModel.onIntent(WorkoutIntent.UpdateNotes(10L, longText))

            val state = awaitItem()
            assertEquals(WorkoutViewModel.NOTES_MAX_LENGTH, state.exercises[0].notes?.length)
        }
    }

    @Test
    fun `notes at exactly 1000 characters are accepted`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // initial

            val exactText = "B".repeat(WorkoutViewModel.NOTES_MAX_LENGTH)
            viewModel.onIntent(WorkoutIntent.UpdateNotes(10L, exactText))

            val state = awaitItem()
            assertEquals(WorkoutViewModel.NOTES_MAX_LENGTH, state.exercises[0].notes?.length)
        }
    }

    @Test
    fun `notes under 1000 characters are preserved exactly`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // initial

            val shortText = "Short note"
            viewModel.onIntent(WorkoutIntent.UpdateNotes(10L, shortText))

            val state = awaitItem()
            assertEquals(shortText, state.exercises[0].notes)
        }
    }
}
