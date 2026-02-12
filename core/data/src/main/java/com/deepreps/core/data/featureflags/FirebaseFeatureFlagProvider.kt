package com.deepreps.core.data.featureflags

import com.deepreps.core.domain.provider.FeatureFlagProvider
import com.deepreps.core.domain.provider.FeatureFlags
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Remote Config implementation of [FeatureFlagProvider].
 *
 * On [refresh], fetches from Firebase Remote Config and activates.
 * If fetch fails (offline, quota exceeded), the last activated values
 * or compiled defaults are used -- the app never crashes from missing flags.
 *
 * Default values are set both as in-code defaults on [FirebaseRemoteConfig]
 * and as the initial [FeatureFlags] data class values, providing two layers
 * of fallback.
 */
@Singleton
class FirebaseFeatureFlagProvider @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
) : FeatureFlagProvider {

    private val _flags = MutableStateFlow(FeatureFlags())
    override val flags: StateFlow<FeatureFlags> = _flags.asStateFlow()

    init {
        configureDefaults()
        // Read any previously activated values immediately
        _flags.value = readCurrentFlags()
    }

    override suspend fun refresh() {
        try {
            remoteConfig.fetchAndActivate().await()
            _flags.value = readCurrentFlags()
            Timber.d("Feature flags refreshed: %s", _flags.value)
        } catch (e: Exception) {
            Timber.w(e, "Feature flag refresh failed; using cached/default values")
            // _flags retains its current value -- no crash, no empty state
        }
    }

    private fun configureDefaults() {
        val defaults = mapOf<String, Any>(
            KEY_AI_PLAN_ENABLED to true,
            KEY_BLOCK_PERIODIZATION_ENABLED to false,
            KEY_MAX_EXERCISES_PER_SESSION to MAX_EXERCISES_DEFAULT.toLong(),
        )
        remoteConfig.setDefaultsAsync(defaults)

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(MINIMUM_FETCH_INTERVAL_SECONDS)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    private fun readCurrentFlags(): FeatureFlags = FeatureFlags(
        isAiPlanEnabled = remoteConfig.getBoolean(KEY_AI_PLAN_ENABLED),
        isBlockPeriodizationEnabled = remoteConfig.getBoolean(KEY_BLOCK_PERIODIZATION_ENABLED),
        maxExercisesPerSession = remoteConfig.getLong(KEY_MAX_EXERCISES_PER_SESSION).toInt(),
    )

    companion object {
        private const val KEY_AI_PLAN_ENABLED = "is_ai_plan_enabled"
        private const val KEY_BLOCK_PERIODIZATION_ENABLED = "is_block_periodization_enabled"
        private const val KEY_MAX_EXERCISES_PER_SESSION = "max_exercises_per_session"
        private const val MAX_EXERCISES_DEFAULT = 12

        /**
         * Minimum fetch interval in seconds.
         * Production: 12 hours (43200s) to stay within free tier quotas.
         * Debug builds can override via FirebaseRemoteConfigSettings.
         */
        private const val MINIMUM_FETCH_INTERVAL_SECONDS = 43200L
    }
}
