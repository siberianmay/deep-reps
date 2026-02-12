package com.deepreps.feature.templates

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.deepreps.core.domain.model.Exercise
import com.deepreps.core.domain.model.Template
import com.deepreps.core.domain.model.TemplateExercise
import com.deepreps.core.domain.model.enums.Difficulty
import com.deepreps.core.domain.model.enums.Equipment
import com.deepreps.core.domain.model.enums.MovementType
import com.deepreps.core.domain.repository.ExerciseRepository
import com.deepreps.core.domain.repository.TemplateRepository
import com.deepreps.core.domain.usecase.InvalidTemplateException
import com.deepreps.core.domain.usecase.SaveTemplateUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateTemplateViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var saveTemplateUseCase: SaveTemplateUseCase
    private lateinit var templateRepository: TemplateRepository
    private lateinit var exerciseRepository: ExerciseRepository
    private lateinit var viewModel: CreateTemplateViewModel

    private val now = System.currentTimeMillis()

    private val existingTemplate = Template(
        id = 1L,
        name = "Push Day",
        createdAt = now - 86_400_000,
        updatedAt = now - 3_600_000,
        muscleGroups = listOf(3L, 5L),
    )

    private val existingTemplateExercises = listOf(
        TemplateExercise(id = 10L, templateId = 1L, exerciseId = 100L, orderIndex = 0),
        TemplateExercise(id = 11L, templateId = 1L, exerciseId = 101L, orderIndex = 1),
    )

    private val exerciseDetails = listOf(
        makeExercise(id = 100L, name = "Bench Press"),
        makeExercise(id = 101L, name = "Overhead Press"),
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        saveTemplateUseCase = mockk(relaxed = true)
        templateRepository = mockk(relaxed = true)
        exerciseRepository = mockk(relaxed = true)

        coEvery { saveTemplateUseCase.invoke(any(), any(), any()) } returns 1L
        coEvery { saveTemplateUseCase.update(any(), any(), any(), any()) } just Runs
        coEvery { templateRepository.getById(1L) } returns existingTemplate
        coEvery { templateRepository.getExercisesForTemplate(1L) } returns existingTemplateExercises
        coEvery { exerciseRepository.getExercisesByIds(listOf(100L, 101L)) } returns exerciseDetails
        coEvery { exerciseRepository.getExercisesByIds(any()) } returns exerciseDetails
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        savedState: Map<String, Any?> = emptyMap(),
    ): CreateTemplateViewModel {
        val handle = SavedStateHandle(savedState)
        return CreateTemplateViewModel(
            savedStateHandle = handle,
            saveTemplateUseCase = saveTemplateUseCase,
            templateRepository = templateRepository,
            exerciseRepository = exerciseRepository,
        )
    }

    // --- Create New Template ---

    @Test
    fun `initial state is blank for new template`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("", state.name)
            assertTrue(state.exercises.isEmpty())
            assertFalse(state.isEditing)
            assertNull(state.templateId)
            assertFalse(state.isSaving)
        }
    }

    @Test
    fun `update name updates state and clears name error`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(CreateTemplateIntent.UpdateName("Leg Day"))

            val state = awaitItem()
            assertEquals("Leg Day", state.name)
            assertNull(state.nameError)
        }
    }

    // --- Edit Existing Template ---

    @Test
    fun `loads existing template in edit mode`() = runTest {
        viewModel = createViewModel(
            savedState = mapOf(CreateTemplateViewModel.TEMPLATE_ID_ARG to 1L),
        )

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.isEditing)
            assertEquals(1L, state.templateId)
            assertEquals("Push Day", state.name)
            assertEquals(2, state.exercises.size)
            assertEquals("Bench Press", state.exercises[0].name)
            assertEquals("Overhead Press", state.exercises[1].name)
        }
    }

    @Test
    fun `edit mode populates muscle group names`() = runTest {
        viewModel = createViewModel(
            savedState = mapOf(CreateTemplateViewModel.TEMPLATE_ID_ARG to 1L),
        )

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.muscleGroupNames.contains("Chest"))
            assertTrue(state.muscleGroupNames.contains("Shoulders"))
        }
    }

    @Test
    fun `edit template load failure emits ShowError`() = runTest {
        coEvery { templateRepository.getById(1L) } throws RuntimeException("DB error")

        viewModel = createViewModel(
            savedState = mapOf(CreateTemplateViewModel.TEMPLATE_ID_ARG to 1L),
        )

        viewModel.sideEffect.test {
            val effect = awaitItem()
            assertTrue(effect is CreateTemplateSideEffect.ShowError)
            assertTrue(
                (effect as CreateTemplateSideEffect.ShowError).message
                    .contains("Failed to load template"),
            )
        }
    }

    // --- Validation: Empty Name ---

    @Test
    fun `save with empty name sets name error`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(CreateTemplateIntent.Save)

            val state = awaitItem()
            assertNotNull(state.nameError)
            assertEquals("Template name required", state.nameError)
        }
    }

    @Test
    fun `save with blank name sets name error`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()

            viewModel.onIntent(CreateTemplateIntent.UpdateName("   "))
            awaitItem()

            viewModel.onIntent(CreateTemplateIntent.Save)

            val state = awaitItem()
            assertNotNull(state.nameError)
            assertEquals("Template name required", state.nameError)
        }
    }

    @Test
    fun `save with name exceeding max length sets name error`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()

            val longName = "A".repeat(CreateTemplateUiState.MAX_NAME_LENGTH + 1)
            viewModel.onIntent(CreateTemplateIntent.UpdateName(longName))
            awaitItem()

            viewModel.onIntent(CreateTemplateIntent.Save)

            val state = awaitItem()
            assertNotNull(state.nameError)
            assertTrue(state.nameError!!.contains("${CreateTemplateUiState.MAX_NAME_LENGTH}"))
        }
    }

    @Test
    fun `save with no exercises sets exercise error`() = runTest {
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()

            viewModel.onIntent(CreateTemplateIntent.UpdateName("Valid Name"))
            awaitItem()

            viewModel.onIntent(CreateTemplateIntent.Save)

            val state = awaitItem()
            assertNotNull(state.exerciseError)
            assertEquals("Add at least one exercise", state.exerciseError)
        }
    }

    // --- Save Success ---

    @Test
    fun `save new template calls use case and emits TemplateSaved`() = runTest {
        viewModel = createViewModel(
            savedState = mapOf(CreateTemplateViewModel.TEMPLATE_ID_ARG to 1L),
        )

        viewModel.state.test {
            awaitItem() // loaded with exercises from edit mode

            // Change name to something new
            viewModel.onIntent(CreateTemplateIntent.UpdateName("Updated Push Day"))
            awaitItem()
        }

        viewModel.sideEffect.test {
            viewModel.onIntent(CreateTemplateIntent.Save)

            val effect = awaitItem()
            assertTrue(effect is CreateTemplateSideEffect.TemplateSaved)
        }
    }

    @Test
    fun `save updates existing template in edit mode`() = runTest {
        viewModel = createViewModel(
            savedState = mapOf(CreateTemplateViewModel.TEMPLATE_ID_ARG to 1L),
        )

        viewModel.state.test {
            awaitItem() // loaded
        }

        viewModel.onIntent(CreateTemplateIntent.Save)

        coVerify {
            saveTemplateUseCase.update(
                templateId = 1L,
                name = any(),
                exerciseIds = any(),
                muscleGroupIds = any(),
            )
        }
    }

    // --- Save Failure ---

    @Test
    fun `save failure from InvalidTemplateException emits ShowError`() = runTest {
        coEvery { saveTemplateUseCase.update(any(), any(), any(), any()) } throws
            InvalidTemplateException("Bad template data")

        viewModel = createViewModel(
            savedState = mapOf(CreateTemplateViewModel.TEMPLATE_ID_ARG to 1L),
        )

        viewModel.state.test { awaitItem() }

        viewModel.sideEffect.test {
            viewModel.onIntent(CreateTemplateIntent.Save)

            val effect = awaitItem()
            assertTrue(effect is CreateTemplateSideEffect.ShowError)
            assertEquals(
                "Bad template data",
                (effect as CreateTemplateSideEffect.ShowError).message,
            )
        }
    }

    @Test
    fun `save failure from generic exception emits ShowError`() = runTest {
        coEvery { saveTemplateUseCase.update(any(), any(), any(), any()) } throws
            RuntimeException("DB write failed")

        viewModel = createViewModel(
            savedState = mapOf(CreateTemplateViewModel.TEMPLATE_ID_ARG to 1L),
        )

        viewModel.state.test { awaitItem() }

        viewModel.sideEffect.test {
            viewModel.onIntent(CreateTemplateIntent.Save)

            val effect = awaitItem()
            assertTrue(effect is CreateTemplateSideEffect.ShowError)
            assertEquals(
                "Failed to save template",
                (effect as CreateTemplateSideEffect.ShowError).message,
            )
        }
    }

    @Test
    fun `save resets isSaving to false after failure`() = runTest {
        coEvery { saveTemplateUseCase.update(any(), any(), any(), any()) } throws
            RuntimeException("DB error")

        viewModel = createViewModel(
            savedState = mapOf(CreateTemplateViewModel.TEMPLATE_ID_ARG to 1L),
        )

        viewModel.state.test {
            awaitItem() // loaded

            viewModel.onIntent(CreateTemplateIntent.Save)

            // After failure, isSaving should be false
            val state = awaitItem()
            assertFalse(state.isSaving)
        }
    }

    // --- Exercise Operations ---

    @Test
    fun `remove exercise updates list and reindexes`() = runTest {
        viewModel = createViewModel(
            savedState = mapOf(CreateTemplateViewModel.TEMPLATE_ID_ARG to 1L),
        )

        viewModel.state.test {
            awaitItem() // loaded with 2 exercises

            viewModel.onIntent(CreateTemplateIntent.RemoveExercise(100L))

            val state = awaitItem()
            assertEquals(1, state.exercises.size)
            assertEquals(101L, state.exercises[0].exerciseId)
            assertEquals(0, state.exercises[0].orderIndex)
        }
    }

    @Test
    fun `move exercise reorders list`() = runTest {
        viewModel = createViewModel(
            savedState = mapOf(CreateTemplateViewModel.TEMPLATE_ID_ARG to 1L),
        )

        viewModel.state.test {
            awaitItem() // loaded: [Bench Press(0), Overhead Press(1)]

            viewModel.onIntent(CreateTemplateIntent.MoveExercise(0, 1))

            val state = awaitItem()
            assertEquals("Overhead Press", state.exercises[0].name)
            assertEquals("Bench Press", state.exercises[1].name)
            assertEquals(0, state.exercises[0].orderIndex)
            assertEquals(1, state.exercises[1].orderIndex)
        }
    }

    @Test
    fun `move exercise with invalid indices does nothing`() = runTest {
        viewModel = createViewModel(
            savedState = mapOf(CreateTemplateViewModel.TEMPLATE_ID_ARG to 1L),
        )

        viewModel.state.test {
            val initial = awaitItem()

            viewModel.onIntent(CreateTemplateIntent.MoveExercise(-1, 5))

            // No state change expected (still same state)
            expectNoEvents()
            assertEquals(2, initial.exercises.size)
        }
    }

    // --- Close ---

    @Test
    fun `close emits NavigateBack`() = runTest {
        viewModel = createViewModel()

        viewModel.sideEffect.test {
            viewModel.onIntent(CreateTemplateIntent.Close)

            val effect = awaitItem()
            assertTrue(effect is CreateTemplateSideEffect.NavigateBack)
        }
    }

    // --- Pre-populated from Workout ---

    @Test
    fun `loads exercises from exerciseIds argument`() = runTest {
        coEvery { exerciseRepository.getExercisesByIds(listOf(100L, 101L)) } returns exerciseDetails

        viewModel = createViewModel(
            savedState = mapOf(CreateTemplateViewModel.EXERCISE_IDS_ARG to "100,101"),
        )

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isEditing)
            assertEquals(2, state.exercises.size)
            assertEquals("Bench Press", state.exercises[0].name)
            assertEquals("Overhead Press", state.exercises[1].name)
        }
    }

    companion object {

        fun makeExercise(
            id: Long = 1L,
            name: String = "Test Exercise",
        ): Exercise = Exercise(
            id = id,
            stableId = "test_${name.lowercase().replace(" ", "_")}",
            name = name,
            description = "Test description",
            equipment = Equipment.BARBELL,
            movementType = MovementType.COMPOUND,
            difficulty = Difficulty.INTERMEDIATE,
            primaryGroupId = 3L,
            secondaryMuscles = emptyList(),
            tips = emptyList(),
            pros = emptyList(),
            displayOrder = id.toInt(),
            orderPriority = id.toInt(),
            supersetTags = emptyList(),
            autoProgramMinLevel = 1,
        )
    }
}
