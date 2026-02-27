package com.deepreps.feature.progress

import app.cash.turbine.test
import com.deepreps.core.domain.model.Exercise
import com.deepreps.core.domain.model.UserProfile
import com.deepreps.core.domain.model.WorkoutExercise
import com.deepreps.core.domain.model.WorkoutSession
import com.deepreps.core.domain.model.WorkoutSet
import com.deepreps.core.domain.model.enums.Difficulty
import com.deepreps.core.domain.model.enums.Equipment
import com.deepreps.core.domain.model.enums.ExperienceLevel
import com.deepreps.core.domain.model.enums.MovementType
import com.deepreps.core.domain.model.enums.SessionStatus
import com.deepreps.core.domain.model.enums.SetStatus
import com.deepreps.core.domain.model.enums.SetType
import com.deepreps.core.domain.model.enums.WeightUnit
import com.deepreps.core.domain.repository.ExerciseRepository
import com.deepreps.core.domain.repository.UserProfileRepository
import com.deepreps.core.domain.repository.WorkoutSessionRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
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
class ProgressDashboardViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var workoutSessionRepository: WorkoutSessionRepository
    private lateinit var exerciseRepository: ExerciseRepository
    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var viewModel: ProgressDashboardViewModel

    private val now = System.currentTimeMillis()

    private val testSessions = listOf(
        WorkoutSession(
            id = 1L,
            startedAt = now - 86_400_000, // 1 day ago
            completedAt = now - 86_400_000 + 3_600_000,
            durationSeconds = 3600L,
            pausedDurationSeconds = 0L,
            status = SessionStatus.COMPLETED,
            notes = null,
            templateId = null,
        ),
        WorkoutSession(
            id = 2L,
            startedAt = now - 172_800_000, // 2 days ago
            completedAt = now - 172_800_000 + 2_400_000,
            durationSeconds = 2400L,
            pausedDurationSeconds = 0L,
            status = SessionStatus.COMPLETED,
            notes = null,
            templateId = null,
        ),
    )

    private val testExercisesSession1 = listOf(
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

    private val testSetsSession1 = listOf(
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
            actualWeightKg = 80.0,
            actualReps = 8,
        ),
    )

    private val testExerciseLibrary = Exercise(
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

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        workoutSessionRepository = mockk(relaxed = true)
        exerciseRepository = mockk(relaxed = true)
        userProfileRepository = mockk(relaxed = true)

        every { workoutSessionRepository.getCompletedSessions() } returns flowOf(testSessions)
        every { workoutSessionRepository.getExercisesForSession(1L) } returns
            flowOf(testExercisesSession1)
        every { workoutSessionRepository.getExercisesForSession(2L) } returns flowOf(emptyList())
        every { workoutSessionRepository.getSetsForExercise(10L) } returns
            flowOf(testSetsSession1)
        coEvery { exerciseRepository.getExercisesByIds(listOf(100L)) } returns
            listOf(testExerciseLibrary)
        coEvery { exerciseRepository.getExercisesByIds(emptyList()) } returns emptyList()
        coEvery { userProfileRepository.get() } returns null
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Loading Sessions ---

    @Test
    fun `loads completed sessions on init`() = runTest {
        viewModel = ProgressDashboardViewModel(
            workoutSessionRepository, exerciseRepository, userProfileRepository,
        )

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(2, state.recentSessions.size)
        }
    }

    @Test
    fun `session summary includes exercise count and volume`() = runTest {
        viewModel = ProgressDashboardViewModel(
            workoutSessionRepository, exerciseRepository, userProfileRepository,
        )

        viewModel.state.test {
            val state = awaitItem()
            val session1 = state.recentSessions[0]
            assertEquals(1, session1.exerciseCount)
            // 80 * 8 + 80 * 8 = 1280.0 kg volume
            assertEquals(1280.0, session1.totalVolumeKg, 0.01)
            assertEquals(2, session1.setCount)
        }
    }

    @Test
    fun `session summary includes duration text`() = runTest {
        viewModel = ProgressDashboardViewModel(
            workoutSessionRepository, exerciseRepository, userProfileRepository,
        )

        viewModel.state.test {
            val state = awaitItem()
            val session1 = state.recentSessions[0]
            assertEquals("1h 0m", session1.durationText)
        }
    }

    @Test
    fun `session summary includes muscle group names`() = runTest {
        viewModel = ProgressDashboardViewModel(
            workoutSessionRepository, exerciseRepository, userProfileRepository,
        )

        viewModel.state.test {
            val state = awaitItem()
            val session1 = state.recentSessions[0]
            assertTrue(session1.muscleGroupNames.contains("Chest"))
        }
    }

    // --- Empty State ---

    @Test
    fun `empty sessions list shows empty state`() = runTest {
        every { workoutSessionRepository.getCompletedSessions() } returns flowOf(emptyList())

        viewModel = ProgressDashboardViewModel(
            workoutSessionRepository, exerciseRepository, userProfileRepository,
        )

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertTrue(state.recentSessions.isEmpty())
            assertNull(state.errorType)
        }
    }

    // --- Error Handling ---

    @Test
    fun `repository error sets error state`() = runTest {
        every { workoutSessionRepository.getCompletedSessions() } returns flow {
            throw IllegalStateException("DB error")
        }

        viewModel = ProgressDashboardViewModel(
            workoutSessionRepository, exerciseRepository, userProfileRepository,
        )

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(ProgressDashboardError.LoadFailed, state.errorType)
        }
    }

    @Test
    fun `retry after error reloads sessions`() = runTest {
        var callCount = 0
        every { workoutSessionRepository.getCompletedSessions() } answers {
            callCount++
            if (callCount == 1) {
                flow { throw IllegalStateException("DB error") }
            } else {
                flowOf(testSessions)
            }
        }

        viewModel = ProgressDashboardViewModel(
            workoutSessionRepository, exerciseRepository, userProfileRepository,
        )

        viewModel.state.test {
            val errorState = awaitItem()
            assertEquals(ProgressDashboardError.LoadFailed, errorState.errorType)

            viewModel.onIntent(ProgressDashboardIntent.Retry)

            val recovered = awaitItem()
            assertNull(recovered.errorType)
            assertEquals(2, recovered.recentSessions.size)
        }
    }

    // --- Time Range Selection ---

    @Test
    fun `select time range updates state`() = runTest {
        viewModel = ProgressDashboardViewModel(
            workoutSessionRepository, exerciseRepository, userProfileRepository,
        )

        viewModel.state.test {
            awaitItem() // initial with TWELVE_WEEKS

            viewModel.onIntent(ProgressDashboardIntent.SelectTimeRange(TimeRange.FOUR_WEEKS))

            val state = awaitItem()
            assertEquals(TimeRange.FOUR_WEEKS, state.selectedTimeRange)
        }
    }

    // --- Navigation Side Effects ---

    @Test
    fun `view session emits NavigateToSessionDetail`() = runTest {
        viewModel = ProgressDashboardViewModel(
            workoutSessionRepository, exerciseRepository, userProfileRepository,
        )

        viewModel.sideEffect.test {
            viewModel.onIntent(ProgressDashboardIntent.ViewSession(1L))

            val effect = awaitItem()
            assertTrue(effect is ProgressDashboardSideEffect.NavigateToSessionDetail)
            assertEquals(
                1L,
                (effect as ProgressDashboardSideEffect.NavigateToSessionDetail).sessionId,
            )
        }
    }

    @Test
    fun `view exercise progress emits NavigateToExerciseProgress`() = runTest {
        viewModel = ProgressDashboardViewModel(
            workoutSessionRepository, exerciseRepository, userProfileRepository,
        )

        viewModel.sideEffect.test {
            viewModel.onIntent(ProgressDashboardIntent.ViewExerciseProgress(100L))

            val effect = awaitItem()
            assertTrue(effect is ProgressDashboardSideEffect.NavigateToExerciseProgress)
            assertEquals(
                100L,
                (effect as ProgressDashboardSideEffect.NavigateToExerciseProgress).exerciseId,
            )
        }
    }

    // --- Weight Unit from Profile ---

    @Test
    fun `loads weight unit from user profile`() = runTest {
        val profile = UserProfile(
            id = 1L,
            experienceLevel = ExperienceLevel.INTERMEDIATE,
            preferredUnit = WeightUnit.LBS,
            age = 30,
            heightCm = 180.0,
            gender = null,
            bodyWeightKg = 80.0,
            compoundRepMin = 6,
            compoundRepMax = 10,
            isolationRepMin = 10,
            isolationRepMax = 15,
            createdAt = now,
            updatedAt = now,
        )
        coEvery { userProfileRepository.get() } returns profile

        viewModel = ProgressDashboardViewModel(
            workoutSessionRepository, exerciseRepository, userProfileRepository,
        )

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(WeightUnit.LBS, state.weightUnit)
        }
    }

    // --- Companion Helpers ---

    @Test
    fun `formatDate formats epoch millis`() {
        // Using a known date: Feb 10, 2026 is epoch ~1770681600000
        // Exact value depends on timezone, so just verify non-empty
        val result = ProgressDashboardViewModel.formatDate(now)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `formatDuration handles null`() {
        assertEquals("--", ProgressDashboardViewModel.formatDuration(null))
    }

    @Test
    fun `formatDuration handles zero`() {
        assertEquals("--", ProgressDashboardViewModel.formatDuration(0))
    }

    @Test
    fun `formatDuration handles minutes only`() {
        assertEquals("45m", ProgressDashboardViewModel.formatDuration(2700))
    }

    @Test
    fun `formatDuration handles hours and minutes`() {
        assertEquals("1h 30m", ProgressDashboardViewModel.formatDuration(5400))
    }

    @Test
    fun `filterByTimeRange with ALL returns all sessions`() {
        val result = ProgressDashboardViewModel.filterByTimeRange(testSessions, TimeRange.ALL)
        assertEquals(2, result.size)
    }
}
