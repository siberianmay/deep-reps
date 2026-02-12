package com.deepreps.core.data.analytics

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Smoke tests for [NoOpAnalyticsTracker].
 *
 * Verifies that all methods complete without throwing.
 * The no-op implementation must be safe to call in all contexts,
 * including when Firebase is unavailable.
 */
class NoOpAnalyticsTrackerTest {

    private val tracker = NoOpAnalyticsTracker()

    @Test
    @DisplayName("trackScreenView completes without exception")
    fun trackScreenView_doesNotThrow() {
        tracker.trackScreenView("screen", "ScreenClass")
    }

    @Test
    @DisplayName("trackScreenView with null screenClass completes without exception")
    fun trackScreenView_nullClass_doesNotThrow() {
        tracker.trackScreenView("screen")
    }

    @Test
    @DisplayName("trackUserAction completes without exception")
    fun trackUserAction_doesNotThrow() {
        tracker.trackUserAction("event", mapOf("key" to "value"))
    }

    @Test
    @DisplayName("trackUserAction with empty params completes without exception")
    fun trackUserAction_emptyParams_doesNotThrow() {
        tracker.trackUserAction("event")
    }

    @Test
    @DisplayName("trackSystemEvent completes without exception")
    fun trackSystemEvent_doesNotThrow() {
        tracker.trackSystemEvent("event", mapOf("key" to 42))
    }

    @Test
    @DisplayName("trackLifecycleEvent completes without exception")
    fun trackLifecycleEvent_doesNotThrow() {
        tracker.trackLifecycleEvent("event", mapOf("key" to true))
    }
}
