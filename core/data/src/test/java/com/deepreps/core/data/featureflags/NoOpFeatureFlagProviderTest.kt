package com.deepreps.core.data.featureflags

import com.deepreps.core.domain.provider.FeatureFlags
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Unit tests for [NoOpFeatureFlagProvider].
 *
 * Verifies that the no-op implementation returns stable defaults
 * and that refresh is a safe no-op.
 */
class NoOpFeatureFlagProviderTest {

    private val provider = NoOpFeatureFlagProvider()

    @Test
    @DisplayName("flags returns FeatureFlags defaults")
    fun flags_returnsDefaults() {
        assertThat(provider.flags.value).isEqualTo(FeatureFlags())
    }

    @Test
    @DisplayName("refresh does not change flags")
    fun refresh_doesNotChangeFlags() = runTest {
        val before = provider.flags.value
        provider.refresh()
        val after = provider.flags.value

        assertThat(after).isEqualTo(before)
    }

    @Test
    @DisplayName("refresh does not throw")
    fun refresh_doesNotThrow() = runTest {
        provider.refresh()
    }
}
