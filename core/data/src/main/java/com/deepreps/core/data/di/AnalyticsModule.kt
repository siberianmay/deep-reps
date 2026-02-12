package com.deepreps.core.data.di

import android.content.Context
import com.deepreps.core.data.analytics.FirebaseAnalyticsTracker
import com.deepreps.core.data.analytics.NoOpAnalyticsTracker
import com.deepreps.core.data.consent.ConsentManager
import com.deepreps.core.data.featureflags.FirebaseFeatureFlagProvider
import com.deepreps.core.data.featureflags.NoOpFeatureFlagProvider
import com.deepreps.core.domain.provider.AnalyticsTracker
import com.deepreps.core.domain.provider.FeatureFlagProvider
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton

/**
 * Hilt module providing analytics and feature flag bindings.
 *
 * Uses runtime detection to determine if Firebase is available:
 * - If google-services.json is present and Firebase initializes, binds
 *   [FirebaseAnalyticsTracker] and [FirebaseFeatureFlagProvider].
 * - Otherwise, falls back to no-op implementations that silently discard
 *   events and return default flag values.
 *
 * This approach avoids build flavors and compile-time branching. The app
 * always compiles and runs regardless of Firebase configuration state.
 */
@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    @Provides
    @Singleton
    fun provideAnalyticsTracker(
        @ApplicationContext context: Context,
        consentManager: ConsentManager,
    ): AnalyticsTracker {
        return try {
            val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
            FirebaseAnalyticsTracker(
                firebaseAnalytics = firebaseAnalytics,
                consentManager = consentManager,
            )
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            Timber.w("Firebase Analytics not available; using NoOpAnalyticsTracker: %s", e.message)
            NoOpAnalyticsTracker()
        }
    }

    @Provides
    @Singleton
    fun provideFeatureFlagProvider(): FeatureFlagProvider {
        return try {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            FirebaseFeatureFlagProvider(remoteConfig = remoteConfig)
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            Timber.w("Firebase Remote Config not available; using NoOpFeatureFlagProvider: %s", e.message)
            NoOpFeatureFlagProvider()
        }
    }
}
