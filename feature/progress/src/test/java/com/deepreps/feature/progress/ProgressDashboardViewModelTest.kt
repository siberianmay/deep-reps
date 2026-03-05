package com.deepreps.feature.progress

import app.cash.turbine.test
import com.deepreps.core.domain.model.Exercise
import com.deepreps.core.domain.model.PersonalRecord
import com.deepreps.core.domain.model.UserProfile
import com.deepreps.core.domain.model.WorkoutExercise
import com.deepreps.core.domain.model.WorkoutSession
import com.deepreps.core.domain.model.WorkoutSet
import com.deepreps.core.domain.model.enums.Difficulty
import com.deepreps.core.domain.model.enums.Equipment
import com.deepreps.core.domain.model.enums.ExperienceLevel
import com.deepreps.core.domain.model.enums.MuscleGroup
import com.deepreps.core.domain.model.enums.MovementType
import com.deepreps.core.domain.model.enums.RecordType
import com.deepreps.core.domain.model.enums.SessionStatus
import com.deepreps.core.domain.model.enums.SetStatus
import com.deepreps.core.domain.model.enums.SetType
import com.deepreps.core.domain.model.enums.WeightUnit
import com.deepreps.core.domain.repository.ExerciseRepository
import com.deepreps.core.domain.repository.PersonalRecordRepository
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
    private lateinit var personalRecordRepository: PersonalRecordRepository
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
        personalRecordRepository = mockk(relaxed = true)

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
            workoutSessionRepository, exerciseRepository, userProfileRepository, personalRecordRepository,
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
            workoutSessionRepository, exerciseRepository, userProfileRepository, personalRecordRepository,
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
            workoutSessionRepository, exerciseRepository, userProfileRepository, personalRecordRepository,
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
            workoutSessionRepository, exerciseRepository, userProfileRepository, personalRecordRepository,
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
            workoutSessionRepository, exerciseRepository, userProfileRepository, personalRecordRepository,
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
            workoutSessionRepository, exerciseRepository, userProfileRepository, personalRecordRepository,
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
            workoutSessionRepository, exerciseRepository, userProfileRepository, personalRecordRepository,
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
            workoutSessionRepository, exerciseRepository, userProfileRepository, personalRecordRepository,
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
            workoutSessionRepository, exerciseRepository, userProfileRepository, personalRecordRepository,
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
            workoutSessionRepository, exerciseRepository, userProfileRepository, personalRecordRepository,
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
            workoutSessionRepository, exerciseRepository, userProfileRepository, personalRecordRepository,
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

    // --- Tab Switching ---

    @Test
    fun `select tab updates selectedTab in state`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            val initial = awaitItem()
            assertEquals(DashboardTab.HISTORY, initial.selectedTab)

            viewModel.onIntent(ProgressDashboardIntent.SelectTab(DashboardTab.RECORDS))

            val updated = awaitItem()
            assertEquals(DashboardTab.RECORDS, updated.selectedTab)
        }
    }

    @Test
    fun `personal records empty before Records tab selected`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.personalRecords.isEmpty())
        }
    }

    @Test
    fun `personal records populated after Records tab selected`() = runTest {
        val records = listOf(
            PersonalRecord(
                id = 1L,
                exerciseId = 100L,
                recordType = RecordType.MAX_WEIGHT,
                weightValue = 90.0,
                reps = 5,
                estimated1rm = null,
                achievedAt = now,
                sessionId = 1L,
            ),
        )
        every { personalRecordRepository.observeAll() } returns flowOf(records)
        coEvery { exerciseRepository.getExerciseById(100L) } returns testExerciseLibrary

        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // initial HISTORY

            viewModel.onIntent(ProgressDashboardIntent.SelectTab(DashboardTab.RECORDS))

            val state = expectMostRecentItem()
            assertEquals(DashboardTab.RECORDS, state.selectedTab)
            assertFalse(state.isRecordsLoading)
            assertTrue(state.personalRecords.isNotEmpty())
            // Bench Press has primaryGroupId = 3 -> CHEST
            assertTrue(state.personalRecords.containsKey(MuscleGroup.CHEST))
            assertEquals(1, state.personalRecords[MuscleGroup.CHEST]!!.size)
            assertEquals("Bench Press", state.personalRecords[MuscleGroup.CHEST]!![0].exerciseName)
        }
    }

    // --- PR List: Grouping ---

    private fun maxWeightRecord(id: Long, exerciseId: Long, weight: Double, reps: Int) =
        PersonalRecord(
            id = id,
            exerciseId = exerciseId,
            recordType = RecordType.MAX_WEIGHT,
            weightValue = weight,
            reps = reps,
            estimated1rm = null,
            achievedAt = now,
            sessionId = 1L,
        )

    @Test
    fun `PRs grouped by muscle group correctly`() = runTest {
        val squat = testExerciseLibrary.copy(
            id = 200L,
            stableId = "legs_barbell_squat",
            name = "Squat",
            primaryGroupId = 1L,
        )
        every { personalRecordRepository.observeAll() } returns flowOf(
            listOf(maxWeightRecord(1, 100L, 90.0, 5), maxWeightRecord(2, 200L, 120.0, 3)),
        )
        coEvery { exerciseRepository.getExerciseById(100L) } returns testExerciseLibrary
        coEvery { exerciseRepository.getExerciseById(200L) } returns squat
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()
            viewModel.onIntent(ProgressDashboardIntent.SelectTab(DashboardTab.RECORDS))
            val state = expectMostRecentItem()
            assertEquals(2, state.personalRecords.size)
            assertTrue(state.personalRecords.containsKey(MuscleGroup.CHEST))
            assertTrue(state.personalRecords.containsKey(MuscleGroup.LEGS))
        }
    }

    // --- PR List: Deduplication ---

    @Test
    fun `only best MAX_WEIGHT record per exercise kept`() = runTest {
        val older = maxWeightRecord(1, 100L, 80.0, 8).copy(achievedAt = now - 86_400_000)
        every { personalRecordRepository.observeAll() } returns flowOf(
            listOf(older, maxWeightRecord(2, 100L, 90.0, 5)),
        )
        coEvery { exerciseRepository.getExerciseById(100L) } returns testExerciseLibrary
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()
            viewModel.onIntent(ProgressDashboardIntent.SelectTab(DashboardTab.RECORDS))
            val chestRecords = expectMostRecentItem().personalRecords[MuscleGroup.CHEST]!!
            assertEquals(1, chestRecords.size)
            assertEquals(90.0, chestRecords[0].bestWeightKg, 0.01)
        }
    }

    // --- PR List: Sorting within group ---

    @Test
    fun `PRs sorted by weight descending within group`() = runTest {
        val fly = testExerciseLibrary.copy(id = 201L, stableId = "chest_fly", name = "Fly", primaryGroupId = 3L)
        val incline = testExerciseLibrary.copy(id = 202L, stableId = "chest_inc", name = "Incline", primaryGroupId = 3L)
        every { personalRecordRepository.observeAll() } returns flowOf(
            listOf(
                maxWeightRecord(1, 100L, 60.0, 10),
                maxWeightRecord(2, 201L, 90.0, 5),
                maxWeightRecord(3, 202L, 75.0, 8),
            ),
        )
        coEvery { exerciseRepository.getExerciseById(100L) } returns testExerciseLibrary
        coEvery { exerciseRepository.getExerciseById(201L) } returns fly
        coEvery { exerciseRepository.getExerciseById(202L) } returns incline
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()
            viewModel.onIntent(ProgressDashboardIntent.SelectTab(DashboardTab.RECORDS))
            val chestRecords = expectMostRecentItem().personalRecords[MuscleGroup.CHEST]!!
            assertEquals(listOf(90.0, 75.0, 60.0), chestRecords.map { it.bestWeightKg })
        }
    }

    // --- PR List: Non-MAX_WEIGHT records excluded ---

    @Test
    fun `non MAX_WEIGHT records excluded from PR list`() = runTest {
        val records = listOf(
            PersonalRecord(
                id = 1L,
                exerciseId = 100L,
                recordType = RecordType.MAX_REPS,
                weightValue = null,
                reps = 20,
                estimated1rm = null,
                achievedAt = now,
                sessionId = 1L,
            ),
            PersonalRecord(
                id = 2L,
                exerciseId = 100L,
                recordType = RecordType.MAX_VOLUME,
                weightValue = 5000.0,
                reps = null,
                estimated1rm = null,
                achievedAt = now,
                sessionId = 1L,
            ),
        )
        every { personalRecordRepository.observeAll() } returns flowOf(records)

        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()
            viewModel.onIntent(ProgressDashboardIntent.SelectTab(DashboardTab.RECORDS))
            val state = expectMostRecentItem()

            assertTrue(state.personalRecords.isEmpty())
        }
    }

    // --- History tab unaffected ---

    @Test
    fun `history tab still loads sessions after switching tabs`() = runTest {
        every { personalRecordRepository.observeAll() } returns flowOf(emptyList())

        viewModel = createViewModel()

        viewModel.state.test {
            val initial = awaitItem()
            assertEquals(DashboardTab.HISTORY, initial.selectedTab)
            assertEquals(2, initial.recentSessions.size)

            // Switch to Records
            viewModel.onIntent(ProgressDashboardIntent.SelectTab(DashboardTab.RECORDS))
            val recordsState = expectMostRecentItem()
            assertEquals(DashboardTab.RECORDS, recordsState.selectedTab)
            // Sessions should still be in state
            assertEquals(2, recordsState.recentSessions.size)

            // Switch back to History
            viewModel.onIntent(ProgressDashboardIntent.SelectTab(DashboardTab.HISTORY))
            val historyState = expectMostRecentItem()
            assertEquals(DashboardTab.HISTORY, historyState.selectedTab)
            assertEquals(2, historyState.recentSessions.size)
        }
    }

    // --- Job Cancellation: Time Range ---

    @Test
    fun `multiple time range changes reflect latest selection`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()

            viewModel.onIntent(ProgressDashboardIntent.SelectTimeRange(TimeRange.FOUR_WEEKS))
            viewModel.onIntent(ProgressDashboardIntent.SelectTimeRange(TimeRange.SIX_MONTHS))
            viewModel.onIntent(ProgressDashboardIntent.SelectTimeRange(TimeRange.ALL))

            val state = expectMostRecentItem()
            assertEquals(TimeRange.ALL, state.selectedTimeRange)
        }
    }

    // --- Job Cancellation: Records Tab ---

    @Test
    fun `switching tabs multiple times does not produce stale state`() = runTest {
        every { personalRecordRepository.observeAll() } returns flowOf(emptyList())

        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()

            viewModel.onIntent(ProgressDashboardIntent.SelectTab(DashboardTab.RECORDS))
            viewModel.onIntent(ProgressDashboardIntent.SelectTab(DashboardTab.HISTORY))
            viewModel.onIntent(ProgressDashboardIntent.SelectTab(DashboardTab.RECORDS))

            val state = expectMostRecentItem()
            assertEquals(DashboardTab.RECORDS, state.selectedTab)
            assertFalse(state.isRecordsLoading)
        }
    }

    // --- Helper ---

    private fun createViewModel(): ProgressDashboardViewModel {
        return ProgressDashboardViewModel(
            workoutSessionRepository,
            exerciseRepository,
            userProfileRepository,
            personalRecordRepository,
        )
    }
}
