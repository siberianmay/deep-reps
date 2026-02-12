package com.deepreps.feature.exerciselibrary

import app.cash.turbine.test
import com.deepreps.core.domain.model.Exercise
import com.deepreps.core.domain.model.enums.Difficulty
import com.deepreps.core.domain.model.enums.Equipment
import com.deepreps.core.domain.model.enums.MuscleGroup
import com.deepreps.core.domain.model.enums.MovementType
import com.deepreps.core.domain.repository.ExerciseRepository
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
class ExerciseListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var repository: ExerciseRepository
    private lateinit var viewModel: ExerciseListViewModel

    private val chestExercises = listOf(
        makeExercise(id = 1, name = "Bench Press", groupId = 3, equipment = Equipment.BARBELL),
        makeExercise(id = 2, name = "Incline Dumbbell Press", groupId = 3, equipment = Equipment.DUMBBELL),
        makeExercise(id = 3, name = "Cable Flye", groupId = 3, equipment = Equipment.CABLE),
    )

    private val legExercises = listOf(
        makeExercise(id = 10, name = "Barbell Squat", groupId = 1, equipment = Equipment.BARBELL),
        makeExercise(id = 11, name = "Leg Press", groupId = 1, equipment = Equipment.MACHINE),
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)

        // Default: return chest exercises for group ID 3 (chest = ordinal 2, +1 = 3)
        every { repository.getExercisesByGroup(3L) } returns flowOf(chestExercises)
        every { repository.getExercisesByGroup(1L) } returns flowOf(legExercises)
        every { repository.getMuscleGroups() } returns flowOf(emptyList())
        every { repository.getAllExercises() } returns flowOf(emptyList())
        every { repository.searchExercises(any()) } returns flowOf(emptyList())
        every { repository.getExercisesWithMuscles(any()) } returns flowOf(emptyList())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads chest exercises`() = runTest {
        viewModel = ExerciseListViewModel(repository)

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(MuscleGroup.CHEST, state.selectedGroup)
            assertEquals(3, state.exercises.size)
            assertFalse(state.isLoading)
            assertNull(state.errorType)
        }
    }

    @Test
    fun `select group switches to legs exercises`() = runTest {
        viewModel = ExerciseListViewModel(repository)

        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onIntent(ExerciseListIntent.SelectGroup(MuscleGroup.LEGS))

            val state = awaitItem()
            assertEquals(MuscleGroup.LEGS, state.selectedGroup)
            assertEquals(2, state.exercises.size)
            assertEquals("Barbell Squat", state.exercises.first().name)
        }
    }

    @Test
    fun `search filters exercises by name`() = runTest {
        viewModel = ExerciseListViewModel(repository)

        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onIntent(ExerciseListIntent.Search("bench"))

            val state = awaitItem()
            assertEquals("bench", state.searchQuery)
            assertEquals(1, state.exercises.size)
            assertEquals("Bench Press", state.exercises.first().name)
        }
    }

    @Test
    fun `clear search shows all exercises for group`() = runTest {
        viewModel = ExerciseListViewModel(repository)

        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(ExerciseListIntent.Search("bench"))
            awaitItem() // filtered

            viewModel.onIntent(ExerciseListIntent.ClearSearch)
            val state = awaitItem()
            assertEquals("", state.searchQuery)
            assertEquals(3, state.exercises.size)
        }
    }

    @Test
    fun `search with no matches shows empty list`() = runTest {
        viewModel = ExerciseListViewModel(repository)

        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(ExerciseListIntent.Search("zzzzz"))
            val state = awaitItem()
            assertTrue(state.exercises.isEmpty())
        }
    }

    @Test
    fun `navigate to detail emits side effect`() = runTest {
        viewModel = ExerciseListViewModel(repository)

        viewModel.sideEffect.test {
            viewModel.onIntent(ExerciseListIntent.NavigateToDetail(1L))

            val effect = awaitItem()
            assertTrue(effect is ExerciseListSideEffect.NavigateToDetail)
            assertEquals(1L, (effect as ExerciseListSideEffect.NavigateToDetail).exerciseId)
        }
    }

    @Test
    fun `repository error sets error state`() = runTest {
        every { repository.getExercisesByGroup(3L) } returns flow {
            throw IllegalStateException("DB error")
        }

        viewModel = ExerciseListViewModel(repository)

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(ExerciseListError.LoadFailed, state.errorType)
        }
    }

    @Test
    fun `groupIdFor maps enum ordinals correctly`() {
        assertEquals(1L, ExerciseListViewModel.groupIdFor(MuscleGroup.LEGS))
        assertEquals(2L, ExerciseListViewModel.groupIdFor(MuscleGroup.LOWER_BACK))
        assertEquals(3L, ExerciseListViewModel.groupIdFor(MuscleGroup.CHEST))
        assertEquals(4L, ExerciseListViewModel.groupIdFor(MuscleGroup.BACK))
        assertEquals(5L, ExerciseListViewModel.groupIdFor(MuscleGroup.SHOULDERS))
        assertEquals(6L, ExerciseListViewModel.groupIdFor(MuscleGroup.ARMS))
        assertEquals(7L, ExerciseListViewModel.groupIdFor(MuscleGroup.CORE))
    }

    companion object {
        fun makeExercise(
            id: Long = 1,
            name: String = "Test Exercise",
            groupId: Long = 1,
            equipment: Equipment = Equipment.BARBELL,
            difficulty: Difficulty = Difficulty.INTERMEDIATE,
            movementType: MovementType = MovementType.COMPOUND,
        ): Exercise = Exercise(
            id = id,
            stableId = "test_${name.lowercase().replace(" ", "_")}",
            name = name,
            description = "Test description for $name",
            equipment = equipment,
            movementType = movementType,
            difficulty = difficulty,
            primaryGroupId = groupId,
            secondaryMuscles = listOf("Core"),
            tips = listOf("Keep your back straight"),
            pros = listOf("Full body engagement"),
            displayOrder = id.toInt(),
            orderPriority = id.toInt(),
            supersetTags = emptyList(),
            autoProgramMinLevel = 1,
        )
    }
}
