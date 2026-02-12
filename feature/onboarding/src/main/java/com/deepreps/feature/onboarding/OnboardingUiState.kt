package com.deepreps.feature.onboarding

import com.deepreps.core.domain.model.enums.ExperienceLevel
import com.deepreps.core.domain.model.enums.Gender
import com.deepreps.core.domain.model.enums.WeightUnit

/**
 * Immutable UI state for the 4-screen onboarding flow.
 *
 * Design spec: design-system.md Section 4.1.
 * Screens: 0 = Consent, 1 = Welcome, 2 = Experience Level, 3 = Unit + Optional Profile.
 */
data class OnboardingUiState(
    val currentStep: Int = 0,
    val totalSteps: Int = 4,

    // Screen 0: Consent
    val analyticsConsent: Boolean = false,
    val crashlyticsConsent: Boolean = false,

    // Screen 2: Experience Level
    val experienceLevel: ExperienceLevel? = null,

    // Screen 3: Unit Preference + Optional Profile
    val weightUnit: WeightUnit = WeightUnit.KG,
    val age: String = "",
    val heightCm: String = "",
    val bodyWeightKg: String = "",
    val gender: Gender? = null,
    val genderDisplayOption: GenderDisplayOption = GenderDisplayOption.UNSELECTED,

    // Completion state
    val isCompleting: Boolean = false,
    val completionError: OnboardingError? = null,
)

/**
 * Gender display options including "Prefer not to say" which maps to null Gender.
 *
 * The domain model [Gender] only has MALE and FEMALE. "Other" and "Prefer not to say"
 * both map to null Gender (baseline calculations use male ratios reduced by 15%).
 */
enum class GenderDisplayOption {
    UNSELECTED,
    MALE,
    FEMALE,
    OTHER,
    PREFER_NOT_TO_SAY;

    fun toDomainGender(): Gender? = when (this) {
        MALE -> Gender.MALE
        FEMALE -> Gender.FEMALE
        OTHER -> null
        PREFER_NOT_TO_SAY -> null
        UNSELECTED -> null
    }
}

/**
 * Typed errors for the onboarding flow.
 */
sealed interface OnboardingError {
    data object SaveFailed : OnboardingError
}
