package com.deepreps.core.domain.usecase

import com.deepreps.core.domain.model.Exercise
import com.deepreps.core.domain.model.PersonalRecord
import com.deepreps.core.domain.model.WorkoutExercise
import com.deepreps.core.domain.model.WorkoutSet
import com.deepreps.core.domain.model.enums.Difficulty
import com.deepreps.core.domain.model.enums.Equipment
import com.deepreps.core.domain.model.enums.MovementType
import com.deepreps.core.domain.model.enums.RecordType
import com.deepreps.core.domain.model.enums.SetStatus
import com.deepreps.core.domain.model.enums.SetType
import com.deepreps.core.domain.repository.ExerciseRepository
import com.deepreps.core.domain.repository.PersonalRecordRepository
import com.deepreps.core.domain.repository.WorkoutSessionRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CalculatePersonalRecordsUseCaseTest {

    private lateinit var workoutSessionRepository: WorkoutSessionRepository
    private lateinit var exerciseRepository: ExerciseRepository
    private lateinit var personalRecordRepository: PersonalRecordRepository
    private lateinit var useCase: CalculatePersonalRecordsUseCase

    private val now = System.currentTimeMillis()

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
            plannedWeightKg = 80.0, plannedReps = 8, actualWeightKg = 100.0, actualReps = 3,
        ),
    )

    private val rowSets = listOf(
        WorkoutSet(
            id = 4, setNumber = 1, type = SetType.WORKING, status = SetStatus.COMPLETED,
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
        personalRecordRepository = mockk(relaxed = true)
        useCase = CalculatePersonalRecordsUseCase(
            workoutSessionRepository, exerciseRepository, personalRecordRepository,
        )

        every { workoutSessionRepository.getExercisesForSession(1L) } returns
            flowOf(testExercises)
        every { workoutSessionRepository.getSetsForExercise(10L) } returns flowOf(benchPressSets)
        every { workoutSessionRepository.getSetsForExercise(11L) } returns flowOf(rowSets)
        coEvery { exerciseRepository.getExerciseById(100L) } returns benchPress
        coEvery { exerciseRepository.getExerciseById(101L) } returns barbellRow
        coEvery { personalRecordRepository.insertAll(any()) } just Runs
    }

    // --- No Previous Records ---

    @Test
    fun `detects PR when no previous records exist`() = runTest {
        coEvery {
            personalRecordRepository.getBestByType(any(), RecordType.MAX_WEIGHT.value)
        } returns null

        val result = useCase(1L)

        assertEquals(2, result.size)

        val benchPr = result.find { it.exerciseId == 100L }
        assertEquals(100.0, benchPr!!.weightKg, 0.01)
        assertEquals(3, benchPr.reps)
        assertEquals(RecordType.MAX_WEIGHT, benchPr.recordType)
        assertEquals("Bench Press", benchPr.exerciseName)

        val rowPr = result.find { it.exerciseId == 101L }
        assertEquals(60.0, rowPr!!.weightKg, 0.01)
    }

    @Test
    fun `inserts records into repository`() = runTest {
        coEvery {
            personalRecordRepository.getBestByType(any(), RecordType.MAX_WEIGHT.value)
        } returns null

        useCase(1L)

        coVerify {
            personalRecordRepository.insertAll(match { records ->
                records.size == 2 &&
                    records.all { it.recordType == RecordType.MAX_WEIGHT } &&
                    records.all { it.sessionId == 1L }
            })
        }
    }

    // --- Existing Records ---

    @Test
    fun `detects PR when new weight exceeds existing record`() = runTest {
        val existingBenchPr = PersonalRecord(
            id = 1L, exerciseId = 100L, recordType = RecordType.MAX_WEIGHT,
            weightValue = 90.0, reps = 5, estimated1rm = null,
            achievedAt = now - 86_400_000, sessionId = null,
        )
        coEvery {
            personalRecordRepository.getBestByType(100L, RecordType.MAX_WEIGHT.value)
        } returns existingBenchPr

        // Row has no previous record
        coEvery {
            personalRecordRepository.getBestByType(101L, RecordType.MAX_WEIGHT.value)
        } returns null

        val result = useCase(1L)

        // Bench: 100.0 > 90.0 -> PR
        // Row: no previous -> PR
        assertEquals(2, result.size)
        assertTrue(result.any { it.exerciseId == 100L && it.weightKg == 100.0 })
    }

    @Test
    fun `does not detect PR when weight equals existing record`() = runTest {
        val existingBenchPr = PersonalRecord(
            id = 1L, exerciseId = 100L, recordType = RecordType.MAX_WEIGHT,
            weightValue = 100.0, reps = 5, estimated1rm = null,
            achievedAt = now - 86_400_000, sessionId = null,
        )
        coEvery {
            personalRecordRepository.getBestByType(100L, RecordType.MAX_WEIGHT.value)
        } returns existingBenchPr

        coEvery {
            personalRecordRepository.getBestByType(101L, RecordType.MAX_WEIGHT.value)
        } returns null

        val result = useCase(1L)

        // Bench: 100.0 == 100.0 -> NOT a PR
        // Row: no previous -> PR
        assertEquals(1, result.size)
        assertEquals(101L, result[0].exerciseId)
    }

    @Test
    fun `does not detect PR when weight is below existing record`() = runTest {
        val existingBenchPr = PersonalRecord(
            id = 1L, exerciseId = 100L, recordType = RecordType.MAX_WEIGHT,
            weightValue = 120.0, reps = 3, estimated1rm = null,
            achievedAt = now - 86_400_000, sessionId = null,
        )
        val existingRowPr = PersonalRecord(
            id = 2L, exerciseId = 101L, recordType = RecordType.MAX_WEIGHT,
            weightValue = 80.0, reps = 8, estimated1rm = null,
            achievedAt = now - 86_400_000, sessionId = null,
        )
        coEvery {
            personalRecordRepository.getBestByType(100L, RecordType.MAX_WEIGHT.value)
        } returns existingBenchPr
        coEvery {
            personalRecordRepository.getBestByType(101L, RecordType.MAX_WEIGHT.value)
        } returns existingRowPr

        val result = useCase(1L)

        // Bench: 100.0 < 120.0 -> NOT a PR
        // Row: 60.0 < 80.0 -> NOT a PR
        assertTrue(result.isEmpty())
    }

    @Test
    fun `does not insert records when no PRs detected`() = runTest {
        val existingBenchPr = PersonalRecord(
            id = 1L, exerciseId = 100L, recordType = RecordType.MAX_WEIGHT,
            weightValue = 120.0, reps = 3, estimated1rm = null,
            achievedAt = now - 86_400_000, sessionId = null,
        )
        val existingRowPr = PersonalRecord(
            id = 2L, exerciseId = 101L, recordType = RecordType.MAX_WEIGHT,
            weightValue = 80.0, reps = 8, estimated1rm = null,
            achievedAt = now - 86_400_000, sessionId = null,
        )
        coEvery {
            personalRecordRepository.getBestByType(100L, RecordType.MAX_WEIGHT.value)
        } returns existingBenchPr
        coEvery {
            personalRecordRepository.getBestByType(101L, RecordType.MAX_WEIGHT.value)
        } returns existingRowPr

        useCase(1L)

        coVerify(exactly = 0) { personalRecordRepository.insertAll(any()) }
    }

    // --- Edge Cases ---

    @Test
    fun `empty exercises returns empty list`() = runTest {
        every { workoutSessionRepository.getExercisesForSession(1L) } returns flowOf(emptyList())

        val result = useCase(1L)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `exercises with only warmup sets are skipped`() = runTest {
        val warmupOnlySets = listOf(
            WorkoutSet(
                id = 1, setNumber = 1, type = SetType.WARMUP, status = SetStatus.COMPLETED,
                plannedWeightKg = 40.0, plannedReps = 12, actualWeightKg = 40.0, actualReps = 12,
            ),
        )
        every { workoutSessionRepository.getSetsForExercise(10L) } returns flowOf(warmupOnlySets)
        every { workoutSessionRepository.getExercisesForSession(1L) } returns
            flowOf(listOf(testExercises[0]))

        coEvery {
            personalRecordRepository.getBestByType(any(), RecordType.MAX_WEIGHT.value)
        } returns null

        val result = useCase(1L)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `exercises with only planned sets are skipped`() = runTest {
        val plannedOnlySets = listOf(
            WorkoutSet(
                id = 1, setNumber = 1, type = SetType.WORKING, status = SetStatus.PLANNED,
                plannedWeightKg = 100.0, plannedReps = 5, actualWeightKg = null, actualReps = null,
            ),
        )
        every { workoutSessionRepository.getSetsForExercise(10L) } returns flowOf(plannedOnlySets)
        every { workoutSessionRepository.getExercisesForSession(1L) } returns
            flowOf(listOf(testExercises[0]))

        val result = useCase(1L)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `only heaviest completed working set is compared for PR`() = runTest {
        coEvery {
            personalRecordRepository.getBestByType(100L, RecordType.MAX_WEIGHT.value)
        } returns null
        coEvery {
            personalRecordRepository.getBestByType(101L, RecordType.MAX_WEIGHT.value)
        } returns null

        val result = useCase(1L)

        val benchPr = result.find { it.exerciseId == 100L }
        // 100.0 > 80.0, so the PR should be at 100.0kg
        assertEquals(100.0, benchPr!!.weightKg, 0.01)
    }
}
