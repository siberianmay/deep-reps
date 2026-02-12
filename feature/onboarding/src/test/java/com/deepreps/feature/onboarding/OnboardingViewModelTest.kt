package com.deepreps.feature.onboarding

import app.cash.turbine.test
import com.deepreps.core.data.consent.ConsentManager
import com.deepreps.core.domain.model.enums.ExperienceLevel
import com.deepreps.core.domain.model.enums.WeightUnit
import com.deepreps.core.domain.repository.OnboardingRepository
import com.deepreps.core.domain.repository.UserProfileRepository
import com.deepreps.core.domain.usecase.CompleteOnboardingUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class OnboardingViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var consentManager: ConsentManager
    private lateinit var onboardingRepository: OnboardingRepository
    private lateinit var completeOnboardingUseCase: CompleteOnboardingUseCase
    private lateinit var viewModel: OnboardingViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        userProfileRepository = mockk(relaxed = true)
        consentManager = mockk(relaxed = true)
        onboardingRepository = mockk(relaxed = true)

        every { consentManager.analyticsConsent = any() } just Runs
        every { consentManager.crashlyticsConsent = any() } just Runs
        every { consentManager.markConsentResponded() } just Runs
        every { onboardingRepository.setOnboardingCompleted() } just Runs

        completeOnboardingUseCase = CompleteOnboardingUseCase(userProfileRepository)

        viewModel = OnboardingViewModel(
            completeOnboardingUseCase = completeOnboardingUseCase,
            consentManager = consentManager,
            onboardingRepository = onboardingRepository,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Step navigation ---

    @Test
    fun `initial state starts at step 0`() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(0, state.currentStep)
            assertEquals(4, state.totalSteps)
        }
    }

    @Test
    fun `next step increments current step`() = runTest {
        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(OnboardingIntent.NextStep)
            assertEquals(1, awaitItem().currentStep)

            viewModel.onIntent(OnboardingIntent.NextStep)
            assertEquals(2, awaitItem().currentStep)

            viewModel.onIntent(OnboardingIntent.NextStep)
            assertEquals(3, awaitItem().currentStep)
        }
    }

    @Test
    fun `next step does not exceed max step`() = runTest {
        viewModel.state.test {
            awaitItem() // initial

            // Navigate to last step
            repeat(3) {
                viewModel.onIntent(OnboardingIntent.NextStep)
                awaitItem()
            }

            // Try to go beyond
            viewModel.onIntent(OnboardingIntent.NextStep)
            // Should still be 3 -- no new emission expected
            expectNoEvents()
        }
    }

    @Test
    fun `previous step decrements current step`() = runTest {
        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(OnboardingIntent.NextStep)
            awaitItem() // step 1

            viewModel.onIntent(OnboardingIntent.PreviousStep)
            assertEquals(0, awaitItem().currentStep)
        }
    }

    @Test
    fun `previous step does not go below zero`() = runTest {
        viewModel.state.test {
            awaitItem() // initial at step 0

            viewModel.onIntent(OnboardingIntent.PreviousStep)
            // Should still be 0 -- no new emission expected
            expectNoEvents()
        }
    }

    // --- Consent defaults ---

    @Test
    fun `consent defaults to OFF`() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.analyticsConsent)
            assertFalse(state.crashlyticsConsent)
        }
    }

    @Test
    fun `set analytics consent updates state`() = runTest {
        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(OnboardingIntent.SetAnalyticsConsent(true))
            assertTrue(awaitItem().analyticsConsent)

            viewModel.onIntent(OnboardingIntent.SetAnalyticsConsent(false))
            assertFalse(awaitItem().analyticsConsent)
        }
    }

    @Test
    fun `set crashlytics consent updates state`() = runTest {
        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(OnboardingIntent.SetCrashlyticsConsent(true))
            assertTrue(awaitItem().crashlyticsConsent)
        }
    }

    // --- Experience level ---

    @Test
    fun `experience level defaults to null`() = runTest {
        viewModel.state.test {
            assertNull(awaitItem().experienceLevel)
        }
    }

    @Test
    fun `set experience level updates state`() = runTest {
        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(OnboardingIntent.SetExperienceLevel(ExperienceLevel.INTERMEDIATE))
            assertEquals(ExperienceLevel.INTERMEDIATE, awaitItem().experienceLevel)
        }
    }

    // --- Unit preference ---

    @Test
    fun `weight unit defaults to KG`() = runTest {
        viewModel.state.test {
            assertEquals(WeightUnit.KG, awaitItem().weightUnit)
        }
    }

    @Test
    fun `set weight unit updates state`() = runTest {
        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(OnboardingIntent.SetWeightUnit(WeightUnit.LBS))
            assertEquals(WeightUnit.LBS, awaitItem().weightUnit)
        }
    }

    // --- Optional profile fields ---

    @Test
    fun `set age filters non-digit characters`() = runTest {
        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(OnboardingIntent.SetAge("28abc"))
            assertEquals("28", awaitItem().age)
        }
    }

    @Test
    fun `set height allows decimal`() = runTest {
        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(OnboardingIntent.SetHeightCm("175.5"))
            assertEquals("175.5", awaitItem().heightCm)
        }
    }

    @Test
    fun `set gender updates state`() = runTest {
        viewModel.state.test {
            awaitItem() // initial

            viewModel.onIntent(OnboardingIntent.SetGender(GenderDisplayOption.FEMALE))
            val state = awaitItem()
            assertEquals(GenderDisplayOption.FEMALE, state.genderDisplayOption)
        }
    }

    // --- Skip / Complete ---

    @Test
    fun `skip works with all fields empty`() = runTest {
        // Set only required fields
        viewModel.onIntent(OnboardingIntent.SetExperienceLevel(ExperienceLevel.BEGINNER))

        viewModel.sideEffect.test {
            viewModel.onIntent(OnboardingIntent.Complete)

            val effect = awaitItem()
            assertTrue(effect is OnboardingSideEffect.NavigateToMain)
        }
    }

    @Test
    fun `completion creates user profile`() = runTest {
        viewModel.onIntent(OnboardingIntent.SetExperienceLevel(ExperienceLevel.ADVANCED))
        viewModel.onIntent(OnboardingIntent.SetWeightUnit(WeightUnit.LBS))
        viewModel.onIntent(OnboardingIntent.SetAge("30"))

        viewModel.sideEffect.test {
            viewModel.onIntent(OnboardingIntent.Complete)

            val effect = awaitItem()
            assertTrue(effect is OnboardingSideEffect.NavigateToMain)
        }

        coVerify { userProfileRepository.save(match { it.experienceLevel == ExperienceLevel.ADVANCED }) }
        verify { onboardingRepository.setOnboardingCompleted() }
    }

    @Test
    fun `completion saves consent preferences`() = runTest {
        viewModel.onIntent(OnboardingIntent.SetExperienceLevel(ExperienceLevel.BEGINNER))
        viewModel.onIntent(OnboardingIntent.SetAnalyticsConsent(true))
        viewModel.onIntent(OnboardingIntent.SetCrashlyticsConsent(true))

        viewModel.sideEffect.test {
            viewModel.onIntent(OnboardingIntent.Complete)
            awaitItem()
        }

        verify { consentManager.analyticsConsent = true }
        verify { consentManager.crashlyticsConsent = true }
        verify { consentManager.markConsentResponded() }
    }

    @Test
    fun `completion without experience level does nothing`() = runTest {
        // Do NOT set experience level

        viewModel.sideEffect.test {
            viewModel.onIntent(OnboardingIntent.Complete)
            expectNoEvents()
        }
    }

    @Test
    fun `completion failure sets error state`() = runTest {
        coEvery { userProfileRepository.save(any()) } throws RuntimeException("DB error")

        viewModel.onIntent(OnboardingIntent.SetExperienceLevel(ExperienceLevel.BEGINNER))

        viewModel.sideEffect.test {
            viewModel.onIntent(OnboardingIntent.Complete)

            val effect = awaitItem()
            assertTrue(effect is OnboardingSideEffect.ShowError)
        }

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(OnboardingError.SaveFailed, state.completionError)
            assertFalse(state.isCompleting)
        }
    }
}
