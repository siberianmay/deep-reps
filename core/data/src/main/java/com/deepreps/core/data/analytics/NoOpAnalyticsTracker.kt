package com.deepreps.core.data.analytics

import com.deepreps.core.domain.provider.AnalyticsTracker
import javax.inject.Inject

/**
 * No-op implementation of [AnalyticsTracker].
 *
 * Used when:
 * - Firebase is not configured (no google-services.json)
 * - Analytics consent has not been granted
 * - Debug builds where analytics noise is undesirable
 *
 * All methods are intentionally empty. This class exists so that callers
 * always have a non-null AnalyticsTracker without conditional logic.
 */
class NoOpAnalyticsTracker @Inject constructor() : AnalyticsTracker {

    override fun trackScreenView(screenName: String, screenClass: String?) {
        // Intentionally empty
    }

    override fun trackUserAction(action: String, params: Map<String, Any>) {
        // Intentionally empty
    }

    override fun trackSystemEvent(event: String, params: Map<String, Any>) {
        // Intentionally empty
    }

    override fun trackLifecycleEvent(event: String, params: Map<String, Any>) {
        // Intentionally empty
    }
}
