package com.deepreps.core.data.analytics

import com.deepreps.core.data.consent.ConsentManager
import com.google.firebase.analytics.FirebaseAnalytics
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for [FirebaseAnalyticsTracker].
 *
 * Verifies:
 * - Consent gating: no events when consent is OFF
 * - Event forwarding: correct Firebase calls when consent is ON
 * - Screen view tracking
 * - Parameter mapping
 *
 * Firebase Analytics is mocked -- these tests do not require a Firebase project.
 */
class FirebaseAnalyticsTrackerTest {

    private val firebaseAnalytics: FirebaseAnalytics = mockk(relaxed = true)
    private val consentManager: ConsentManager = mockk()

    private lateinit var tracker: FirebaseAnalyticsTracker

    @BeforeEach
    fun setup() {
        tracker = FirebaseAnalyticsTracker(
            firebaseAnalytics = firebaseAnalytics,
            consentManager = consentManager,
        )
    }

    @Nested
    @DisplayName("Consent gating")
    inner class ConsentGating {

        @Test
        @DisplayName("trackScreenView does nothing when consent is denied")
        fun screenView_consentDenied_noFirebaseCall() {
            every { consentManager.analyticsConsent } returns false

            tracker.trackScreenView("home", "HomeScreen")

            verify(exactly = 0) { firebaseAnalytics.logEvent(any(), any()) }
        }

        @Test
        @DisplayName("trackUserAction does nothing when consent is denied")
        fun userAction_consentDenied_noFirebaseCall() {
            every { consentManager.analyticsConsent } returns false

            tracker.trackUserAction("workout_started", mapOf("exercise_count" to 5))

            verify(exactly = 0) { firebaseAnalytics.logEvent(any(), any()) }
        }

        @Test
        @DisplayName("trackSystemEvent does nothing when consent is denied")
        fun systemEvent_consentDenied_noFirebaseCall() {
            every { consentManager.analyticsConsent } returns false

            tracker.trackSystemEvent("deload_recommended", mapOf("reason" to "fatigue"))

            verify(exactly = 0) { firebaseAnalytics.logEvent(any(), any()) }
        }

        @Test
        @DisplayName("trackLifecycleEvent does nothing when consent is denied")
        fun lifecycleEvent_consentDenied_noFirebaseCall() {
            every { consentManager.analyticsConsent } returns false

            tracker.trackLifecycleEvent("app_session_start", mapOf("has_active_workout" to false))

            verify(exactly = 0) { firebaseAnalytics.logEvent(any(), any()) }
        }
    }

    @Nested
    @DisplayName("Event forwarding with consent granted")
    inner class ConsentGranted {

        @BeforeEach
        fun grantConsent() {
            every { consentManager.analyticsConsent } returns true
        }

        @Test
        @DisplayName("trackScreenView logs screen_view event to Firebase")
        fun screenView_consentGranted_logsToFirebase() {
            tracker.trackScreenView("workout_active", "WorkoutScreen")

            verify(exactly = 1) {
                firebaseAnalytics.logEvent(
                    eq(FirebaseAnalytics.Event.SCREEN_VIEW),
                    any(),
                )
            }
        }

        @Test
        @DisplayName("trackUserAction logs custom event to Firebase")
        fun userAction_consentGranted_logsToFirebase() {
            tracker.trackUserAction("workout_started", mapOf("exercise_count" to 5))

            verify(exactly = 1) {
                firebaseAnalytics.logEvent(eq("workout_started"), any())
            }
        }

        @Test
        @DisplayName("trackSystemEvent logs custom event to Firebase")
        fun systemEvent_consentGranted_logsToFirebase() {
            tracker.trackSystemEvent("personal_record_achieved", mapOf("pr_type" to "weight"))

            verify(exactly = 1) {
                firebaseAnalytics.logEvent(eq("personal_record_achieved"), any())
            }
        }

        @Test
        @DisplayName("trackLifecycleEvent logs custom event to Firebase")
        fun lifecycleEvent_consentGranted_logsToFirebase() {
            tracker.trackLifecycleEvent("app_session_start", mapOf("has_active_workout" to true))

            verify(exactly = 1) {
                firebaseAnalytics.logEvent(eq("app_session_start"), any())
            }
        }

        @Test
        @DisplayName("empty params map still logs event")
        fun emptyParams_logsEvent() {
            tracker.trackUserAction("onboarding_started")

            verify(exactly = 1) {
                firebaseAnalytics.logEvent(eq("onboarding_started"), any())
            }
        }
    }

    @Nested
    @DisplayName("Consent state transitions")
    inner class ConsentTransitions {

        @Test
        @DisplayName("events are blocked after consent is revoked")
        fun consentRevoked_eventsBlocked() {
            // Initially granted
            every { consentManager.analyticsConsent } returns true
            tracker.trackUserAction("event_1")
            verify(exactly = 1) { firebaseAnalytics.logEvent(eq("event_1"), any()) }

            // Consent revoked
            every { consentManager.analyticsConsent } returns false
            tracker.trackUserAction("event_2")
            verify(exactly = 0) { firebaseAnalytics.logEvent(eq("event_2"), any()) }
        }

        @Test
        @DisplayName("events flow again after consent is re-granted")
        fun consentRegranted_eventsFlow() {
            every { consentManager.analyticsConsent } returns false
            tracker.trackUserAction("blocked_event")

            every { consentManager.analyticsConsent } returns true
            tracker.trackUserAction("allowed_event")

            verify(exactly = 0) { firebaseAnalytics.logEvent(eq("blocked_event"), any()) }
            verify(exactly = 1) { firebaseAnalytics.logEvent(eq("allowed_event"), any()) }
        }
    }
}
