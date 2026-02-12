package com.deepreps.feature.onboarding

import com.deepreps.core.domain.model.enums.ExperienceLevel
import com.deepreps.core.domain.model.enums.WeightUnit

/**
 * User intents for the onboarding flow.
 */
sealed interface OnboardingIntent {

    // Navigation
    data object NextStep : OnboardingIntent
    data object PreviousStep : OnboardingIntent

    // Screen 0: Consent
    data class SetAnalyticsConsent(val enabled: Boolean) : OnboardingIntent
    data class SetCrashlyticsConsent(val enabled: Boolean) : OnboardingIntent

    // Screen 2: Experience Level
    data class SetExperienceLevel(val level: ExperienceLevel) : OnboardingIntent

    // Screen 3: Unit + Profile
    data class SetWeightUnit(val unit: WeightUnit) : OnboardingIntent
    data class SetAge(val value: String) : OnboardingIntent
    data class SetHeightCm(val value: String) : OnboardingIntent
    data class SetBodyWeightKg(val value: String) : OnboardingIntent
    data class SetGender(val option: GenderDisplayOption) : OnboardingIntent

    /** Skip optional profile fields and complete onboarding. */
    data object Complete : OnboardingIntent
}
