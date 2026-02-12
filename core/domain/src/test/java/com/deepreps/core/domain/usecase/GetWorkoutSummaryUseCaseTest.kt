package com.deepreps.core.domain.usecase

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
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetWorkoutSummaryUseCaseTest {

    private lateinit var workoutSessionRepository: WorkoutSessionRepository
    private lateinit var exerciseRepository: ExerciseRepository
    private lateinit var useCase: GetWorkoutSummaryUseCase

    private val now = System.currentTimeMillis()

    private val testSession = WorkoutSession(
        id = 1L,
        startedAt = now - 3_600_000,
        completedAt = now,
        durationSeconds = 3600L,
        pausedDurationSeconds = 0L,
        status = SessionStatus.COMPLETED,
        notes = null,
        templateId = null,
    )

    private val testExercises = listOf(
        WorkoutExercise(
            id = 10L, sessionId = 1L, exerciseId = 100L,
            orderIndex = 0, supersetGroupId = null, restTimerSeconds = 120, notes = null,
        ),
        WorkoutExercise(
            id = 11L, sessionId = 1L, exerciseId = 101L,
            orderIndex = 1, supersetGroupId = null, restTimerSeconds = 90, notes = null,
        ),
    )

    // Exercise 100: Bench Press (Chest = group 3)
    private val benchPressSets = listOf(
        WorkoutSet(
            id = 1, setNumber = 1, type = SetType.WARMUP, status = SetStatus.COMPLETED,
            plannedWeightKg = 40.0, plannedReps = 12, actualWeightKg = 40.0, actualReps = 12,
        ),
        WorkoutSet(
            id = 2, setNumber = 2, type = SetType.WORKING, status = SetStatus.COMPLETED,
            plannedWeightKg = 80.0, plannedReps = 8, actualWeightKg = 80.0, actualReps = 8,
        ),
        WorkoutSet(
            id = 3, setNumber = 3, type = SetType.WORKING, status = SetStatus.COMPLETED,
            plannedWeightKg = 80.0, plannedReps = 8, actualWeightKg = 85.0, actualReps = 6,
        ),
        WorkoutSet(
            id = 4, setNumber = 4, type = SetType.WORKING, status = SetStatus.PLANNED,
            plannedWeightKg = 80.0, plannedReps = 8, actualWeightKg = null, actualReps = null,
        ),
    )

    // Exercise 101: Barbell Row (Back = group 4)
    private val rowSets = listOf(
        WorkoutSet(
            id = 5, setNumber = 1, type = SetType.WORKING, status = SetStatus.COMPLETED,
            plannedWeightKg = 60.0, plannedReps = 10, actualWeightKg = 60.0, actualReps = 10,
        ),
        WorkoutSet(
            id = 6, setNumber = 2, type = SetType.WORKING, status = SetStatus.COMPLETED,
            plannedWeightKg = 60.0, plannedReps = 10, actualWeightKg = 60.0, actualReps = 10,
        ),
    )

    private val benchPress = Exercise(
        id = 100L, stableId = "chest_bench_press", name = "Bench Press",
        description = "", equipment = Equipment.BARBELL,
        movementType = MovementType.COMPOUND, difficulty = Difficulty.INTERMEDIATE,
        primaryGroupId = 3L, secondaryMuscles = emptyList(), tips = emptyList(),
        pros = emptyList(), displayOrder = 1, orderPriority = 1,
        supersetTags = emptyList(), autoProgramMinLevel = 1,
    )

    private val barbellRow = Exercise(
        id = 101L, stableId = "back_barbell_row", name = "Barbell Row",
        description = "", equipment = Equipment.BARBELL,
        movementType = MovementType.COMPOUND, difficulty = Difficulty.INTERMEDIATE,
        primaryGroupId = 4L, secondaryMuscles = emptyList(), tips = emptyList(),
        pros = emptyList(), displayOrder = 2, orderPriority = 2,
        supersetTags = emptyList(), autoProgramMinLevel = 1,
    )

    @BeforeEach
    fun setUp() {
        workoutSessionRepository = mockk(relaxed = true)
        exerciseRepository = mockk(relaxed = true)
        useCase = GetWorkoutSummaryUseCase(workoutSessionRepository, exerciseRepository)

        coEvery { workoutSessionRepository.getSession(1L) } returns testSession
        every { workoutSessionRepository.getExercisesForSession(1L) } returns
            flowOf(testExercises)
        every { workoutSessionRepository.getSetsForExercise(10L) } returns flowOf(benchPressSets)
        every { workoutSessionRepository.getSetsForExercise(11L) } returns flowOf(rowSets)
        coEvery { exerciseRepository.getExercisesByIds(listOf(100L, 101L)) } returns
            listOf(benchPress, barbellRow)
    }

    @Test
    fun `returns null for non-existent session`() = runTest {
        coEvery { workoutSessionRepository.getSession(99L) } returns null

        val result = useCase(99L)

        assertNull(result)
    }

    @Test
    fun `returns summary with correct session ID and duration`() = runTest {
        val result = useCase(1L)

        assertNotNull(result)
        assertEquals(1L, result!!.sessionId)
        assertEquals(3600L, result.durationSeconds)
    }

    @Test
    fun `returns correct exercise count`() = runTest {
        val result = useCase(1L)

        assertEquals(2, result!!.exerciseCount)
    }

    @Test
    fun `counts only completed working sets for total`() = runTest {
        val result = useCase(1L)

        // Bench: 2 completed working sets (warmup excluded, planned excluded)
        // Row: 2 completed working sets
        // Total: 4
        assertEquals(4, result!!.totalWorkingSets)
    }

    @Test
    fun `calculates tonnage from completed working sets only`() = runTest {
        val result = useCase(1L)

        // Bench working sets: 80*8 + 85*6 = 640 + 510 = 1150 (warmup 40*12 excluded)
        // Row working sets: 60*10 + 60*10 = 600 + 600 = 1200
        // Total: 1150 + 1200 = 2350
        assertEquals(2350.0, result!!.totalTonnageKg, 0.01)
    }

    @Test
    fun `per-group volume breakdown is correct`() = runTest {
        val result = useCase(1L)

        assertEquals(2, result!!.perGroupVolume.size)

        val backGroup = result.perGroupVolume.find { it.groupName == "Back" }
        assertNotNull(backGroup)
        assertEquals(2, backGroup!!.workingSets)
        assertEquals(1200.0, backGroup.tonnageKg, 0.01)

        val chestGroup = result.perGroupVolume.find { it.groupName == "Chest" }
        assertNotNull(chestGroup)
        assertEquals(2, chestGroup!!.workingSets)
        assertEquals(1150.0, chestGroup.tonnageKg, 0.01)
    }

    @Test
    fun `empty exercises returns zero volume`() = runTest {
        every { workoutSessionRepository.getExercisesForSession(1L) } returns flowOf(emptyList())

        val result = useCase(1L)

        assertNotNull(result)
        assertEquals(0, result!!.exerciseCount)
        assertEquals(0, result.totalWorkingSets)
        assertEquals(0.0, result.totalTonnageKg, 0.01)
        assertTrue(result.perGroupVolume.isEmpty())
    }

    @Test
    fun `personal records list is empty from use case`() = runTest {
        val result = useCase(1L)

        // PRs are calculated separately by CalculatePersonalRecordsUseCase
        assertTrue(result!!.personalRecords.isEmpty())
    }

    @Test
    fun `session with null duration uses zero`() = runTest {
        val sessionNoDuration = testSession.copy(durationSeconds = null)
        coEvery { workoutSessionRepository.getSession(1L) } returns sessionNoDuration

        val result = useCase(1L)

        assertEquals(0L, result!!.durationSeconds)
    }

    // --- Companion Helpers ---

    @Test
    fun `muscleGroupNameFromId maps correctly`() {
        assertEquals("Legs", GetWorkoutSummaryUseCase.muscleGroupNameFromId(1L))
        assertEquals("Chest", GetWorkoutSummaryUseCase.muscleGroupNameFromId(3L))
        assertEquals("Back", GetWorkoutSummaryUseCase.muscleGroupNameFromId(4L))
        assertNull(GetWorkoutSummaryUseCase.muscleGroupNameFromId(0L))
        assertNull(GetWorkoutSummaryUseCase.muscleGroupNameFromId(99L))
    }
}
