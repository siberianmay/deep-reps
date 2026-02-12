package com.deepreps.core.domain.usecase

import com.deepreps.core.domain.model.Exercise
import com.deepreps.core.domain.model.UserProfile
import com.deepreps.core.domain.model.enums.Difficulty
import com.deepreps.core.domain.model.enums.Equipment
import com.deepreps.core.domain.model.enums.ExperienceLevel
import com.deepreps.core.domain.model.enums.Gender
import com.deepreps.core.domain.model.enums.MovementType
import com.deepreps.core.domain.model.enums.WeightUnit
import com.deepreps.core.domain.repository.UserProfileRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ResolveRestTimerUseCaseTest {

    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var useCase: ResolveRestTimerUseCase

    private val intermediateProfile = UserProfile(
        id = 1L,
        experienceLevel = ExperienceLevel.INTERMEDIATE,
        preferredUnit = WeightUnit.KG,
        age = 28,
        heightCm = 178.0,
        gender = Gender.MALE,
        bodyWeightKg = 80.0,
        createdAt = 1000L,
        updatedAt = 1000L,
    )

    private val beginnerProfile = intermediateProfile.copy(
        experienceLevel = ExperienceLevel.BEGINNER,
    )

    private val advancedProfile = intermediateProfile.copy(
        experienceLevel = ExperienceLevel.ADVANCED,
    )

    @BeforeEach
    fun setUp() {
        userProfileRepository = mockk()
        useCase = ResolveRestTimerUseCase(userProfileRepository)
    }

    private fun makeExercise(
        movementType: MovementType = MovementType.COMPOUND,
        primaryGroupId: Long = 3L,
    ): Exercise = Exercise(
        id = 1L,
        stableId = "test_exercise",
        name = "Test Exercise",
        description = "Test",
        equipment = Equipment.BARBELL,
        movementType = movementType,
        difficulty = Difficulty.INTERMEDIATE,
        primaryGroupId = primaryGroupId,
        secondaryMuscles = emptyList(),
        tips = emptyList(),
        pros = emptyList(),
        displayOrder = 1,
        orderPriority = 1,
        supersetTags = emptyList(),
        autoProgramMinLevel = 1,
    )

    // --- Priority Chain ---

    @Nested
    inner class PriorityChain {

        @Test
        fun `priority 1 -- AI plan rest seconds takes highest precedence`() = runTest {
            val exercise = makeExercise()
            val result = useCase(
                exercise = exercise,
                aiPlanRestSeconds = 150,
                userOverrideSeconds = 100,
                userGlobalDefaultSeconds = 90,
            )
            assertEquals(150, result)
        }

        @Test
        fun `priority 2 -- user override when no AI plan`() = runTest {
            val exercise = makeExercise()
            val result = useCase(
                exercise = exercise,
                aiPlanRestSeconds = null,
                userOverrideSeconds = 100,
                userGlobalDefaultSeconds = 90,
            )
            assertEquals(100, result)
        }

        @Test
        fun `priority 3 -- global default when no AI plan or override`() = runTest {
            val exercise = makeExercise()
            val result = useCase(
                exercise = exercise,
                aiPlanRestSeconds = null,
                userOverrideSeconds = null,
                userGlobalDefaultSeconds = 90,
            )
            assertEquals(90, result)
        }

        @Test
        fun `priority 4 -- CSCS baseline when no other values`() = runTest {
            coEvery { userProfileRepository.get() } returns intermediateProfile
            val exercise = makeExercise(movementType = MovementType.COMPOUND)
            val result = useCase(
                exercise = exercise,
                aiPlanRestSeconds = null,
                userOverrideSeconds = null,
                userGlobalDefaultSeconds = null,
            )
            assertEquals(120, result) // Intermediate compound = 120s
        }

        @Test
        fun `zero AI plan seconds falls through to next priority`() = runTest {
            val exercise = makeExercise()
            val result = useCase(
                exercise = exercise,
                aiPlanRestSeconds = 0,
                userOverrideSeconds = 100,
                userGlobalDefaultSeconds = null,
            )
            assertEquals(100, result)
        }

        @Test
        fun `negative AI plan seconds falls through to next priority`() = runTest {
            val exercise = makeExercise()
            val result = useCase(
                exercise = exercise,
                aiPlanRestSeconds = -5,
                userOverrideSeconds = null,
                userGlobalDefaultSeconds = 60,
            )
            assertEquals(60, result)
        }
    }

    // --- CSCS Baseline Values ---

    @Nested
    inner class CscsBaseline {

        @Test
        fun `compound beginner returns 90s`() = runTest {
            coEvery { userProfileRepository.get() } returns beginnerProfile
            val result = useCase(makeExercise(MovementType.COMPOUND))
            assertEquals(90, result)
        }

        @Test
        fun `compound intermediate returns 120s`() = runTest {
            coEvery { userProfileRepository.get() } returns intermediateProfile
            val result = useCase(makeExercise(MovementType.COMPOUND))
            assertEquals(120, result)
        }

        @Test
        fun `compound advanced returns 180s`() = runTest {
            coEvery { userProfileRepository.get() } returns advancedProfile
            val result = useCase(makeExercise(MovementType.COMPOUND))
            assertEquals(180, result)
        }

        @Test
        fun `isolation beginner returns 60s`() = runTest {
            coEvery { userProfileRepository.get() } returns beginnerProfile
            val result = useCase(makeExercise(MovementType.ISOLATION))
            assertEquals(60, result)
        }

        @Test
        fun `isolation intermediate returns 75s`() = runTest {
            coEvery { userProfileRepository.get() } returns intermediateProfile
            val result = useCase(makeExercise(MovementType.ISOLATION))
            assertEquals(75, result)
        }

        @Test
        fun `isolation advanced returns 75s`() = runTest {
            coEvery { userProfileRepository.get() } returns advancedProfile
            val result = useCase(makeExercise(MovementType.ISOLATION))
            assertEquals(75, result)
        }

        @Test
        fun `core exercises always return 60s regardless of level`() = runTest {
            coEvery { userProfileRepository.get() } returns advancedProfile
            // Core group ID = 7
            val result = useCase(makeExercise(MovementType.COMPOUND, primaryGroupId = 7L))
            assertEquals(60, result)
        }

        @Test
        fun `null profile defaults to intermediate`() = runTest {
            coEvery { userProfileRepository.get() } returns null
            val result = useCase(makeExercise(MovementType.COMPOUND))
            assertEquals(120, result) // Intermediate compound = 120s
        }
    }
}
