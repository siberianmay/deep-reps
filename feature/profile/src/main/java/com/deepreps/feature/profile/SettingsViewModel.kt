package com.deepreps.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepreps.core.data.consent.ConsentManager
import com.deepreps.core.domain.model.enums.ExperienceLevel
import com.deepreps.core.domain.model.enums.WeightUnit
import com.deepreps.core.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Settings/Profile screen.
 *
 * Loads current user profile and consent state on init, then handles
 * each [SettingsIntent] by persisting the change and updating UI state.
 *
 * Body weight is stored internally in kg. When the user's preferred unit
 * is lbs, the display value is converted for the text field and converted
 * back to kg on save.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val consentManager: ConsentManager,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        loadCurrentSettings()
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SetExperienceLevel -> handleSetExperienceLevel(intent.level)
            is SettingsIntent.SetWeightUnit -> handleSetWeightUnit(intent.unit)
            is SettingsIntent.SetBodyWeight -> handleSetBodyWeight(intent.input)
            is SettingsIntent.SetAnalyticsConsent -> handleSetAnalyticsConsent(intent.enabled)
            is SettingsIntent.SetPerformanceConsent -> handleSetPerformanceConsent(intent.enabled)
        }
    }

    private fun loadCurrentSettings() {
        viewModelScope.launch {
            try {
                val profile = userProfileRepository.get()
                val analyticsEnabled = consentManager.analyticsConsent
                val performanceEnabled = consentManager.crashlyticsConsent

                if (profile != null) {
                    val displayWeight = formatWeightForDisplay(
                        profile.bodyWeightKg,
                        profile.preferredUnit,
                    )
                    _state.update {
                        it.copy(
                            experienceLevel = profile.experienceLevel,
                            weightUnit = profile.preferredUnit,
                            bodyWeightKg = profile.bodyWeightKg,
                            bodyWeightDisplay = displayWeight,
                            analyticsConsent = analyticsEnabled,
                            performanceConsent = performanceEnabled,
                            isLoading = false,
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            analyticsConsent = analyticsEnabled,
                            performanceConsent = performanceEnabled,
                            isLoading = false,
                        )
                    }
                }
            } catch (_: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun handleSetExperienceLevel(level: ExperienceLevel) {
        _state.update { it.copy(experienceLevel = level) }
        saveProfile()
    }

    private fun handleSetWeightUnit(unit: WeightUnit) {
        val current = _state.value
        val newDisplay = formatWeightForDisplay(current.bodyWeightKg, unit)
        _state.update { it.copy(weightUnit = unit, bodyWeightDisplay = newDisplay) }
        saveProfile()
    }

    private fun handleSetBodyWeight(input: String) {
        val filtered = input.filter { it.isDigit() || it == '.' }
        val currentUnit = _state.value.weightUnit
        val kg = parseToKg(filtered, currentUnit)
        _state.update { it.copy(bodyWeightDisplay = filtered, bodyWeightKg = kg) }
        saveProfile()
    }

    private fun handleSetAnalyticsConsent(enabled: Boolean) {
        consentManager.analyticsConsent = enabled
        _state.update { it.copy(analyticsConsent = enabled) }
    }

    private fun handleSetPerformanceConsent(enabled: Boolean) {
        consentManager.crashlyticsConsent = enabled
        _state.update { it.copy(performanceConsent = enabled) }
    }

    private fun saveProfile() {
        viewModelScope.launch {
            try {
                val current = _state.value
                val existingProfile = userProfileRepository.get() ?: return@launch
                val updatedProfile = existingProfile.copy(
                    experienceLevel = current.experienceLevel,
                    preferredUnit = current.weightUnit,
                    bodyWeightKg = current.bodyWeightKg,
                    updatedAt = System.currentTimeMillis(),
                )
                userProfileRepository.save(updatedProfile)
            } catch (_: Exception) {
                // Silent failure for settings save -- profile data is non-critical
                // and will be retried on next change.
            }
        }
    }

    companion object {
        private const val LBS_PER_KG = 2.20462

        private fun parseToKg(input: String, unit: WeightUnit): Double? {
            val value = input.toDoubleOrNull() ?: return null
            return when (unit) {
                WeightUnit.KG -> value
                WeightUnit.LBS -> value / LBS_PER_KG
            }
        }

        private fun formatWeightForDisplay(weightKg: Double?, unit: WeightUnit): String {
            if (weightKg == null) return ""
            return when (unit) {
                WeightUnit.KG -> "%.1f".format(weightKg)
                WeightUnit.LBS -> "%.1f".format(weightKg * LBS_PER_KG)
            }
        }
    }
}
