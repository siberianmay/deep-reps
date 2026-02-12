package com.deepreps.feature.templates

import app.cash.turbine.test
import com.deepreps.core.domain.model.Exercise
import com.deepreps.core.domain.model.Template
import com.deepreps.core.domain.model.TemplateExercise
import com.deepreps.core.domain.model.enums.Difficulty
import com.deepreps.core.domain.model.enums.Equipment
import com.deepreps.core.domain.model.enums.MovementType
import com.deepreps.core.domain.repository.ExerciseRepository
import com.deepreps.core.domain.repository.TemplateRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TemplateListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var templateRepository: TemplateRepository
    private lateinit var exerciseRepository: ExerciseRepository
    private lateinit var viewModel: TemplateListViewModel

    private val now = System.currentTimeMillis()

    private val testTemplates = listOf(
        Template(
            id = 1L,
            name = "Push Day",
            createdAt = now - 86_400_000,
            updatedAt = now - 3_600_000,
            muscleGroups = listOf(3L, 5L),
        ),
        Template(
            id = 2L,
            name = "Pull Day",
            createdAt = now - 172_800_000,
            updatedAt = now - 7_200_000,
            muscleGroups = listOf(4L, 6L),
        ),
    )

    private val templateExercises1 = listOf(
        TemplateExercise(id = 10L, templateId = 1L, exerciseId = 100L, orderIndex = 0),
        TemplateExercise(id = 11L, templateId = 1L, exerciseId = 101L, orderIndex = 1),
    )

    private val templateExercises2 = listOf(
        TemplateExercise(id = 20L, templateId = 2L, exerciseId = 102L, orderIndex = 0),
    )

    private val exerciseDetails = listOf(
        makeExercise(id = 100L, name = "Bench Press"),
        makeExercise(id = 101L, name = "Overhead Press"),
        makeExercise(id = 102L, name = "Barbell Row"),
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        templateRepository = mockk(relaxed = true)
        exerciseRepository = mockk(relaxed = true)

        every { templateRepository.getAll() } returns flowOf(testTemplates)
        coEvery { templateRepository.getExercisesForTemplate(1L) } returns templateExercises1
        coEvery { templateRepository.getExercisesForTemplate(2L) } returns templateExercises2
        coEvery { exerciseRepository.getExercisesByIds(listOf(100L, 101L)) } returns
            exerciseDetails.filter { it.id in listOf(100L, 101L) }
        coEvery { exerciseRepository.getExercisesByIds(listOf(102L)) } returns
            exerciseDetails.filter { it.id == 102L }
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Loading Templates ---

    @Test
    fun `loads templates on init`() = runTest {
        viewModel = TemplateListViewModel(templateRepository, exerciseRepository)

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(2, state.templates.size)
            assertEquals("Push Day", state.templates[0].name)
            assertEquals("Pull Day", state.templates[1].name)
        }
    }

    @Test
    fun `template UI includes exercise count and preview`() = runTest {
        viewModel = TemplateListViewModel(templateRepository, exerciseRepository)

        viewModel.state.test {
            val state = awaitItem()
            val pushDay = state.templates[0]
            assertEquals(2, pushDay.exerciseCount)
            assertEquals("Bench Press, Overhead Press", pushDay.exercisePreview)

            val pullDay = state.templates[1]
            assertEquals(1, pullDay.exerciseCount)
            assertEquals("Barbell Row", pullDay.exercisePreview)
        }
    }

    @Test
    fun `template UI includes muscle group names`() = runTest {
        viewModel = TemplateListViewModel(templateRepository, exerciseRepository)

        viewModel.state.test {
            val state = awaitItem()
            val pushDay = state.templates[0]
            assertTrue(pushDay.muscleGroupNames.contains("Chest"))
            assertTrue(pushDay.muscleGroupNames.contains("Shoulders"))

            val pullDay = state.templates[1]
            assertTrue(pullDay.muscleGroupNames.contains("Back"))
            assertTrue(pullDay.muscleGroupNames.contains("Arms"))
        }
    }

    // --- Empty State ---

    @Test
    fun `empty template list shows empty state`() = runTest {
        every { templateRepository.getAll() } returns flowOf(emptyList())

        viewModel = TemplateListViewModel(templateRepository, exerciseRepository)

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertTrue(state.templates.isEmpty())
            assertNull(state.errorType)
        }
    }

    // --- Error Handling ---

    @Test
    fun `repository error sets error state`() = runTest {
        every { templateRepository.getAll() } returns flow {
            throw IllegalStateException("DB error")
        }

        viewModel = TemplateListViewModel(templateRepository, exerciseRepository)

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(TemplateListError.LoadFailed, state.errorType)
        }
    }

    @Test
    fun `retry after error reloads templates`() = runTest {
        var callCount = 0
        every { templateRepository.getAll() } answers {
            callCount++
            if (callCount == 1) {
                flow { throw IllegalStateException("DB error") }
            } else {
                flowOf(testTemplates)
            }
        }

        viewModel = TemplateListViewModel(templateRepository, exerciseRepository)

        viewModel.state.test {
            val errorState = awaitItem()
            assertEquals(TemplateListError.LoadFailed, errorState.errorType)

            viewModel.onIntent(TemplateListIntent.Retry)

            val recovered = awaitItem()
            assertNull(recovered.errorType)
            assertEquals(2, recovered.templates.size)
        }
    }

    // --- Delete with Confirmation ---

    @Test
    fun `request delete shows confirmation dialog`() = runTest {
        viewModel = TemplateListViewModel(templateRepository, exerciseRepository)

        viewModel.state.test {
            awaitItem() // loaded

            viewModel.onIntent(TemplateListIntent.RequestDelete(1L, "Push Day"))

            val state = awaitItem()
            assertNotNull(state.showDeleteConfirmation)
            assertEquals(1L, state.showDeleteConfirmation?.templateId)
            assertEquals("Push Day", state.showDeleteConfirmation?.templateName)
        }
    }

    @Test
    fun `dismiss delete hides confirmation dialog`() = runTest {
        viewModel = TemplateListViewModel(templateRepository, exerciseRepository)

        viewModel.state.test {
            awaitItem() // loaded

            viewModel.onIntent(TemplateListIntent.RequestDelete(1L, "Push Day"))
            awaitItem() // dialog showing

            viewModel.onIntent(TemplateListIntent.DismissDelete)

            val state = awaitItem()
            assertNull(state.showDeleteConfirmation)
        }
    }

    @Test
    fun `confirm delete removes template via repository`() = runTest {
        coEvery { templateRepository.getById(1L) } returns testTemplates[0]

        viewModel = TemplateListViewModel(templateRepository, exerciseRepository)

        viewModel.state.test { awaitItem() }

        viewModel.onIntent(TemplateListIntent.RequestDelete(1L, "Push Day"))
        viewModel.onIntent(TemplateListIntent.ConfirmDelete)

        coVerify { templateRepository.delete(testTemplates[0]) }
    }

    @Test
    fun `confirm delete emits snackbar side effect`() = runTest {
        coEvery { templateRepository.getById(1L) } returns testTemplates[0]

        viewModel = TemplateListViewModel(templateRepository, exerciseRepository)

        viewModel.sideEffect.test {
            viewModel.onIntent(TemplateListIntent.RequestDelete(1L, "Push Day"))
            viewModel.onIntent(TemplateListIntent.ConfirmDelete)

            val effect = awaitItem()
            assertTrue(effect is TemplateListSideEffect.ShowSnackbar)
            assertTrue(
                (effect as TemplateListSideEffect.ShowSnackbar).message.contains("Push Day"),
            )
        }
    }

    @Test
    fun `confirm delete when template not found does nothing`() = runTest {
        coEvery { templateRepository.getById(99L) } returns null

        viewModel = TemplateListViewModel(templateRepository, exerciseRepository)

        viewModel.state.test { awaitItem() }

        viewModel.onIntent(TemplateListIntent.RequestDelete(99L, "Ghost"))
        viewModel.onIntent(TemplateListIntent.ConfirmDelete)

        // Should not crash. Verify delete was never called.
        coVerify(exactly = 0) { templateRepository.delete(any()) }
    }

    @Test
    fun `delete failure sets DeleteFailed error`() = runTest {
        coEvery { templateRepository.getById(1L) } returns testTemplates[0]
        coEvery { templateRepository.delete(any()) } throws RuntimeException("Delete failed")

        viewModel = TemplateListViewModel(templateRepository, exerciseRepository)

        viewModel.state.test {
            awaitItem() // loaded

            viewModel.onIntent(TemplateListIntent.RequestDelete(1L, "Push Day"))
            awaitItem() // dialog showing

            viewModel.onIntent(TemplateListIntent.ConfirmDelete)

            // Dialog dismissed
            val afterConfirm = awaitItem()
            assertNull(afterConfirm.showDeleteConfirmation)

            // Error set
            val errorState = awaitItem()
            assertEquals(TemplateListError.DeleteFailed, errorState.errorType)
        }
    }

    // --- Navigation Side Effects ---

    @Test
    fun `load template emits NavigateToWorkoutSetup`() = runTest {
        viewModel = TemplateListViewModel(templateRepository, exerciseRepository)

        viewModel.sideEffect.test {
            viewModel.onIntent(TemplateListIntent.LoadTemplate(1L))

            val effect = awaitItem()
            assertTrue(effect is TemplateListSideEffect.NavigateToWorkoutSetup)
            assertEquals(1L, (effect as TemplateListSideEffect.NavigateToWorkoutSetup).templateId)
        }
    }

    @Test
    fun `create template emits NavigateToCreateTemplate`() = runTest {
        viewModel = TemplateListViewModel(templateRepository, exerciseRepository)

        viewModel.sideEffect.test {
            viewModel.onIntent(TemplateListIntent.CreateTemplate)

            val effect = awaitItem()
            assertTrue(effect is TemplateListSideEffect.NavigateToCreateTemplate)
        }
    }

    @Test
    fun `edit template emits NavigateToEditTemplate`() = runTest {
        viewModel = TemplateListViewModel(templateRepository, exerciseRepository)

        viewModel.sideEffect.test {
            viewModel.onIntent(TemplateListIntent.EditTemplate(2L))

            val effect = awaitItem()
            assertTrue(effect is TemplateListSideEffect.NavigateToEditTemplate)
            assertEquals(
                2L,
                (effect as TemplateListSideEffect.NavigateToEditTemplate).templateId,
            )
        }
    }

    // --- Companion Helpers ---

    @Test
    fun `muscleGroupNameFromId maps IDs correctly`() {
        assertEquals("Legs", TemplateListViewModel.muscleGroupNameFromId(1L))
        assertEquals("Lower Back", TemplateListViewModel.muscleGroupNameFromId(2L))
        assertEquals("Chest", TemplateListViewModel.muscleGroupNameFromId(3L))
        assertEquals("Back", TemplateListViewModel.muscleGroupNameFromId(4L))
        assertEquals("Shoulders", TemplateListViewModel.muscleGroupNameFromId(5L))
        assertEquals("Arms", TemplateListViewModel.muscleGroupNameFromId(6L))
        assertEquals("Core", TemplateListViewModel.muscleGroupNameFromId(7L))
        assertNull(TemplateListViewModel.muscleGroupNameFromId(0L))
        assertNull(TemplateListViewModel.muscleGroupNameFromId(99L))
    }

    @Test
    fun `formatLastUsed returns correct relative text`() {
        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L

        assertEquals("Used today", TemplateListViewModel.formatLastUsed(now - 1000))
        assertEquals("Used yesterday", TemplateListViewModel.formatLastUsed(now - oneDay))
        assertEquals("Used 3 days ago", TemplateListViewModel.formatLastUsed(now - 3 * oneDay))
        assertEquals("Used 2 weeks ago", TemplateListViewModel.formatLastUsed(now - 14 * oneDay))
        assertEquals("Used 2 months ago", TemplateListViewModel.formatLastUsed(now - 60 * oneDay))
        assertEquals("Used 1 years ago", TemplateListViewModel.formatLastUsed(now - 400 * oneDay))
    }

    companion object {

        fun makeExercise(
            id: Long = 1L,
            name: String = "Test Exercise",
        ): Exercise = Exercise(
            id = id,
            stableId = "test_${name.lowercase().replace(" ", "_")}",
            name = name,
            description = "Test description for $name",
            equipment = Equipment.BARBELL,
            movementType = MovementType.COMPOUND,
            difficulty = Difficulty.INTERMEDIATE,
            primaryGroupId = 3L,
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
