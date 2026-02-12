package com.deepreps.core.domain.usecase

import com.deepreps.core.domain.model.ExerciseForPlan
import com.deepreps.core.domain.model.ExercisePlan
import com.deepreps.core.domain.model.GeneratedPlan
import com.deepreps.core.domain.model.PlannedSet
import com.deepreps.core.domain.model.PlanRequest
import com.deepreps.core.domain.model.PlanResult
import com.deepreps.core.domain.model.UserPlanProfile
import com.deepreps.core.domain.model.enums.SetType
import com.deepreps.core.domain.provider.AiPlanException
import com.deepreps.core.domain.provider.AiPlanProvider
import com.deepreps.core.domain.provider.BaselinePlanGenerator
import com.deepreps.core.domain.provider.ConnectivityChecker
import com.deepreps.core.domain.repository.CachedPlanRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GeneratePlanUseCaseTest {

    private val aiProvider = mockk<AiPlanProvider>()
    private val cachedPlanRepository = mockk<CachedPlanRepository>(relaxed = true)
    private val baselinePlanGenerator = mockk<BaselinePlanGenerator>()
    private val connectivityChecker = mockk<ConnectivityChecker>()

    private lateinit var useCase: GeneratePlanUseCase

    private val samplePlan = GeneratedPlan(
        exercises = listOf(
            ExercisePlan(
                exerciseId = 1,
                stableId = "chest_barbell_bench_press",
                exerciseName = "Barbell Bench Press",
                sets = listOf(
                    PlannedSet(SetType.WORKING, 60.0, 10),
                ),
                restSeconds = 120,
            ),
        ),
    )

    private val sampleRequest = PlanRequest(
        userProfile = UserPlanProfile(
            experienceLevel = 1,
            bodyWeightKg = 80.0,
            age = 25,
            gender = "male",
        ),
        exercises = listOf(
            ExerciseForPlan(
                exerciseId = 1,
                stableId = "chest_barbell_bench_press",
                name = "Barbell Bench Press",
                equipment = "barbell",
                movementType = "compound",
                difficulty = "beginner",
                primaryGroup = "chest",
            ),
        ),
        trainingHistory = emptyList(),
        periodizationModel = "linear",
        performanceTrend = null,
        weeksSinceDeload = null,
        deloadRecommended = false,
        currentBlockPhase = null,
        currentBlockWeek = null,
    )

    @BeforeEach
    fun setup() {
        useCase = GeneratePlanUseCase(
            aiProvider = aiProvider,
            cachedPlanRepository = cachedPlanRepository,
            baselinePlanGenerator = baselinePlanGenerator,
            connectivityChecker = connectivityChecker,
        )
    }

    @Test
    fun `returns AiGenerated when online and AI succeeds`() = runTest {
        every { connectivityChecker.isOnline() } returns true
        coEvery { aiProvider.generatePlan(any()) } returns samplePlan

        val result = useCase.execute(sampleRequest)

        assertThat(result).isInstanceOf(PlanResult.AiGenerated::class.java)
        assertThat((result as PlanResult.AiGenerated).plan).isEqualTo(samplePlan)
    }

    @Test
    fun `caches plan after successful AI generation`() = runTest {
        every { connectivityChecker.isOnline() } returns true
        coEvery { aiProvider.generatePlan(any()) } returns samplePlan

        useCase.execute(sampleRequest)

        coVerify { cachedPlanRepository.save(any(), eq(1), eq(samplePlan)) }
    }

    @Test
    fun `returns Cached when offline and cache hit exists`() = runTest {
        every { connectivityChecker.isOnline() } returns false
        coEvery { cachedPlanRepository.getByHash(any(), any()) } returns samplePlan

        val result = useCase.execute(sampleRequest)

        assertThat(result).isInstanceOf(PlanResult.Cached::class.java)
        assertThat((result as PlanResult.Cached).plan).isEqualTo(samplePlan)
    }

    @Test
    fun `returns Cached when AI fails and cache hit exists`() = runTest {
        every { connectivityChecker.isOnline() } returns true
        coEvery { aiProvider.generatePlan(any()) } throws AiPlanException("API error")
        coEvery { cachedPlanRepository.getByHash(any(), any()) } returns samplePlan

        val result = useCase.execute(sampleRequest)

        assertThat(result).isInstanceOf(PlanResult.Cached::class.java)
    }

    @Test
    fun `returns Baseline when offline and no cache`() = runTest {
        every { connectivityChecker.isOnline() } returns false
        coEvery { cachedPlanRepository.getByHash(any(), any()) } returns null
        every { baselinePlanGenerator.generate(any()) } returns samplePlan

        val result = useCase.execute(sampleRequest)

        assertThat(result).isInstanceOf(PlanResult.Baseline::class.java)
        assertThat((result as PlanResult.Baseline).plan).isEqualTo(samplePlan)
    }

    @Test
    fun `returns Manual when all fallbacks exhausted`() = runTest {
        every { connectivityChecker.isOnline() } returns false
        coEvery { cachedPlanRepository.getByHash(any(), any()) } returns null
        every { baselinePlanGenerator.generate(any()) } returns null

        val result = useCase.execute(sampleRequest)

        assertThat(result).isInstanceOf(PlanResult.Manual::class.java)
        assertThat(result.plan).isNull()
    }

    @Test
    fun `AI failure falls through to cache before baseline`() = runTest {
        every { connectivityChecker.isOnline() } returns true
        coEvery { aiProvider.generatePlan(any()) } throws AiPlanException("timeout")
        coEvery { cachedPlanRepository.getByHash(any(), any()) } returns null
        every { baselinePlanGenerator.generate(any()) } returns samplePlan

        val result = useCase.execute(sampleRequest)

        assertThat(result).isInstanceOf(PlanResult.Baseline::class.java)
    }

    @Test
    fun `does not call AI provider when offline`() = runTest {
        every { connectivityChecker.isOnline() } returns false
        coEvery { cachedPlanRepository.getByHash(any(), any()) } returns samplePlan

        useCase.execute(sampleRequest)

        coVerify(exactly = 0) { aiProvider.generatePlan(any()) }
    }

    @Test
    fun `cache hash is deterministic for same exercise set`() {
        val hash1 = GeneratePlanUseCase.computeCacheHash(sampleRequest)
        val hash2 = GeneratePlanUseCase.computeCacheHash(sampleRequest)

        assertThat(hash1).isEqualTo(hash2)
    }

    @Test
    fun `cache hash is different for different exercise sets`() {
        val request2 = sampleRequest.copy(
            exercises = listOf(
                ExerciseForPlan(
                    exerciseId = 2,
                    stableId = "chest_dumbbell_fly",
                    name = "Dumbbell Fly",
                    equipment = "dumbbell",
                    movementType = "isolation",
                    difficulty = "beginner",
                    primaryGroup = "chest",
                ),
            ),
        )

        val hash1 = GeneratePlanUseCase.computeCacheHash(sampleRequest)
        val hash2 = GeneratePlanUseCase.computeCacheHash(request2)

        assertThat(hash1).isNotEqualTo(hash2)
    }

    @Test
    fun `cleans expired cache entries on each execution`() = runTest {
        every { connectivityChecker.isOnline() } returns false
        coEvery { cachedPlanRepository.getByHash(any(), any()) } returns samplePlan

        useCase.execute(sampleRequest)

        coVerify { cachedPlanRepository.deleteExpired(any()) }
    }
}
