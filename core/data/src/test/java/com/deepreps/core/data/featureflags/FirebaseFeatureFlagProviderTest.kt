package com.deepreps.core.data.featureflags

import com.deepreps.core.domain.provider.FeatureFlags
import com.google.android.gms.tasks.Tasks
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import com.google.common.truth.Truth.assertThat

/**
 * Unit tests for [FirebaseFeatureFlagProvider].
 *
 * Verifies:
 * - Default values are used when Remote Config is not available
 * - refresh() updates the StateFlow
 * - refresh() failure does not crash or clear existing values
 */
class FirebaseFeatureFlagProviderTest {

    private val remoteConfig: FirebaseRemoteConfig = mockk(relaxed = true)
    private lateinit var provider: FirebaseFeatureFlagProvider

    @BeforeEach
    fun setup() {
        // Default values from Remote Config
        every { remoteConfig.getBoolean("is_ai_plan_enabled") } returns true
        every { remoteConfig.getBoolean("is_block_periodization_enabled") } returns false
        every { remoteConfig.getLong("max_exercises_per_session") } returns 12L

        provider = FirebaseFeatureFlagProvider(remoteConfig = remoteConfig)
    }

    @Nested
    @DisplayName("Default values")
    inner class DefaultValues {

        @Test
        @DisplayName("initial flags match FeatureFlags defaults")
        fun initialFlags_matchDefaults() {
            val flags = provider.flags.value

            assertThat(flags.isAiPlanEnabled).isTrue()
            assertThat(flags.isBlockPeriodizationEnabled).isFalse()
            assertThat(flags.maxExercisesPerSession).isEqualTo(12)
        }

        @Test
        @DisplayName("flags match FeatureFlags data class defaults")
        fun flags_matchDataClassDefaults() {
            val expected = FeatureFlags()
            val actual = provider.flags.value

            assertThat(actual).isEqualTo(expected)
        }
    }

    @Nested
    @DisplayName("Refresh")
    inner class Refresh {

        @Test
        @DisplayName("refresh updates flags from Remote Config")
        fun refresh_updatesFlags() = runTest {
            // Simulate Remote Config returning different values after fetch
            every { remoteConfig.fetchAndActivate() } returns Tasks.forResult(true)
            every { remoteConfig.getBoolean("is_ai_plan_enabled") } returns false
            every { remoteConfig.getLong("max_exercises_per_session") } returns 8L

            provider.refresh()

            val flags = provider.flags.value
            assertThat(flags.isAiPlanEnabled).isFalse()
            assertThat(flags.maxExercisesPerSession).isEqualTo(8)
        }

        @Test
        @DisplayName("refresh failure retains existing values")
        fun refresh_failure_retainsExistingValues() = runTest {
            // Record initial state
            val initialFlags = provider.flags.value

            // Simulate fetch failure
            every { remoteConfig.fetchAndActivate() } returns
                Tasks.forException(RuntimeException("Network unavailable"))

            provider.refresh()

            // Flags should remain unchanged
            assertThat(provider.flags.value).isEqualTo(initialFlags)
        }
    }

    @Nested
    @DisplayName("Configuration")
    inner class Configuration {

        @Test
        @DisplayName("sets default values on Remote Config")
        fun setsDefaults() {
            verify { remoteConfig.setDefaultsAsync(any<Map<String, Any>>()) }
        }

        @Test
        @DisplayName("sets config settings")
        fun setsConfigSettings() {
            verify { remoteConfig.setConfigSettingsAsync(any<FirebaseRemoteConfigSettings>()) }
        }
    }
}
