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
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModelTest {

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
        startedAt = System.currentTimeMillis() - 300_000, // 5 minutes ago
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
            notes = null,
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

    private val testSets1 = listOf(
        WorkoutSet(
            id = 1,
            setNumber = 1,
            type = SetType.WORKING,
            status = SetStatus.PLANNED,
            plannedWeightKg = 80.0,
            plannedReps = 8,
            actualWeightKg = null,
            actualReps = null
        ),
        WorkoutSet(
            id = 2,
            setNumber = 2,
            type = SetType.WORKING,
            status = SetStatus.PLANNED,
            plannedWeightKg = 80.0,
            plannedReps = 8,
            actualWeightKg = null,
            actualReps = null
        ),
        WorkoutSet(
            id = 3,
            setNumber = 3,
            type = SetType.WORKING,
            status = SetStatus.PLANNED,
            plannedWeightKg = 80.0,
            plannedReps = 8,
            actualWeightKg = null,
            actualReps = null
        ),
    )

    private val testSets2 = listOf(
        WorkoutSet(
            id = 4,
            setNumber = 1,
            type = SetType.WORKING,
            status = SetStatus.PLANNED,
            plannedWeightKg = 40.0,
            plannedReps = 12,
            actualWeightKg = null,
            actualReps = null
        ),
        WorkoutSet(
            id = 5,
            setNumber = 2,
            type = SetType.WORKING,
            status = SetStatus.PLANNED,
            plannedWeightKg = 40.0,
            plannedReps = 12,
            actualWeightKg = null,
            actualReps = null
        ),
    )

    private val testExerciseLibrary1 = Exercise(
        id = 100L,
        stableId = "chest_barbell_bench_press",
        name = "Bench Press",
        description = "Barbell bench press",
        equipment = Equipment.BARBELL,
        movementType = MovementType.COMPOUND,
        difficulty = Difficulty.INTERMEDIATE,
        primaryGroupId = 3L,
        secondaryMuscles = emptyList(),
        tips = emptyList(),
        pros = emptyList(),
        displayOrder = 1,
        orderPriority = 1,
        supersetTags = emptyList(),
        autoProgramMinLevel = 1,
    )

    private val testExerciseLibrary2 = Exercise(
        id = 101L,
        stableId = "chest_dumbbell_fly",
        name = "Dumbbell Fly",
        description = "Dumbbell chest fly",
        equipment = Equipment.DUMBBELL,
        movementType = MovementType.ISOLATION,
        difficulty = Difficulty.BEGINNER,
        primaryGroupId = 3L,
        secondaryMuscles = emptyList(),
        tips = emptyList(),
        pros = emptyList(),
        displayOrder = 2,
        orderPriority = 2,
        supersetTags = emptyList(),
        autoProgramMinLevel = 1,
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

        // Setup mocks
        coEvery { workoutSessionRepository.getSession(1L) } returns testSession
        coEvery { workoutSessionRepository.getActiveSession() } returns testSession
        every { workoutSessionRepository.getExercisesForSession(1L) } returns flowOf(testExercises)
        every { workoutSessionRepository.getSetsForExercise(10L) } returns flowOf(testSets1)
        every { workoutSessionRepository.getSetsForExercise(11L) } returns flowOf(testSets2)
        coEvery { exerciseRepository.getExerciseById(100L) } returns testExerciseLibrary1
        coEvery { exerciseRepository.getExerciseById(101L) } returns testExerciseLibrary2
        every { restTimerManager.state } returns restTimerStateFlow
        coEvery { workoutSessionRepository.completeSet(any(), any(), any(), any()) } just Runs
        coEvery { workoutSessionRepository.updateSession(any()) } just Runs
        coEvery { workoutSessionRepository.updateStatus(any(), any(), any()) } just Runs
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

    // --- Session Loading ---

    @Test
    fun `loads session and exercises on init`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(2, state.exercises.size)
            assertEquals("Bench Press", state.exercises[0].name)
            assertEquals("Dumbbell Fly", state.exercises[1].name)
            assertEquals(3, state.exercises[0].sets.size)
            assertEquals(2, state.exercises[1].sets.size)
            assertEquals(1L, state.sessionId)
            assertTrue(state.phase is WorkoutPhaseUi.Active)
        }
    }

    @Test
    fun `first planned set is marked IN_PROGRESS`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            val firstSet = state.exercises[0].sets[0]
            assertEquals(SetStatus.IN_PROGRESS, firstSet.status)
            assertEquals(SetStatus.PLANNED, state.exercises[0].sets[1].status)
        }
    }

    @Test
    fun `active exercise is expanded, others collapsed or auto-expanded based on completeness`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            // First exercise has incomplete sets -> expanded
            assertTrue(state.exercises[0].isExpanded)
            // Second exercise also has incomplete sets -> expanded
            assertTrue(state.exercises[1].isExpanded)
        }
    }

    @Test
    fun `error state when session not found`() = runTest {
        coEvery { workoutSessionRepository.getSession(1L) } returns null

        viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.phase is WorkoutPhaseUi.Error)
        }
    }

    // --- Set Completion ---

    @Test
    fun `complete set writes to Room immediately`() = runTest {
        viewModel = createViewModel()

        // Wait for init
        viewModel.state.test { awaitItem() }

        viewModel.onIntent(
            WorkoutIntent.CompleteSet(
                workoutExerciseId = 10L,
                setId = 1L,
                setIndex = 1,
                weight = 80.0,
                reps = 8,
            ),
        )

        // Verify Room write
        coVerify {
            workoutSessionRepository.completeSet(
                workoutExerciseId = 10L,
                setIndex = 1,
                weight = 80.0,
                reps = 8,
            )
        }
    }

    @Test
    fun `complete set updates state to COMPLETED`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // initial loaded state

            viewModel.onIntent(
                WorkoutIntent.CompleteSet(
                    workoutExerciseId = 10L,
                    setId = 1L,
                    setIndex = 1,
                    weight = 80.0,
                    reps = 8,
                ),
            )

            val updated = awaitItem()
            val completedSet = updated.exercises[0].sets.find { it.id == 1L }
            assertEquals(SetStatus.COMPLETED, completedSet?.status)
            assertEquals(80.0, completedSet?.actualWeightKg)
            assertEquals(8, completedSet?.actualReps)
        }
    }

    @Test
    fun `complete set starts rest timer when more sets remain`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test { awaitItem() }

        viewModel.onIntent(
            WorkoutIntent.CompleteSet(
                workoutExerciseId = 10L,
                setId = 1L,
                setIndex = 1,
                weight = 80.0,
                reps = 8,
            ),
        )

        verify { restTimerManager.start(120) }
    }

    @Test
    fun `next set becomes IN_PROGRESS after completing current`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(
                WorkoutIntent.CompleteSet(
                    workoutExerciseId = 10L,
                    setId = 1L,
                    setIndex = 1,
                    weight = 80.0,
                    reps = 8,
                ),
            )

            val updated = awaitItem()
            val secondSet = updated.exercises[0].sets[1]
            assertEquals(SetStatus.IN_PROGRESS, secondSet.status)
        }
    }

    // --- Rest Timer ---

    @Test
    fun `skip rest timer delegates to manager`() = runTest {
        viewModel = createViewModel()

        viewModel.onIntent(WorkoutIntent.SkipRestTimer)

        verify { restTimerManager.skip() }
    }

    @Test
    fun `extend rest timer adds 30s`() = runTest {
        viewModel = createViewModel()

        viewModel.onIntent(WorkoutIntent.ExtendRestTimer)

        verify { restTimerManager.extend(30) }
    }

    @Test
    fun `rest timer state is reflected in UI state`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // initial - no timer

            // Simulate timer starting
            val timerState = RestTimerState(
                remainingSeconds = 120,
                totalSeconds = 120,
                isActive = true,
            )
            restTimerStateFlow.value = timerState

            val updated = awaitItem()
            assertNotNull(updated.activeRestTimer)
            assertEquals(120, updated.activeRestTimer?.remainingSeconds)
        }
    }

    // --- Pause/Resume ---

    @Test
    fun `pause workout updates status in Room`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test { awaitItem() }

        viewModel.onIntent(WorkoutIntent.PauseWorkout)

        coVerify {
            workoutSessionRepository.updateStatus(
                id = 1L,
                status = "paused",
                completedAt = null,
            )
        }
    }

    @Test
    fun `pause workout updates UI state`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(WorkoutIntent.PauseWorkout)

            val paused = awaitItem()
            assertTrue(paused.isPaused)
            assertEquals(WorkoutPhaseUi.Paused, paused.phase)
        }
    }

    @Test
    fun `resume workout updates UI state back to active`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()

            viewModel.onIntent(WorkoutIntent.PauseWorkout)
            awaitItem() // paused

            viewModel.onIntent(WorkoutIntent.ResumeWorkout)

            val resumed = awaitItem()
            assertFalse(resumed.isPaused)
            assertEquals(WorkoutPhaseUi.Active, resumed.phase)
        }
    }

    @Test
    fun `pause also pauses rest timer`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test { awaitItem() }

        viewModel.onIntent(WorkoutIntent.PauseWorkout)

        verify { restTimerManager.pause() }
    }

    @Test
    fun `resume also resumes rest timer`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test { awaitItem() }

        viewModel.onIntent(WorkoutIntent.PauseWorkout)
        viewModel.onIntent(WorkoutIntent.ResumeWorkout)

        verify { restTimerManager.resume() }
    }

    // --- Finish Workout ---

    @Test
    fun `request finish shows confirmation dialog`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()

            viewModel.onIntent(WorkoutIntent.RequestFinishWorkout)

            val updated = awaitItem()
            assertTrue(updated.showFinishDialog)
        }
    }

    @Test
    fun `dismiss finish dialog hides it`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()

            viewModel.onIntent(WorkoutIntent.RequestFinishWorkout)
            awaitItem() // dialog showing

            viewModel.onIntent(WorkoutIntent.DismissFinishDialog)

            val updated = awaitItem()
            assertFalse(updated.showFinishDialog)
        }
    }

    @Test
    fun `confirm finish completes session in Room`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test { awaitItem() }

        viewModel.onIntent(WorkoutIntent.ConfirmFinishWorkout)

        coVerify {
            workoutSessionRepository.updateSession(
                match {
                it.status == SessionStatus.COMPLETED && it.completedAt != null
            }
            )
        }
    }

    @Test
    fun `confirm finish emits NavigateToSummary side effect`() = runTest {
        viewModel = createViewModel()

        viewModel.sideEffect.test {
            // Skip the initial ScrollToExercise effect from init
            val initial = awaitItem()
            assertTrue(initial is WorkoutSideEffect.ScrollToExercise)

            viewModel.onIntent(WorkoutIntent.ConfirmFinishWorkout)

            val effect = awaitItem()
            assertTrue(effect is WorkoutSideEffect.NavigateToSummary)
            assertEquals(1L, (effect as WorkoutSideEffect.NavigateToSummary).sessionId)
        }
    }

    @Test
    fun `confirm finish cancels rest timer`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test { awaitItem() }

        viewModel.onIntent(WorkoutIntent.ConfirmFinishWorkout)

        verify { restTimerManager.cancel() }
    }

    // --- Toggle Expand ---

    @Test
    fun `toggle expand flips exercise expansion state`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            val initial = awaitItem()
            val wasExpanded = initial.exercises[0].isExpanded

            viewModel.onIntent(WorkoutIntent.ToggleExerciseExpanded(10L))

            val updated = awaitItem()
            assertEquals(!wasExpanded, updated.exercises[0].isExpanded)
        }
    }

    // --- Elapsed Time Formatting ---

    @Test
    fun `formatElapsedTime with minutes only`() {
        assertEquals("05:30", formatElapsedTime(330))
    }

    @Test
    fun `formatElapsedTime with hours`() {
        assertEquals("1:05:30", formatElapsedTime(3930))
    }

    @Test
    fun `formatElapsedTime zero`() {
        assertEquals("00:00", formatElapsedTime(0))
    }

    @Test
    fun `formatElapsedTime edge case 59 seconds`() {
        assertEquals("00:59", formatElapsedTime(59))
    }

    @Test
    fun `formatElapsedTime exactly one hour`() {
        assertEquals("1:00:00", formatElapsedTime(3600))
    }
}
