package com.deepreps.app

import android.app.Application
import com.deepreps.core.data.consent.ConsentManager
import com.deepreps.core.domain.provider.AnalyticsTracker
import com.deepreps.core.domain.provider.FeatureFlagProvider
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Application entry point for Deep Reps.
 *
 * Initialization order (non-negotiable):
 * 1. Timber (logging must be available for all subsequent init).
 * 2. ConsentManager.initialize() (reads persisted consent from EncryptedSharedPreferences).
 * 3. Firebase collection disabled by default (analytics OFF, crashlytics OFF).
 * 4. Observe consent flows and enable/disable collection reactively.
 * 5. Feature flags refresh (best-effort, uses defaults if offline).
 *
 * Firebase Analytics and Crashlytics are DISABLED by default. They are only
 * enabled after the user explicitly grants consent in the onboarding flow
 * or settings. This is enforced by calling setAnalyticsCollectionEnabled(false)
 * and setCrashlyticsCollectionEnabled(false) before any events can fire.
 */
@HiltAndroidApp
class DeepRepsApplication : Application() {

    @Inject
    lateinit var consentManager: ConsentManager

    @Inject
    lateinit var analyticsTracker: AnalyticsTracker

    @Inject
    lateinit var featureFlagProvider: FeatureFlagProvider

    @Inject
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var appLifecycleTracker: AppLifecycleTracker

    override fun onCreate() {
        super.onCreate()
        initTimber()
        initConsent()
        initFirebaseCollectionDefaults()
        observeConsentChanges()
        refreshFeatureFlags()
        appLifecycleTracker.register()
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    /**
     * Hydrates ConsentManager's in-memory StateFlows from persisted preferences.
     * Must run before any Firebase collection toggle.
     */
    private fun initConsent() {
        consentManager.initialize()
    }

    /**
     * Disables Firebase Analytics and Crashlytics collection by default.
     * Then immediately re-enables if consent was previously granted.
     *
     * This ensures no data is collected between Application.onCreate() and
     * the consent check -- even if the app is killed and restarted.
     */
    private fun initFirebaseCollectionDefaults() {
        try {
            val firebaseAnalytics = com.google.firebase.analytics.FirebaseAnalytics.getInstance(this)
            firebaseAnalytics.setAnalyticsCollectionEnabled(false)

            val crashlytics = com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
            crashlytics.setCrashlyticsCollectionEnabled(false)

            // If consent was previously granted, enable immediately
            if (consentManager.analyticsConsent) {
                firebaseAnalytics.setAnalyticsCollectionEnabled(true)
                Timber.d("Analytics collection enabled (previously consented)")
            }
            if (consentManager.crashlyticsConsent) {
                crashlytics.setCrashlyticsCollectionEnabled(true)
                Timber.d("Crashlytics collection enabled (previously consented)")
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            Timber.w("Firebase not available; skipping collection toggle: %s", e.message)
        }
    }

    /**
     * Observes consent state changes and toggles Firebase collection accordingly.
     * This handles the case where the user changes consent in settings or during
     * onboarding -- the change takes effect immediately without app restart.
     */
    private fun observeConsentChanges() {
        applicationScope.launch {
            consentManager.analyticsConsentFlow
                .distinctUntilChanged()
                .collect { enabled ->
                    try {
                        com.google.firebase.analytics.FirebaseAnalytics
                            .getInstance(this@DeepRepsApplication)
                            .setAnalyticsCollectionEnabled(enabled)
                        Timber.d("Analytics collection toggled: %s", enabled)
                    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                        Timber.w("Failed to toggle analytics collection: %s", e.message)
                    }
                }
        }

        applicationScope.launch {
            consentManager.crashlyticsConsentFlow
                .distinctUntilChanged()
                .collect { enabled ->
                    try {
                        com.google.firebase.crashlytics.FirebaseCrashlytics
                            .getInstance()
                            .setCrashlyticsCollectionEnabled(enabled)
                        Timber.d("Crashlytics collection toggled: %s", enabled)
                    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                        Timber.w("Failed to toggle crashlytics collection: %s", e.message)
                    }
                }
        }
    }

    /**
     * Best-effort refresh of feature flags on app start.
     * If offline or Remote Config unavailable, defaults are used.
     */
    private fun refreshFeatureFlags() {
        applicationScope.launch {
            featureFlagProvider.refresh()
        }
    }
}
