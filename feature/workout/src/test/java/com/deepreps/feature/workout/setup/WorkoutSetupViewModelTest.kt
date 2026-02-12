package com.deepreps.feature.workout.setup

import app.cash.turbine.test
import com.deepreps.core.domain.model.Exercise
import com.deepreps.core.domain.model.Template
import com.deepreps.core.domain.model.TemplateExercise
import com.deepreps.core.domain.model.enums.Difficulty
import com.deepreps.core.domain.model.enums.Equipment
import com.deepreps.core.domain.model.enums.MovementType
import com.deepreps.core.domain.model.enums.MuscleGroup
import com.deepreps.core.domain.repository.ExerciseRepository
import com.deepreps.core.domain.repository.TemplateRepository
import com.deepreps.core.domain.usecase.OrderExercisesUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class WorkoutSetupViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var exerciseRepository: ExerciseRepository
    private lateinit var templateRepository: TemplateRepository
    private lateinit var orderExercisesUseCase: OrderExercisesUseCase
    private lateinit var viewModel: WorkoutSetupViewModel

    private val testExercises = listOf(
        makeExercise(id = 1, name = "Bench Press", groupId = 3, orderPriority = 1),
        makeExercise(id = 2, name = "Incline DB Press", groupId = 3, orderPriority = 2),
        makeExercise(
            id = 3,
            name = "Cable Flye",
            groupId = 3,
            orderPriority = 3,
            movementType = MovementType.ISOLATION
        ),
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        exerciseRepository = mockk(relaxed = true)
        templateRepository = mockk(relaxed = true)
        orderExercisesUseCase = OrderExercisesUseCase()

        coEvery { exerciseRepository.getExercisesByIds(any()) } returns testExercises

        viewModel = WorkoutSetupViewModel(
            exerciseRepository = exerciseRepository,
            templateRepository = templateRepository,
            orderExercisesUseCase = orderExercisesUseCase,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Group selection ---

    @Test
    fun `initial state has no groups selected`() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.selectedGroups.isEmpty())
            assertFalse(state.canProceedFromGroups)
        }
    }

    @Test
    fun `toggle group adds to selection`() = runTest {
        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(WorkoutSetupIntent.ToggleGroup(MuscleGroup.CHEST))
            val state = awaitItem()
            assertTrue(MuscleGroup.CHEST in state.selectedGroups)
            assertEquals(1, state.selectedGroupCount)
            assertTrue(state.canProceedFromGroups)
        }
    }

    @Test
    fun `toggle group twice removes from selection`() = runTest {
        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(WorkoutSetupIntent.ToggleGroup(MuscleGroup.CHEST))
            awaitItem()

            viewModel.onIntent(WorkoutSetupIntent.ToggleGroup(MuscleGroup.CHEST))
            val state = awaitItem()
            assertFalse(MuscleGroup.CHEST in state.selectedGroups)
            assertEquals(0, state.selectedGroupCount)
        }
    }

    @Test
    fun `multiple groups can be selected`() = runTest {
        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(WorkoutSetupIntent.ToggleGroup(MuscleGroup.CHEST))
            awaitItem()

            viewModel.onIntent(WorkoutSetupIntent.ToggleGroup(MuscleGroup.BACK))
            awaitItem()

            viewModel.onIntent(WorkoutSetupIntent.ToggleGroup(MuscleGroup.SHOULDERS))
            val state = awaitItem()
            assertEquals(3, state.selectedGroupCount)
            assertTrue(MuscleGroup.CHEST in state.selectedGroups)
            assertTrue(MuscleGroup.BACK in state.selectedGroups)
            assertTrue(MuscleGroup.SHOULDERS in state.selectedGroups)
        }
    }

    // --- Exercise ordering ---

    @Test
    fun `set exercises triggers auto-ordering and navigation`() = runTest {
        viewModel.sideEffect.test {
            viewModel.onIntent(WorkoutSetupIntent.SetExercises(setOf(1L, 2L, 3L)))

            val effect = awaitItem()
            assertTrue(effect is WorkoutSetupSideEffect.NavigateToExerciseOrder)
        }

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.hasExercises)
            assertEquals(3, state.selectedExercises.size)
        }
    }

    @Test
    fun `move exercise reorders list`() = runTest {
        // First load exercises
        viewModel.onIntent(WorkoutSetupIntent.SetExercises(setOf(1L, 2L, 3L)))

        viewModel.state.test {
            val initial = awaitItem()
            val firstExercise = initial.selectedExercises.first()

            // Move first to second position
            viewModel.onIntent(WorkoutSetupIntent.MoveExercise(0, 1))
            val state = awaitItem()

            // First exercise should now be at index 1
            assertEquals(firstExercise.exerciseId, state.selectedExercises[1].exerciseId)
            // Order indices should be updated
            state.selectedExercises.forEachIndexed { index, item ->
                assertEquals(index, item.orderIndex)
            }
        }
    }

    @Test
    fun `move exercise with invalid indices does nothing`() = runTest {
        viewModel.onIntent(WorkoutSetupIntent.SetExercises(setOf(1L, 2L, 3L)))

        viewModel.state.test {
            val initial = awaitItem()

            viewModel.onIntent(WorkoutSetupIntent.MoveExercise(-1, 0))
            expectNoEvents() // No state change -- invalid index
        }
    }

    // --- Template loading ---

    @Test
    fun `load template sets exercises from template order`() = runTest {
        val template = Template(
            id = 1L,
            name = "Push Day A",
            createdAt = 1000L,
            updatedAt = 1000L,
            muscleGroups = listOf(3L),
        )

        val templateExercises = listOf(
            TemplateExercise(id = 1L, templateId = 1L, exerciseId = 3L, orderIndex = 0),
            TemplateExercise(id = 2L, templateId = 1L, exerciseId = 1L, orderIndex = 1),
            TemplateExercise(id = 3L, templateId = 1L, exerciseId = 2L, orderIndex = 2),
        )

        coEvery { templateRepository.getById(1L) } returns template
        coEvery { templateRepository.getExercisesForTemplate(1L) } returns templateExercises
        coEvery { exerciseRepository.getExercisesByIds(any()) } returns testExercises

        viewModel.sideEffect.test {
            viewModel.onIntent(WorkoutSetupIntent.LoadTemplate(1L))

            val effect = awaitItem()
            assertTrue(effect is WorkoutSetupSideEffect.NavigateToExerciseOrder)
        }

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.isFromTemplate)
            assertEquals("Push Day A", state.templateName)
            // Template ordering: Cable Flye (id=3) first, then Bench Press (id=1), then Incline DB Press (id=2)
            assertEquals(3L, state.selectedExercises[0].exerciseId)
            assertEquals(1L, state.selectedExercises[1].exerciseId)
            assertEquals(2L, state.selectedExercises[2].exerciseId)
        }
    }

    // --- Generate plan ---

    @Test
    fun `generate plan emits navigation side effect with exercise IDs`() = runTest {
        viewModel.onIntent(WorkoutSetupIntent.SetExercises(setOf(1L, 2L)))

        // Consume the NavigateToExerciseOrder effect first
        viewModel.sideEffect.test {
            // The NavigateToExerciseOrder was already sent, might need to check
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.sideEffect.test {
            viewModel.onIntent(WorkoutSetupIntent.GeneratePlan)

            val effect = awaitItem()
            assertTrue(effect is WorkoutSetupSideEffect.NavigateToPlanReview)
            val planReview = effect as WorkoutSetupSideEffect.NavigateToPlanReview
            assertTrue(planReview.exerciseIds.isNotEmpty())
        }
    }

    @Test
    fun `generate plan with no exercises does nothing`() = runTest {
        viewModel.sideEffect.test {
            viewModel.onIntent(WorkoutSetupIntent.GeneratePlan)
            expectNoEvents()
        }
    }

    // --- Reset ---

    @Test
    fun `reset clears all state`() = runTest {
        viewModel.onIntent(WorkoutSetupIntent.ToggleGroup(MuscleGroup.CHEST))

        viewModel.state.test {
            awaitItem() // current state

            viewModel.onIntent(WorkoutSetupIntent.Reset)
            val state = awaitItem()
            assertTrue(state.selectedGroups.isEmpty())
            assertTrue(state.selectedExercises.isEmpty())
            assertFalse(state.isFromTemplate)
        }
    }

    companion object {
        fun makeExercise(
            id: Long,
            name: String,
            groupId: Long,
            equipment: Equipment = Equipment.BARBELL,
            movementType: MovementType = MovementType.COMPOUND,
            difficulty: Difficulty = Difficulty.INTERMEDIATE,
            orderPriority: Int = 1,
        ): Exercise = Exercise(
            id = id,
            stableId = "test_${name.lowercase().replace(" ", "_")}",
            name = name,
            description = "Test exercise: $name",
            equipment = equipment,
            movementType = movementType,
            difficulty = difficulty,
            primaryGroupId = groupId,
            secondaryMuscles = emptyList(),
            tips = emptyList(),
            pros = emptyList(),
            displayOrder = id.toInt(),
            orderPriority = orderPriority,
            supersetTags = emptyList(),
            autoProgramMinLevel = 1,
        )
    }
}
