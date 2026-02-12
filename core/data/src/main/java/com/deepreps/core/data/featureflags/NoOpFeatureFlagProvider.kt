package com.deepreps.core.data.featureflags

import com.deepreps.core.domain.provider.FeatureFlagProvider
import com.deepreps.core.domain.provider.FeatureFlags
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * No-op implementation of [FeatureFlagProvider].
 *
 * Returns hardcoded defaults. Used when Firebase is not configured
 * (no google-services.json) or in tests.
 *
 * [refresh] is a no-op -- the flags never change at runtime.
 */
class NoOpFeatureFlagProvider @Inject constructor() : FeatureFlagProvider {

    private val _flags = MutableStateFlow(FeatureFlags())
    override val flags: StateFlow<FeatureFlags> = _flags.asStateFlow()

    override suspend fun refresh() {
        // Intentionally empty -- defaults are always used
    }
}
