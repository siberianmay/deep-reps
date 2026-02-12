package com.deepreps.core.domain.usecase

import com.deepreps.core.domain.model.UserProfile
import com.deepreps.core.domain.model.enums.ExperienceLevel
import com.deepreps.core.domain.model.enums.Gender
import com.deepreps.core.domain.model.enums.WeightUnit
import com.deepreps.core.domain.repository.UserProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CompleteOnboardingUseCaseTest {

    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var useCase: CompleteOnboardingUseCase

    @BeforeEach
    fun setUp() {
        userProfileRepository = mockk(relaxed = true)
        useCase = CompleteOnboardingUseCase(userProfileRepository)
    }

    @Test
    fun `creates profile with all fields`() = runTest {
        val profileSlot = slot<UserProfile>()
        coEvery { userProfileRepository.save(capture(profileSlot)) } returns Unit

        useCase(
            experienceLevel = ExperienceLevel.INTERMEDIATE,
            preferredUnit = WeightUnit.KG,
            age = 28,
            heightCm = 180.0,
            gender = Gender.MALE,
            bodyWeightKg = 80.0,
        )

        coVerify(exactly = 1) { userProfileRepository.save(any()) }

        val saved = profileSlot.captured
        assertEquals(1L, saved.id)
        assertEquals(ExperienceLevel.INTERMEDIATE, saved.experienceLevel)
        assertEquals(WeightUnit.KG, saved.preferredUnit)
        assertEquals(28, saved.age)
        assertEquals(180.0, saved.heightCm)
        assertEquals(Gender.MALE, saved.gender)
        assertEquals(80.0, saved.bodyWeightKg)
    }

    @Test
    fun `creates profile with optional fields null`() = runTest {
        val profileSlot = slot<UserProfile>()
        coEvery { userProfileRepository.save(capture(profileSlot)) } returns Unit

        useCase(
            experienceLevel = ExperienceLevel.BEGINNER,
            preferredUnit = WeightUnit.LBS,
            age = null,
            heightCm = null,
            gender = null,
            bodyWeightKg = null,
        )

        coVerify(exactly = 1) { userProfileRepository.save(any()) }

        val saved = profileSlot.captured
        assertEquals(ExperienceLevel.BEGINNER, saved.experienceLevel)
        assertEquals(WeightUnit.LBS, saved.preferredUnit)
        assertNull(saved.age)
        assertNull(saved.heightCm)
        assertNull(saved.gender)
        assertNull(saved.bodyWeightKg)
    }

    @Test
    fun `profile ID is always 1 (singleton)`() = runTest {
        val profileSlot = slot<UserProfile>()
        coEvery { userProfileRepository.save(capture(profileSlot)) } returns Unit

        useCase(
            experienceLevel = ExperienceLevel.ADVANCED,
            preferredUnit = WeightUnit.KG,
            age = null,
            heightCm = null,
            gender = null,
            bodyWeightKg = null,
        )

        assertEquals(1L, profileSlot.captured.id)
    }

    @Test
    fun `timestamps are set to current time`() = runTest {
        val profileSlot = slot<UserProfile>()
        coEvery { userProfileRepository.save(capture(profileSlot)) } returns Unit

        val before = System.currentTimeMillis()

        useCase(
            experienceLevel = ExperienceLevel.BEGINNER,
            preferredUnit = WeightUnit.KG,
            age = null,
            heightCm = null,
            gender = null,
            bodyWeightKg = null,
        )

        val after = System.currentTimeMillis()

        val saved = profileSlot.captured
        assert(saved.createdAt in before..after)
        assert(saved.updatedAt in before..after)
        assertEquals(saved.createdAt, saved.updatedAt)
    }
}
