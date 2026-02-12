package com.deepreps.core.data.analytics

import com.deepreps.core.data.consent.ConsentManager
import com.deepreps.core.domain.provider.AnalyticsTracker
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Analytics implementation of [AnalyticsTracker].
 *
 * Consent-gated: every logging call checks [ConsentManager.analyticsConsent]
 * before forwarding to the Firebase SDK. If consent is denied, the event is
 * silently dropped (no queuing, no retry).
 *
 * This is the ONLY class in the codebase that imports Firebase Analytics SDK
 * types. All other modules interact via the [AnalyticsTracker] interface
 * in :core:domain.
 *
 * Firebase event name constraints:
 * - Max 40 characters
 * - Must start with alphabetic character
 * - Only alphanumeric + underscores
 * - Max 25 custom parameters per event
 * - Parameter key max 40 characters, string value max 100 characters
 */
@Singleton
class FirebaseAnalyticsTracker @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val consentManager: ConsentManager,
) : AnalyticsTracker {

    override fun trackScreenView(screenName: String, screenClass: String?) {
        if (!isConsentGranted()) return

        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName.truncate(PARAM_STRING_MAX))
            if (screenClass != null) {
                param(
                    FirebaseAnalytics.Param.SCREEN_CLASS,
                    screenClass.truncate(PARAM_STRING_MAX),
                )
            }
        }

        Timber.d("Analytics: screen_view [%s]", screenName)
    }

    override fun trackUserAction(action: String, params: Map<String, Any>) {
        if (!isConsentGranted()) return

        val sanitizedName = sanitizeEventName(action)
        firebaseAnalytics.logEvent(sanitizedName) {
            params.entries.take(MAX_PARAMS).forEach { (key, value) ->
                putParam(sanitizeParamKey(key), value)
            }
        }

        Timber.d("Analytics: user_action [%s] params=%s", sanitizedName, params.keys)
    }

    override fun trackSystemEvent(event: String, params: Map<String, Any>) {
        if (!isConsentGranted()) return

        val sanitizedName = sanitizeEventName(event)
        firebaseAnalytics.logEvent(sanitizedName) {
            params.entries.take(MAX_PARAMS).forEach { (key, value) ->
                putParam(sanitizeParamKey(key), value)
            }
        }

        Timber.d("Analytics: system_event [%s] params=%s", sanitizedName, params.keys)
    }

    override fun trackLifecycleEvent(event: String, params: Map<String, Any>) {
        if (!isConsentGranted()) return

        val sanitizedName = sanitizeEventName(event)
        firebaseAnalytics.logEvent(sanitizedName) {
            params.entries.take(MAX_PARAMS).forEach { (key, value) ->
                putParam(sanitizeParamKey(key), value)
            }
        }

        Timber.d("Analytics: lifecycle_event [%s] params=%s", sanitizedName, params.keys)
    }

    private fun isConsentGranted(): Boolean = consentManager.analyticsConsent

    companion object {
        /** Firebase max parameter string value length. */
        private const val PARAM_STRING_MAX = 100

        /** Firebase max event name length. */
        private const val EVENT_NAME_MAX = 40

        /** Firebase max parameter key length. */
        private const val PARAM_KEY_MAX = 40

        /** Firebase max custom parameters per event. */
        private const val MAX_PARAMS = 25
    }
}

/**
 * Sanitizes an event name to conform to Firebase constraints:
 * max 40 chars, alphanumeric + underscores only, starts with alpha.
 */
private fun sanitizeEventName(name: String): String {
    val cleaned = name
        .replace(Regex("[^a-zA-Z0-9_]"), "_")
        .take(40)
    // Ensure starts with alphabetic character
    return if (cleaned.isNotEmpty() && cleaned[0].isLetter()) {
        cleaned
    } else {
        "evt_$cleaned".take(40)
    }
}

/**
 * Sanitizes a parameter key to conform to Firebase constraints.
 */
private fun sanitizeParamKey(key: String): String {
    return key
        .replace(Regex("[^a-zA-Z0-9_]"), "_")
        .take(40)
}

/**
 * Truncates a string to the given max length.
 */
private fun String.truncate(maxLength: Int): String {
    return if (length > maxLength) take(maxLength) else this
}

/**
 * Extension to add a parameter with type-safe dispatch.
 *
 * Firebase only accepts String, Long, and Double as parameter values.
 * Other types are converted to String.
 */
private fun com.google.firebase.analytics.ParametersBuilder.putParam(
    key: String,
    value: Any,
) {
    when (value) {
        is String -> param(key, value.truncate(100))
        is Long -> param(key, value)
        is Int -> param(key, value.toLong())
        is Double -> param(key, value)
        is Float -> param(key, value.toDouble())
        is Boolean -> param(key, if (value) 1L else 0L)
        else -> param(key, value.toString().truncate(100))
    }
}
