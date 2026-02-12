package com.deepreps.core.data.consent

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages user consent for analytics and crash reporting.
 *
 * Uses EncryptedSharedPreferences (NOT Room) because consent state must be readable
 * before the Room database is opened. Firebase Analytics collection must be disabled
 * on Application.onCreate() before any analytics event fires.
 *
 * Defaults to false (opt-in model) per analytics-plan.md.
 */
@Singleton
class ConsentManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private val _analyticsConsent = MutableStateFlow(false)
    private val _crashlyticsConsent = MutableStateFlow(false)

    /** Observable analytics consent state. */
    val analyticsConsentFlow: Flow<Boolean> = _analyticsConsent.asStateFlow()

    /** Observable crashlytics consent state. */
    val crashlyticsConsentFlow: Flow<Boolean> = _crashlyticsConsent.asStateFlow()

    /** Current analytics consent value. Defaults to false. */
    var analyticsConsent: Boolean
        get() = prefs.getBoolean(KEY_ANALYTICS_CONSENT, false)
        set(value) {
            prefs.edit().putBoolean(KEY_ANALYTICS_CONSENT, value).apply()
            _analyticsConsent.value = value
        }

    /** Current crashlytics consent value. Defaults to false. */
    var crashlyticsConsent: Boolean
        get() = prefs.getBoolean(KEY_CRASHLYTICS_CONSENT, false)
        set(value) {
            prefs.edit().putBoolean(KEY_CRASHLYTICS_CONSENT, value).apply()
            _crashlyticsConsent.value = value
        }

    /** Whether the user has responded to the consent screen at all. */
    val hasUserRespondedToConsent: Boolean
        get() = prefs.getBoolean(KEY_CONSENT_RESPONDED, false)

    /** Marks that the user has seen and responded to the consent screen. */
    fun markConsentResponded() {
        prefs.edit().putBoolean(KEY_CONSENT_RESPONDED, true).apply()
    }

    /** Initializes the in-memory state from persisted preferences. Call on app start. */
    fun initialize() {
        _analyticsConsent.value = analyticsConsent
        _crashlyticsConsent.value = crashlyticsConsent
    }

    companion object {
        private const val PREFS_NAME = "consent_prefs"
        private const val KEY_ANALYTICS_CONSENT = "analytics_consent"
        private const val KEY_CRASHLYTICS_CONSENT = "crashlytics_consent"
        private const val KEY_CONSENT_RESPONDED = "consent_responded"
    }
}
