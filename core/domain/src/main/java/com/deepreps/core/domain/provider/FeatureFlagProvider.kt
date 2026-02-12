package com.deepreps.core.domain.provider

import kotlinx.coroutines.flow.StateFlow

/**
 * Feature flag provider interface.
 *
 * Lives in :core:domain. Implementation in :core:data wraps Firebase Remote Config.
 * Feature flags control runtime behavior without code changes or app updates.
 */
interface FeatureFlagProvider {

    /** Current snapshot of all feature flags. Immutable. */
    val flags: StateFlow<FeatureFlags>

    /** Fetches latest flags from remote config. Call on app start and periodically. */
    suspend fun refresh()
}

/**
 * Immutable snapshot of all feature flags.
 *
 * Defaults are the fallback when remote config is unavailable (offline).
 */
@Suppress("ForbiddenPublicDataClass")
data class FeatureFlags(
    val isAiPlanEnabled: Boolean = true,
    val isBlockPeriodizationEnabled: Boolean = false,
    val maxExercisesPerSession: Int = 12,
)
