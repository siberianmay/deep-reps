package com.deepreps.feature.profile

import com.deepreps.core.domain.model.enums.ExperienceLevel
import com.deepreps.core.domain.model.enums.WeightUnit

/**
 * UI state for the Settings/Profile screen.
 *
 * [bodyWeightKg] is nullable because body weight is optional in the user profile.
 * [bodyWeightDisplay] holds the text field string for the weight input, which may
 * be in kg or lbs depending on [weightUnit]. Conversion happens on save.
 */
@Suppress("ForbiddenPublicDataClass")
data class SettingsUiState(
    val experienceLevel: ExperienceLevel = ExperienceLevel.BEGINNER,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val bodyWeightKg: Double? = null,
    val bodyWeightDisplay: String = "",
    val analyticsConsent: Boolean = false,
    val performanceConsent: Boolean = false,
    val appVersion: String = "1.0.0",
    val isLoading: Boolean = true,
)
