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
import com.deepreps.core.domain.model.UserProfile
import com.deepreps.core.domain.model.enums.ExperienceLevel
import com.deepreps.core.domain.model.enums.WeightUnit
import com.deepreps.core.domain.util.Estimated1rmCalculator
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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@Suppress("LargeClass")
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

    // --- Dual-Line: Estimated 1RM Computation ---

    private fun completedSet(id: Long, num: Int, weight: Double, reps: Int) = WorkoutSet(
        id = id,
        setNumber = num,
        type = SetType.WORKING,
        status = SetStatus.COMPLETED,
        plannedWeightKg = weight,
        plannedReps = reps,
        actualWeightKg = weight,
        actualReps = reps,
    )

    private fun setupSingleSessionSets(sets: List<WorkoutSet>) {
        every { workoutSessionRepository.getCompletedSessions() } returns
            flowOf(listOf(testSessions[0]))
        every { workoutSessionRepository.getSetsForExercise(10L) } returns flowOf(sets)
    }

    @Test
    fun `estimated 1RM uses highest Epley estimate across working sets`() = runTest {
        // 80x5 -> 93.33, 70x10 -> 93.33, 60x15 -> 90.0
        setupSingleSessionSets(
            listOf(
                completedSet(1, 1, 80.0, 5),
                completedSet(2, 2, 70.0, 10),
                completedSet(3, 3, 60.0, 15),
            ),
        )

        viewModel = createViewModel()

        viewModel.state.test {
            val dataPoint = awaitItem().chartData.first()
            assertNotNull(dataPoint.estimated1rmKg)
            assertEquals(93.33, dataPoint.estimated1rmKg!!, 0.01)
        }
    }

    @Test
    fun `confidence is HIGH for best 1RM from 3-rep set`() = runTest {
        setupSingleSessionSets(listOf(completedSet(1, 1, 100.0, 3)))
        viewModel = createViewModel()

        viewModel.state.test {
            assertEquals(Estimated1rmCalculator.Confidence.HIGH, awaitItem().chartData.first().confidence)
        }
    }

    @Test
    fun `confidence is MODERATE for best 1RM from 8-rep set`() = runTest {
        setupSingleSessionSets(listOf(completedSet(1, 1, 80.0, 8)))
        viewModel = createViewModel()

        viewModel.state.test {
            assertEquals(
                Estimated1rmCalculator.Confidence.MODERATE,
                awaitItem().chartData.first().confidence,
            )
        }
    }

    @Test
    fun `confidence is LOW for best 1RM from 15-rep set`() = runTest {
        setupSingleSessionSets(listOf(completedSet(1, 1, 60.0, 15)))
        viewModel = createViewModel()

        viewModel.state.test {
            assertEquals(Estimated1rmCalculator.Confidence.LOW, awaitItem().chartData.first().confidence)
        }
    }

    @Test
    fun `21-plus rep sets produce null estimated 1RM`() = runTest {
        setupSingleSessionSets(listOf(completedSet(1, 1, 40.0, 25)))
        viewModel = createViewModel()

        viewModel.state.test {
            val dataPoint = awaitItem().chartData.first()
            assertEquals(40.0, dataPoint.weightKg, 0.01)
            assertNull(dataPoint.estimated1rmKg)
            assertNull(dataPoint.confidence)
        }
    }

    @Test
    fun `single rep set has estimated 1RM equal to weight`() = runTest {
        setupSingleSessionSets(listOf(completedSet(1, 1, 120.0, 1)))
        viewModel = createViewModel()

        viewModel.state.test {
            val dataPoint = awaitItem().chartData.first()
            assertEquals(120.0, dataPoint.estimated1rmKg!!, 0.01)
            assertEquals(120.0, dataPoint.weightKg, 0.01)
            assertEquals(Estimated1rmCalculator.Confidence.HIGH, dataPoint.confidence)
        }
    }

    @Test
    fun `both weight and estimated 1RM populated when valid sets exist`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            // Default test data: session1Sets has 80kg x 8 and 85kg x 6
            // Session 2 has 90kg x 5
            for (dataPoint in state.chartData) {
                assertNotNull(dataPoint.weightKg)
                assertNotNull(dataPoint.estimated1rmKg)
                assertNotNull(dataPoint.confidence)
            }
        }
    }

    // --- Bodyweight Exercise 1RM ---

    private val bwExercise = testExercise.copy(
        id = 200L,
        stableId = "core_bodyweight_pullup",
        name = "Pull-Up",
        equipment = Equipment.BODYWEIGHT,
    )

    private val bwSession = WorkoutSession(
        id = 3L,
        startedAt = now - oneWeekMs,
        completedAt = now - oneWeekMs + 3_600_000,
        durationSeconds = 3600L,
        pausedDurationSeconds = 0L,
        status = SessionStatus.COMPLETED,
        notes = null,
        templateId = null,
    )

    private val bwWorkoutExercises = listOf(
        WorkoutExercise(
            id = 30L,
            sessionId = 3L,
            exerciseId = 200L,
            orderIndex = 0,
            supersetGroupId = null,
            restTimerSeconds = 60,
            notes = null,
        ),
    )

    private val bwSets = listOf(
        WorkoutSet(
            id = 10,
            setNumber = 1,
            type = SetType.WORKING,
            status = SetStatus.COMPLETED,
            plannedWeightKg = null,
            plannedReps = 8,
            actualWeightKg = null,
            actualReps = 8,
        ),
    )

    private fun setupBodyweightMocks(profile: UserProfile?) {
        every { workoutSessionRepository.getCompletedSessions() } returns flowOf(listOf(bwSession))
        every { workoutSessionRepository.getExercisesForSession(3L) } returns flowOf(bwWorkoutExercises)
        every { workoutSessionRepository.getSetsForExercise(30L) } returns flowOf(bwSets)
        coEvery { exerciseRepository.getExerciseById(200L) } returns bwExercise
        coEvery { userProfileRepository.get() } returns profile
    }

    @Test
    fun `bodyweight exercise with profile weight uses body weight for 1RM`() = runTest {
        val profile = UserProfile(
            id = 1L, experienceLevel = ExperienceLevel.INTERMEDIATE,
            preferredUnit = WeightUnit.KG, age = 30, heightCm = 180.0,
            gender = null, bodyWeightKg = 70.0,
            compoundRepMin = 6, compoundRepMax = 10,
            isolationRepMin = 10, isolationRepMax = 15,
            createdAt = now, updatedAt = now,
        )
        setupBodyweightMocks(profile)

        viewModel = createViewModel(exerciseId = 200L)

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isBodyweightMissingProfile)
            val dataPoint = state.chartData.first()
            val expected1rm = Estimated1rmCalculator.epley(70.0, 8)
            assertNotNull(expected1rm)
            assertEquals(expected1rm!!, dataPoint.estimated1rmKg!!, 0.01)
        }
    }

    @Test
    fun `bodyweight exercise without profile weight has null 1RM`() = runTest {
        setupBodyweightMocks(profile = null)

        viewModel = createViewModel(exerciseId = 200L)

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.isBodyweightMissingProfile)
            assertTrue(state.chartData.isEmpty())
        }
    }

    // --- 1RM Summary Fields ---

    @Test
    fun `current and all-time best estimated 1RM are computed`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            // Session 1: sets at 80x8, 85x6
            //   80*8: Epley = 80*(1+8/30) = 101.33
            //   85*6: Epley = 85*(1+6/30) = 102.0
            //   Best 1RM = 102.0
            // Session 2: set at 90x5
            //   90*5: Epley = 90*(1+5/30) = 105.0
            //   Best 1RM = 105.0
            // current = last data point = 105.0
            // allTime = max = 105.0
            assertNotNull(state.currentBestEstimated1rmKg)
            assertNotNull(state.allTimeBestEstimated1rmKg)
            assertEquals(105.0, state.currentBestEstimated1rmKg!!, 0.01)
            assertEquals(105.0, state.allTimeBestEstimated1rmKg!!, 0.01)
        }
    }

    // --- Companion: computeBest1rm ---

    @Test
    fun `computeBest1rm returns null for bodyweight without body weight`() {
        val sets = listOf(
            WorkoutSet(
                id = 1,
                setNumber = 1,
                type = SetType.WORKING,
                status = SetStatus.COMPLETED,
                plannedWeightKg = null,
                plannedReps = 8,
                actualWeightKg = null,
                actualReps = 8,
            ),
        )
        assertNull(ExerciseProgressViewModel.computeBest1rm(sets, isBodyweight = true, bodyWeightKg = null))
    }

    @Test
    fun `computeBest1rm for non-bodyweight uses actual weight`() {
        val sets = listOf(
            WorkoutSet(
                id = 1,
                setNumber = 1,
                type = SetType.WORKING,
                status = SetStatus.COMPLETED,
                plannedWeightKg = 80.0,
                plannedReps = 5,
                actualWeightKg = 80.0,
                actualReps = 5,
            ),
        )
        val result = ExerciseProgressViewModel.computeBest1rm(sets, isBodyweight = false, bodyWeightKg = null)
        assertNotNull(result)
        // Epley: 80 * (1 + 5/30) = 93.33
        assertEquals(93.33, result!!.estimatedKg, 0.01)
        assertEquals(Estimated1rmCalculator.Confidence.HIGH, result.confidence)
    }

    @Test
    fun `computeBest1rm excludes 21-plus rep sets`() {
        val sets = listOf(
            WorkoutSet(
                id = 1,
                setNumber = 1,
                type = SetType.WORKING,
                status = SetStatus.COMPLETED,
                plannedWeightKg = 40.0,
                plannedReps = 25,
                actualWeightKg = 40.0,
                actualReps = 25,
            ),
        )
        assertNull(ExerciseProgressViewModel.computeBest1rm(sets, isBodyweight = false, bodyWeightKg = null))
    }

    @Test
    fun `computeBest1rm picks highest estimate across multiple sets`() {
        val sets = listOf(
            WorkoutSet(
                id = 1,
                setNumber = 1,
                type = SetType.WORKING,
                status = SetStatus.COMPLETED,
                plannedWeightKg = 80.0,
                plannedReps = 5,
                actualWeightKg = 80.0,
                actualReps = 5,
            ),
            WorkoutSet(
                id = 2,
                setNumber = 2,
                type = SetType.WORKING,
                status = SetStatus.COMPLETED,
                plannedWeightKg = 70.0,
                plannedReps = 10,
                actualWeightKg = 70.0,
                actualReps = 10,
            ),
        )
        val result = ExerciseProgressViewModel.computeBest1rm(sets, isBodyweight = false, bodyWeightKg = null)
        assertNotNull(result)
        // 80*(1+5/30) = 93.33, 70*(1+10/30) = 93.33 -> tied, either is fine
        assertEquals(93.33, result!!.estimatedKg, 0.01)
    }
}
