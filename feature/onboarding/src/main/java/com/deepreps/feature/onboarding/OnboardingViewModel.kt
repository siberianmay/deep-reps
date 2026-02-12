package com.deepreps.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepreps.core.data.consent.ConsentManager
import com.deepreps.core.domain.model.enums.ExperienceLevel
import com.deepreps.core.domain.model.enums.WeightUnit
import com.deepreps.core.domain.provider.AnalyticsTracker
import com.deepreps.core.domain.repository.OnboardingRepository
import com.deepreps.core.domain.usecase.CompleteOnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the 4-screen onboarding flow.
 *
 * Screens:
 * - 0: Privacy & Consent
 * - 1: Welcome
 * - 2: Experience Level
 * - 3: Unit Preference + Optional Profile Fields
 *
 * Manages state across all screens. On completion, persists consent, creates
 * the user profile, and sets the onboarding_completed flag.
 *
 * Analytics events (P0, per analytics-plan.md Section 1.2):
 * - onboarding_started: on init
 * - onboarding_step_viewed: on each step transition (screen_name param)
 * - onboarding_consent_granted / onboarding_consent_denied: on consent toggle
 * - onboarding_experience_selected: on experience level selection
 * - onboarding_finished: on successful completion
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val completeOnboardingUseCase: CompleteOnboardingUseCase,
    private val consentManager: ConsentManager,
    private val onboardingRepository: OnboardingRepository,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    private val _sideEffect = Channel<OnboardingSideEffect>(Channel.BUFFERED)
    val sideEffect: Flow<OnboardingSideEffect> = _sideEffect.receiveAsFlow()

    /** Timestamp (SystemClock) when onboarding started, for duration tracking. */
    private val onboardingStartedAtMillis = System.currentTimeMillis()

    init {
        analyticsTracker.trackUserAction(EVENT_ONBOARDING_STARTED, mapOf("screen_name" to "consent"))
    }

    fun onIntent(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.NextStep -> handleNextStep()
            is OnboardingIntent.PreviousStep -> handlePreviousStep()
            is OnboardingIntent.SetAnalyticsConsent -> handleSetAnalyticsConsent(intent.enabled)
            is OnboardingIntent.SetCrashlyticsConsent -> handleSetCrashlyticsConsent(intent.enabled)
            is OnboardingIntent.SetExperienceLevel -> handleSetExperienceLevel(intent.level)
            is OnboardingIntent.SetWeightUnit -> handleSetWeightUnit(intent.unit)
            is OnboardingIntent.SetAge -> handleSetAge(intent.value)
            is OnboardingIntent.SetHeightCm -> handleSetHeightCm(intent.value)
            is OnboardingIntent.SetBodyWeightKg -> handleSetBodyWeightKg(intent.value)
            is OnboardingIntent.SetGender -> handleSetGender(intent.option)
            is OnboardingIntent.Complete -> handleComplete()
        }
    }

    private fun handleNextStep() {
        _state.update { current ->
            if (current.currentStep < current.totalSteps - 1) {
                val nextStep = current.currentStep + 1
                trackStepViewed(nextStep)
                current.copy(currentStep = nextStep)
            } else {
                current
            }
        }
    }

    private fun handlePreviousStep() {
        _state.update { current ->
            if (current.currentStep > 0) {
                val prevStep = current.currentStep - 1
                trackStepViewed(prevStep)
                current.copy(currentStep = prevStep)
            } else {
                current
            }
        }
    }

    private fun handleSetAnalyticsConsent(enabled: Boolean) {
        _state.update { it.copy(analyticsConsent = enabled) }

        val eventName = if (enabled) EVENT_CONSENT_GRANTED else EVENT_CONSENT_DENIED
        analyticsTracker.trackUserAction(eventName, mapOf("consent_type" to "analytics"))
    }

    private fun handleSetCrashlyticsConsent(enabled: Boolean) {
        _state.update { it.copy(crashlyticsConsent = enabled) }

        val eventName = if (enabled) EVENT_CONSENT_GRANTED else EVENT_CONSENT_DENIED
        analyticsTracker.trackUserAction(eventName, mapOf("consent_type" to "crashlytics"))
    }

    private fun handleSetExperienceLevel(level: ExperienceLevel) {
        _state.update { it.copy(experienceLevel = level) }

        analyticsTracker.trackUserAction(
            EVENT_EXPERIENCE_SELECTED,
            mapOf("level" to level.name.lowercase()),
        )
    }

    private fun handleSetWeightUnit(unit: WeightUnit) {
        _state.update { it.copy(weightUnit = unit) }

        analyticsTracker.trackUserAction(
            EVENT_UNIT_SELECTED,
            mapOf("unit" to unit.value),
        )
    }

    private fun handleSetAge(value: String) {
        val filtered = value.filter { it.isDigit() }
        _state.update { it.copy(age = filtered) }
    }

    private fun handleSetHeightCm(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        _state.update { it.copy(heightCm = filtered) }
    }

    private fun handleSetBodyWeightKg(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        _state.update { it.copy(bodyWeightKg = filtered) }
    }

    private fun handleSetGender(option: GenderDisplayOption) {
        _state.update { it.copy(gender = option.toDomainGender(), genderDisplayOption = option) }
    }

    private fun handleComplete() {
        val current = _state.value

        // Experience level is required -- the UI enforces this via disabled button,
        // but guard defensively.
        val experienceLevel = current.experienceLevel ?: return

        _state.update { it.copy(isCompleting = true, completionError = null) }

        viewModelScope.launch {
            try {
                // 1. Save consent preferences
                consentManager.analyticsConsent = current.analyticsConsent
                consentManager.crashlyticsConsent = current.crashlyticsConsent
                consentManager.markConsentResponded()

                // 2. Create user profile
                completeOnboardingUseCase(
                    experienceLevel = experienceLevel,
                    preferredUnit = current.weightUnit,
                    age = current.age.toIntOrNull(),
                    heightCm = current.heightCm.toDoubleOrNull(),
                    gender = current.gender,
                    bodyWeightKg = parseBodyWeight(current.bodyWeightKg, current.weightUnit),
                )

                // 3. Mark onboarding completed (SharedPreferences, NOT Room)
                onboardingRepository.setOnboardingCompleted()

                // 4. Track completion analytics
                val durationSeconds = (System.currentTimeMillis() - onboardingStartedAtMillis) / 1000
                val filledFields = buildFilledFieldsList(current)

                analyticsTracker.trackUserAction(
                    EVENT_ONBOARDING_FINISHED,
                    mapOf(
                        "duration_seconds" to durationSeconds,
                        "fields_filled_count" to filledFields.size,
                        "experience_level" to experienceLevel.name.lowercase(),
                        "unit" to current.weightUnit.value,
                        "analytics_consent" to current.analyticsConsent,
                        "crashlytics_consent" to current.crashlyticsConsent,
                    ),
                )

                _state.update { it.copy(isCompleting = false) }
                _sideEffect.trySend(OnboardingSideEffect.NavigateToMain)
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isCompleting = false,
                        completionError = OnboardingError.SaveFailed,
                    )
                }
                _sideEffect.trySend(
                    OnboardingSideEffect.ShowError("Failed to save profile. Please try again."),
                )
            }
        }
    }

    /**
     * Tracks which onboarding step is being viewed.
     */
    private fun trackStepViewed(step: Int) {
        val screenName = when (step) {
            0 -> "consent"
            1 -> "welcome"
            2 -> "experience_level_select"
            3 -> "unit_profile"
            else -> "unknown_step_$step"
        }
        analyticsTracker.trackUserAction(
            EVENT_STEP_VIEWED,
            mapOf("screen_name" to screenName, "step_index" to step),
        )
    }

    /**
     * Returns a list of optional profile field names that the user filled in.
     */
    private fun buildFilledFieldsList(state: OnboardingUiState): List<String> {
        return buildList {
            if (state.age.isNotBlank()) add("age")
            if (state.heightCm.isNotBlank()) add("height")
            if (state.bodyWeightKg.isNotBlank()) add("body_weight")
            if (state.gender != null) add("gender")
        }
    }

    /**
     * Parses body weight input, converting from lbs to kg if needed.
     * All weights stored internally in kg.
     */
    private fun parseBodyWeight(input: String, unit: WeightUnit): Double? {
        val value = input.toDoubleOrNull() ?: return null
        return when (unit) {
            WeightUnit.KG -> value
            WeightUnit.LBS -> value / LBS_PER_KG
        }
    }

    companion object {
        private const val LBS_PER_KG = 2.20462

        // Event names matching analytics-plan.md taxonomy
        private const val EVENT_ONBOARDING_STARTED = "onboarding_started"
        private const val EVENT_STEP_VIEWED = "onboarding_step_viewed"
        private const val EVENT_CONSENT_GRANTED = "onboarding_consent_granted"
        private const val EVENT_CONSENT_DENIED = "onboarding_consent_denied"
        private const val EVENT_EXPERIENCE_SELECTED = "onboarding_experience_selected"
        private const val EVENT_UNIT_SELECTED = "onboarding_unit_selected"
        private const val EVENT_ONBOARDING_FINISHED = "onboarding_finished"
    }
}
