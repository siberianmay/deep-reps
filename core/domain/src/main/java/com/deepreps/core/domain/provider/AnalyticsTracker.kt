package com.deepreps.core.domain.provider

/**
 * Analytics tracking interface for the domain layer.
 *
 * Lives in :core:domain with zero knowledge of any analytics SDK (Firebase, etc.).
 * Implementation in :core:data wraps the actual SDK. Hilt binds the implementation.
 *
 * Placement rules:
 * - Screen views: from Composable functions via LaunchedEffect.
 * - User actions: from ViewModel onIntent() handlers.
 * - System events: from domain use cases.
 * - Lifecycle events: from Application or Activity.
 */
interface AnalyticsTracker {

    /** Screen view event. */
    fun trackScreenView(screenName: String, screenClass: String? = null)

    /** User action event with optional parameters. */
    fun trackUserAction(action: String, params: Map<String, Any> = emptyMap())

    /** System-detected event (e.g., deload triggered, PR detected). */
    fun trackSystemEvent(event: String, params: Map<String, Any> = emptyMap())

    /** Lifecycle event (e.g., app start, session resume). */
    fun trackLifecycleEvent(event: String, params: Map<String, Any> = emptyMap())
}
