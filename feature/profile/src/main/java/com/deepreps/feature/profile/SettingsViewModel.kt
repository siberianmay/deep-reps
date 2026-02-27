package com.deepreps.feature.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepreps.core.data.consent.ConsentManager
import com.deepreps.core.data.export.DataExporter
import com.deepreps.core.data.export.DataImporter
import com.deepreps.core.domain.model.UserProfile
import com.deepreps.core.domain.model.enums.ExperienceLevel
import com.deepreps.core.domain.model.enums.WeightUnit
import com.deepreps.core.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
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
@Suppress("TooManyFunctions")
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val userProfileRepository: UserProfileRepository,
    private val consentManager: ConsentManager,
    private val dataExporter: DataExporter,
    private val dataImporter: DataImporter,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    private val _sideEffect = Channel<SettingsSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        loadCurrentSettings()
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SetExperienceLevel -> handleSetExperienceLevel(intent.level)
            is SettingsIntent.SetWeightUnit -> handleSetWeightUnit(intent.unit)
            is SettingsIntent.SetBodyWeight -> handleSetBodyWeight(intent.input)
            is SettingsIntent.SetCompoundRepRange -> handleSetCompoundRepRange(intent.min, intent.max)
            is SettingsIntent.SetIsolationRepRange -> handleSetIsolationRepRange(intent.min, intent.max)
            is SettingsIntent.SetDefaultWorkingSets -> handleSetDefaultWorkingSets(intent.count)
            is SettingsIntent.SetAnalyticsConsent -> handleSetAnalyticsConsent(intent.enabled)
            is SettingsIntent.SetPerformanceConsent -> handleSetPerformanceConsent(intent.enabled)
            is SettingsIntent.ExportData -> handleExport()
            is SettingsIntent.RequestImport -> handleRequestImport()
            is SettingsIntent.DismissImportDialog -> handleDismissImportDialog()
            is SettingsIntent.ImportData -> handleImport(intent.uri)
        }
    }

    // -------------------------------------------------------------------------
    // Settings load / save
    // -------------------------------------------------------------------------

    private fun loadCurrentSettings() {
        viewModelScope.launch {
            try {
                val profile = userProfileRepository.get()
                val consentState = loadConsentState()

                if (profile != null) {
                    _state.update { applyProfileToState(it, profile, consentState) }
                } else {
                    _state.update {
                        it.copy(
                            analyticsConsent = consentState.first,
                            performanceConsent = consentState.second,
                            isLoading = false,
                        )
                    }
                }
            } catch (_: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadConsentState(): Pair<Boolean, Boolean> =
        consentManager.analyticsConsent to consentManager.crashlyticsConsent

    private fun applyProfileToState(
        current: SettingsUiState,
        profile: UserProfile,
        consentState: Pair<Boolean, Boolean>,
    ): SettingsUiState {
        val displayWeight = formatWeightForDisplay(profile.bodyWeightKg, profile.preferredUnit)
        return current.copy(
            experienceLevel = profile.experienceLevel,
            weightUnit = profile.preferredUnit,
            bodyWeightKg = profile.bodyWeightKg,
            bodyWeightDisplay = displayWeight,
            compoundRepMin = profile.compoundRepMin,
            compoundRepMax = profile.compoundRepMax,
            isolationRepMin = profile.isolationRepMin,
            isolationRepMax = profile.isolationRepMax,
            defaultWorkingSets = profile.defaultWorkingSets,
            analyticsConsent = consentState.first,
            performanceConsent = consentState.second,
            isLoading = false,
        )
    }

    private fun handleSetExperienceLevel(level: ExperienceLevel) {
        val defaults = ExperienceLevel.defaultRepRanges(level)
        _state.update {
            it.copy(
                experienceLevel = level,
                compoundRepMin = defaults.compoundRepMin,
                compoundRepMax = defaults.compoundRepMax,
                isolationRepMin = defaults.isolationRepMin,
                isolationRepMax = defaults.isolationRepMax,
            )
        }
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

    private fun handleSetCompoundRepRange(min: Int, max: Int) {
        val clamped = clampRepRange(min, max)
        _state.update { it.copy(compoundRepMin = clamped.first, compoundRepMax = clamped.second) }
        saveProfile()
    }

    private fun handleSetIsolationRepRange(min: Int, max: Int) {
        val clamped = clampRepRange(min, max)
        _state.update { it.copy(isolationRepMin = clamped.first, isolationRepMax = clamped.second) }
        saveProfile()
    }

    private fun handleSetDefaultWorkingSets(count: Int) {
        _state.update { it.copy(defaultWorkingSets = count) }
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
                    compoundRepMin = current.compoundRepMin,
                    compoundRepMax = current.compoundRepMax,
                    isolationRepMin = current.isolationRepMin,
                    isolationRepMax = current.isolationRepMax,
                    defaultWorkingSets = current.defaultWorkingSets,
                    updatedAt = System.currentTimeMillis(),
                )
                userProfileRepository.save(updatedProfile)
            } catch (_: Exception) {
                // Silent failure for settings save -- profile data is non-critical
                // and will be retried on next change.
            }
        }
    }

    // -------------------------------------------------------------------------
    // Export / Import
    // -------------------------------------------------------------------------

    private fun handleExport() {
        if (_state.value.isExporting) return
        _state.update { it.copy(isExporting = true) }

        viewModelScope.launch {
            try {
                val file = dataExporter.exportToZip(application.cacheDir)
                _state.update { it.copy(isExporting = false) }
                _sideEffect.send(SettingsSideEffect.ExportReady(file))
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                Timber.e(e, "Export failed")
                _state.update { it.copy(isExporting = false) }
                _sideEffect.send(SettingsSideEffect.ShowError("Export failed: ${e.message}"))
            }
        }
    }

    private fun handleRequestImport() {
        _state.update { it.copy(showImportConfirmDialog = true) }
    }

    private fun handleDismissImportDialog() {
        _state.update { it.copy(showImportConfirmDialog = false) }
    }

    private fun handleImport(uri: Uri) {
        if (_state.value.isImporting) return
        _state.update { it.copy(isImporting = true, showImportConfirmDialog = false) }

        viewModelScope.launch {
            try {
                val contentResolver = application.contentResolver
                val mimeType = contentResolver.getType(uri)
                val inputStream = contentResolver.openInputStream(uri)
                    ?: throw IllegalStateException("Could not open file")

                val result = inputStream.use { stream ->
                    if (mimeType == "application/json") {
                        val jsonString = stream.bufferedReader().readText()
                        dataImporter.importFromJson(jsonString)
                    } else {
                        dataImporter.importFromZip(stream)
                    }
                }

                _state.update { it.copy(isImporting = false) }

                if (result.success) {
                    _sideEffect.send(SettingsSideEffect.ImportComplete(result))
                    // Reload settings since the imported data may have changed the profile.
                    loadCurrentSettings()
                } else {
                    _sideEffect.send(
                        SettingsSideEffect.ShowError(result.error ?: "Import failed"),
                    )
                }
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                Timber.e(e, "Import failed")
                _state.update { it.copy(isImporting = false) }
                _sideEffect.send(SettingsSideEffect.ShowError("Import failed: ${e.message}"))
            }
        }
    }

    companion object {
        private const val LBS_PER_KG = 2.20462
        private const val REP_MIN = 1
        private const val REP_MAX = 30
        private const val REP_GAP = 2

        /**
         * Clamps a rep range so that min is in [1..30], max is in [1..30],
         * and min is at least [REP_GAP] less than max.
         */
        private fun clampRepRange(min: Int, max: Int): Pair<Int, Int> {
            val clampedMin = min.coerceIn(REP_MIN, REP_MAX - REP_GAP)
            val clampedMax = max.coerceIn(clampedMin + REP_GAP, REP_MAX)
            return clampedMin to clampedMax
        }

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
