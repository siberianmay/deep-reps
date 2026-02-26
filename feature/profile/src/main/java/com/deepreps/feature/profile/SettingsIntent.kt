package com.deepreps.feature.profile

import com.deepreps.core.domain.model.enums.ExperienceLevel
import com.deepreps.core.domain.model.enums.WeightUnit

/**
 * User intents for the Settings/Profile screen.
 */
sealed interface SettingsIntent {
    data class SetExperienceLevel(val level: ExperienceLevel) : SettingsIntent
    data class SetWeightUnit(val unit: WeightUnit) : SettingsIntent
    data class SetBodyWeight(val input: String) : SettingsIntent
    data class SetAnalyticsConsent(val enabled: Boolean) : SettingsIntent
    data class SetPerformanceConsent(val enabled: Boolean) : SettingsIntent
}
