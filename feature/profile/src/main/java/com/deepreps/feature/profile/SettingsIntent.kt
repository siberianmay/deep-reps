package com.deepreps.feature.profile

import android.net.Uri
import com.deepreps.core.domain.model.enums.ExperienceLevel
import com.deepreps.core.domain.model.enums.WeightUnit

/**
 * User intents for the Settings/Profile screen.
 */
sealed interface SettingsIntent {
    data class SetExperienceLevel(val level: ExperienceLevel) : SettingsIntent
    data class SetWeightUnit(val unit: WeightUnit) : SettingsIntent
    data class SetBodyWeight(val input: String) : SettingsIntent
    data class SetCompoundRepRange(val min: Int, val max: Int) : SettingsIntent
    data class SetIsolationRepRange(val min: Int, val max: Int) : SettingsIntent
    data class SetDefaultWorkingSets(val count: Int) : SettingsIntent
    data class SetAnalyticsConsent(val enabled: Boolean) : SettingsIntent
    data class SetPerformanceConsent(val enabled: Boolean) : SettingsIntent
    data object ExportData : SettingsIntent
    data object RequestImport : SettingsIntent
    data object DismissImportDialog : SettingsIntent
    data class ImportData(val uri: Uri) : SettingsIntent
}
