package com.deepreps.core.domain.usecase

import com.deepreps.core.domain.model.UserProfile
import com.deepreps.core.domain.model.enums.ExperienceLevel
import com.deepreps.core.domain.model.enums.Gender
import com.deepreps.core.domain.model.enums.WeightUnit
import com.deepreps.core.domain.repository.UserProfileRepository

/**
 * Creates the singleton user profile from onboarding data and marks onboarding as complete.
 *
 * The caller (OnboardingViewModel) is responsible for:
 * - Saving consent preferences via ConsentManager (data layer, not domain)
 * - Setting the onboarding_completed flag in SharedPreferences (data layer, not domain)
 *
 * This use case handles only the domain-level operation: persisting the user profile.
 * Pure domain -- no Android dependencies.
 */
class CompleteOnboardingUseCase(
    private val userProfileRepository: UserProfileRepository,
) {

    /**
     * Creates the user profile with onboarding data.
     *
     * @param experienceLevel Selected experience level (required).
     * @param preferredUnit Selected weight unit (required).
     * @param age Optional age in years.
     * @param heightCm Optional height in centimeters.
     * @param gender Optional gender.
     * @param bodyWeightKg Optional body weight in kilograms.
     * @throws IllegalArgumentException if experienceLevel is null (should never happen if UI enforces selection).
     */
    suspend operator fun invoke(
        experienceLevel: ExperienceLevel,
        preferredUnit: WeightUnit,
        age: Int?,
        heightCm: Double?,
        gender: Gender?,
        bodyWeightKg: Double?,
    ) {
        val now = System.currentTimeMillis()
        val profile = UserProfile(
            id = 1,
            experienceLevel = experienceLevel,
            preferredUnit = preferredUnit,
            age = age,
            heightCm = heightCm,
            gender = gender,
            bodyWeightKg = bodyWeightKg,
            createdAt = now,
            updatedAt = now,
        )
        userProfileRepository.save(profile)
    }
}
