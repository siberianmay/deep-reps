package com.deepreps.feature.progress

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
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
import com.deepreps.core.domain.repository.UserProfileRepository
import com.deepreps.core.domain.repository.WorkoutSessionRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class ExerciseProgressViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var workoutSessionRepository: WorkoutSessionRepository
    private lateinit var exerciseRepository: ExerciseRepository
    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var viewModel: ExerciseProgressViewModel

    private val now = System.currentTimeMillis()
    private val oneWeekMs = 7 * 24 * 60 * 60 * 1000L

    private val testSessions = listOf(
        WorkoutSession(
            id = 1L,
            startedAt = now - 2 * oneWeekMs,
            completedAt = now - 2 * oneWeekMs + 3_600_000,
            durationSeconds = 3600L,
            pausedDurationSeconds = 0L,
            status = SessionStatus.COMPLETED,
            notes = null,
            templateId = null,
        ),
        WorkoutSession(
            id = 2L,
            startedAt = now - oneWeekMs,
            completedAt = now - oneWeekMs + 3_000_000,
            durationSeconds = 3000L,
            pausedDurationSeconds = 0L,
            status = SessionStatus.COMPLETED,
            notes = null,
            templateId = null,
        ),
    )

    private val testExercise = Exercise(
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

    private val session1Exercises = listOf(
        WorkoutExercise(
            id = 10L,
            sessionId = 1L,
            exerciseId = 100L,
            orderIndex = 0,
            supersetGroupId = null,
            restTimerSeconds = 120,
            notes = null,
        ),
    )

    private val session2Exercises = listOf(
        WorkoutExercise(
            id = 20L,
            sessionId = 2L,
            exerciseId = 100L,
            orderIndex = 0,
            supersetGroupId = null,
            restTimerSeconds = 120,
            notes = null,
        ),
    )

    private val session1Sets = listOf(
        WorkoutSet(
            id = 1,
            setNumber = 1,
            type = SetType.WORKING,
            status = SetStatus.COMPLETED,
            plannedWeightKg = 80.0,
            plannedReps = 8,
            actualWeightKg = 80.0,
            actualReps = 8,
        ),
        WorkoutSet(
            id = 2,
            setNumber = 2,
            type = SetType.WORKING,
            status = SetStatus.COMPLETED,
            plannedWeightKg = 80.0,
            plannedReps = 8,
            actualWeightKg = 85.0,
            actualReps = 6,
        ),
    )

    private val session2Sets = listOf(
        WorkoutSet(
            id = 3, setNumber = 1, type = SetType.WORKING, status = SetStatus.COMPLETED,
            plannedWeightKg = 85.0, plannedReps = 8, actualWeightKg = 90.0, actualReps = 5,
            isPersonalRecord = true,
        ),
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        workoutSessionRepository = mockk(relaxed = true)
        exerciseRepository = mockk(relaxed = true)
        userProfileRepository = mockk(relaxed = true)

        every { workoutSessionRepository.getCompletedSessions() } returns flowOf(testSessions)
        every { workoutSessionRepository.getExercisesForSession(1L) } returns
            flowOf(session1Exercises)
        every { workoutSessionRepository.getExercisesForSession(2L) } returns
            flowOf(session2Exercises)
        every { workoutSessionRepository.getSetsForExercise(10L) } returns flowOf(session1Sets)
        every { workoutSessionRepository.getSetsForExercise(20L) } returns flowOf(session2Sets)
        coEvery { exerciseRepository.getExerciseById(100L) } returns testExercise
        coEvery { userProfileRepository.get() } returns null
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(exerciseId: Long = 100L): ExerciseProgressViewModel {
        val handle = SavedStateHandle(
            mapOf(ExerciseProgressViewModel.EXERCISE_ID_ARG to exerciseId),
        )
        return ExerciseProgressViewModel(
            savedStateHandle = handle,
            workoutSessionRepository = workoutSessionRepository,
            exerciseRepository = exerciseRepository,
            userProfileRepository = userProfileRepository,
        )
    }

    // --- Loading Chart Data ---

    @Test
    fun `loads exercise name on init`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Bench Press", state.exerciseName)
        }
    }

    @Test
    fun `loads chart data from completed sessions`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(2, state.chartData.size)
        }
    }

    @Test
    fun `chart data sorted by date ascending`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            val dates = state.chartData.map { it.dateEpochMs }
            assertEquals(dates.sorted(), dates)
        }
    }

    @Test
    fun `best weight is picked for each session`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            // Session 1: max(80.0, 85.0) = 85.0
            assertEquals(85.0, state.chartData[0].weightKg, 0.01)
            // Session 2: 90.0
            assertEquals(90.0, state.chartData[1].weightKg, 0.01)
        }
    }

    @Test
    fun `personal record flag propagates to chart data`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            // Session 2 has a PR set at 90.0
            assertTrue(state.chartData[1].isPersonalRecord)
        }
    }

    @Test
    fun `current best and all time best are computed`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            // Current best = last data point's weight = 90.0
            assertEquals(90.0, state.currentBestKg)
            // All time best = max across all points = 90.0
            assertEquals(90.0, state.allTimeBestKg)
        }
    }

    // --- Empty State ---

    @Test
    fun `no sessions produces empty chart data`() = runTest {
        every { workoutSessionRepository.getCompletedSessions() } returns flowOf(emptyList())

        viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertTrue(state.chartData.isEmpty())
            assertNull(state.currentBestKg)
            assertNull(state.allTimeBestKg)
        }
    }

    @Test
    fun `sessions without target exercise produce empty chart data`() = runTest {
        every { workoutSessionRepository.getExercisesForSession(1L) } returns flowOf(emptyList())
        every { workoutSessionRepository.getExercisesForSession(2L) } returns flowOf(emptyList())

        viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.chartData.isEmpty())
        }
    }

    // --- Error Handling ---

    @Test
    fun `error loading chart data sets error state`() = runTest {
        every { workoutSessionRepository.getCompletedSessions() } returns flowOf(
            listOf(testSessions[0]),
        )
        every { workoutSessionRepository.getExercisesForSession(1L) } answers {
            throw IllegalStateException("DB error")
        }

        viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(ExerciseProgressError.LoadFailed, state.errorType)
        }
    }

    // --- Time Range Selection ---

    @Test
    fun `select time range updates state and reloads data`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(ExerciseProgressIntent.SelectTimeRange(TimeRange.FOUR_WEEKS))

            val state = awaitItem()
            assertEquals(TimeRange.FOUR_WEEKS, state.selectedTimeRange)
        }
    }

    // --- Retry ---

    @Test
    fun `retry reloads data`() = runTest {
        var callCount = 0
        every { workoutSessionRepository.getCompletedSessions() } answers {
            callCount++
            if (callCount == 1) {
                flowOf(emptyList())
            } else {
                flowOf(testSessions)
            }
        }

        viewModel = createViewModel()

        viewModel.state.test {
            val initial = awaitItem()
            assertTrue(initial.chartData.isEmpty())

            viewModel.onIntent(ExerciseProgressIntent.Retry)

            val retried = awaitItem()
            assertEquals(2, retried.chartData.size)
        }
    }

    // --- Companion Helpers ---

    @Test
    fun `findBestWeight returns heaviest completed set`() {
        val sets = listOf(
            WorkoutSet(
                id = 1,
                setNumber = 1,
                type = SetType.WORKING,
                status = SetStatus.COMPLETED,
                plannedWeightKg = 80.0,
                plannedReps = 8,
                actualWeightKg = 80.0,
                actualReps = 8,
            ),
            WorkoutSet(
                id = 2,
                setNumber = 2,
                type = SetType.WORKING,
                status = SetStatus.COMPLETED,
                plannedWeightKg = 80.0,
                plannedReps = 8,
                actualWeightKg = 90.0,
                actualReps = 6,
            ),
            WorkoutSet(
                id = 3,
                setNumber = 3,
                type = SetType.WORKING,
                status = SetStatus.PLANNED,
                plannedWeightKg = 100.0,
                plannedReps = 5,
                actualWeightKg = null,
                actualReps = null,
            ),
        )
        assertEquals(90.0, ExerciseProgressViewModel.findBestWeight(sets))
    }

    @Test
    fun `findBestWeight returns null when no completed sets`() {
        val sets = listOf(
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
        assertNull(ExerciseProgressViewModel.findBestWeight(sets))
    }

    @Test
    fun `findBestWeight returns null for empty list`() {
        assertNull(ExerciseProgressViewModel.findBestWeight(emptyList()))
    }
}
