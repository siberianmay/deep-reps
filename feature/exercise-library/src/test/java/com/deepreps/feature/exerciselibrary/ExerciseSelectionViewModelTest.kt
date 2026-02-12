package com.deepreps.feature.exerciselibrary

import app.cash.turbine.test
import com.deepreps.core.domain.model.enums.Equipment
import com.deepreps.core.domain.model.enums.MuscleGroup
import com.deepreps.core.domain.repository.ExerciseRepository
import com.deepreps.feature.exerciselibrary.ExerciseListViewModelTest.Companion.makeExercise
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExerciseSelectionViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var repository: ExerciseRepository
    private lateinit var viewModel: ExerciseSelectionViewModel

    private val chestExercises = listOf(
        makeExercise(id = 1, name = "Bench Press", groupId = 3, equipment = Equipment.BARBELL),
        makeExercise(id = 2, name = "Incline Dumbbell Press", groupId = 3, equipment = Equipment.DUMBBELL),
        makeExercise(id = 3, name = "Cable Flye", groupId = 3, equipment = Equipment.CABLE),
    )

    private val backExercises = listOf(
        makeExercise(id = 20, name = "Pull-Up", groupId = 4, equipment = Equipment.BODYWEIGHT),
        makeExercise(id = 21, name = "Barbell Row", groupId = 4, equipment = Equipment.BARBELL),
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)

        every { repository.getExercisesByGroup(3L) } returns flowOf(chestExercises)
        every { repository.getExercisesByGroup(4L) } returns flowOf(backExercises)
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
    fun `initial state loads with chest and no selections`() = runTest {
        viewModel = ExerciseSelectionViewModel(repository)

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(MuscleGroup.CHEST, state.activeGroup)
            assertEquals(3, state.exercises.size)
            assertTrue(state.selectedExerciseIds.isEmpty())
            assertEquals(0, state.selectedCount)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `toggle exercise adds to selection`() = runTest {
        viewModel = ExerciseSelectionViewModel(repository)

        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(ExerciseSelectionIntent.ToggleExercise(1L))
            val state = awaitItem()
            assertTrue(1L in state.selectedExerciseIds)
            assertEquals(1, state.selectedCount)
        }
    }

    @Test
    fun `toggle exercise twice removes from selection`() = runTest {
        viewModel = ExerciseSelectionViewModel(repository)

        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(ExerciseSelectionIntent.ToggleExercise(1L))
            awaitItem() // selected

            viewModel.onIntent(ExerciseSelectionIntent.ToggleExercise(1L))
            val state = awaitItem()
            assertFalse(1L in state.selectedExerciseIds)
            assertEquals(0, state.selectedCount)
        }
    }

    @Test
    fun `toggle multiple exercises`() = runTest {
        viewModel = ExerciseSelectionViewModel(repository)

        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(ExerciseSelectionIntent.ToggleExercise(1L))
            awaitItem()

            viewModel.onIntent(ExerciseSelectionIntent.ToggleExercise(3L))
            val state = awaitItem()
            assertEquals(setOf(1L, 3L), state.selectedExerciseIds)
            assertEquals(2, state.selectedCount)
        }
    }

    @Test
    fun `selections persist across group switch`() = runTest {
        viewModel = ExerciseSelectionViewModel(repository)

        viewModel.state.test {
            awaitItem() // initial chest

            // Select exercise in chest group
            viewModel.onIntent(ExerciseSelectionIntent.ToggleExercise(1L))
            awaitItem()

            // Switch to back group
            viewModel.onIntent(ExerciseSelectionIntent.SelectGroup(MuscleGroup.BACK))
            val backState = awaitItem()
            assertEquals(MuscleGroup.BACK, backState.activeGroup)
            assertEquals(2, backState.exercises.size)
            // Selection from chest should persist
            assertTrue(1L in backState.selectedExerciseIds)
            assertEquals(1, backState.selectedCount)
        }
    }

    @Test
    fun `confirm selection emits side effect with selected IDs`() = runTest {
        viewModel = ExerciseSelectionViewModel(repository)

        // Wait for initial load
        viewModel.state.test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        // Select some exercises
        viewModel.onIntent(ExerciseSelectionIntent.ToggleExercise(1L))
        viewModel.onIntent(ExerciseSelectionIntent.ToggleExercise(2L))

        viewModel.sideEffect.test {
            viewModel.onIntent(ExerciseSelectionIntent.ConfirmSelection)

            val effect = awaitItem()
            assertTrue(effect is ExerciseSelectionSideEffect.SelectionConfirmed)
            val confirmed = effect as ExerciseSelectionSideEffect.SelectionConfirmed
            assertEquals(setOf(1L, 2L), confirmed.exerciseIds)
        }
    }

    @Test
    fun `confirm with empty selection emits empty set`() = runTest {
        viewModel = ExerciseSelectionViewModel(repository)

        viewModel.state.test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.sideEffect.test {
            viewModel.onIntent(ExerciseSelectionIntent.ConfirmSelection)

            val effect = awaitItem()
            assertTrue(effect is ExerciseSelectionSideEffect.SelectionConfirmed)
            val confirmed = effect as ExerciseSelectionSideEffect.SelectionConfirmed
            assertTrue(confirmed.exerciseIds.isEmpty())
        }
    }

    @Test
    fun `search filters exercises in current group`() = runTest {
        viewModel = ExerciseSelectionViewModel(repository)

        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(ExerciseSelectionIntent.Search("cable"))
            val state = awaitItem()
            assertEquals(1, state.exercises.size)
            assertEquals("Cable Flye", state.exercises.first().name)
        }
    }

    @Test
    fun `view detail emits navigation side effect`() = runTest {
        viewModel = ExerciseSelectionViewModel(repository)

        viewModel.sideEffect.test {
            viewModel.onIntent(ExerciseSelectionIntent.ViewDetail(2L))

            val effect = awaitItem()
            assertTrue(effect is ExerciseSelectionSideEffect.NavigateToDetail)
            assertEquals(2L, (effect as ExerciseSelectionSideEffect.NavigateToDetail).exerciseId)
        }
    }

    @Test
    fun `repository error sets error state`() = runTest {
        every { repository.getExercisesByGroup(3L) } returns flow {
            throw RuntimeException("DB error")
        }

        viewModel = ExerciseSelectionViewModel(repository)

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(ExerciseSelectionError.LoadFailed, state.errorType)
        }
    }
}
