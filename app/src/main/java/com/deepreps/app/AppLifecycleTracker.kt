package com.deepreps.app

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.deepreps.core.domain.provider.AnalyticsTracker
import com.deepreps.core.domain.repository.WorkoutSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks app-level lifecycle events using [ProcessLifecycleOwner].
 *
 * Events (per analytics-plan.md Section 1.8):
 * - app_session_start: app brought to foreground (ON_START)
 * - app_session_end: app backgrounded (ON_STOP), with session duration + screens viewed
 * - app_crash_detected: detected by [MainViewModel] during session recovery, not here
 *
 * This class is a singleton initialized once in [DeepRepsApplication.onCreate].
 * It registers itself on the process lifecycle, which survives activity
 * recreation but fires ON_STOP when the entire app goes to background.
 */
@Singleton
class AppLifecycleTracker @Inject constructor(
    private val analyticsTracker: AnalyticsTracker,
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val applicationScope: CoroutineScope,
) : DefaultLifecycleObserver {

    /** Millis timestamp when the current foreground session started. */
    private var sessionStartTimeMillis: Long = 0L

    /** Millis timestamp of the last app_session_end, for calculating time since last session. */
    private var lastSessionEndTimeMillis: Long = 0L

    /** Accumulated screen names visited during this foreground session. */
    private val screensViewed = mutableListOf<String>()

    /**
     * Call once from Application.onCreate() to register on ProcessLifecycleOwner.
     */
    fun register() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        Timber.d("AppLifecycleTracker registered")
    }

    override fun onStart(owner: LifecycleOwner) {
        sessionStartTimeMillis = System.currentTimeMillis()
        screensViewed.clear()

        val timeSinceLastHours = if (lastSessionEndTimeMillis > 0) {
            (sessionStartTimeMillis - lastSessionEndTimeMillis) / 3_600_000.0
        } else {
            -1.0 // First session, no previous data
        }

        applicationScope.launch {
            @Suppress("TooGenericExceptionCaught")
            val hasActiveWorkout = try {
                workoutSessionRepository.getActiveSession() != null
            } catch (e: Exception) {
                Timber.w(e, "Failed to check active workout for session start event")
                false
            }

            analyticsTracker.trackLifecycleEvent(
                EVENT_APP_SESSION_START,
                buildMap {
                    if (timeSinceLastHours >= 0) {
                        put("time_since_last_session_hours", timeSinceLastHours)
                    }
                    put("has_active_workout", hasActiveWorkout)
                },
            )
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        val sessionDurationSeconds = if (sessionStartTimeMillis > 0) {
            (System.currentTimeMillis() - sessionStartTimeMillis) / 1000
        } else {
            0L
        }

        lastSessionEndTimeMillis = System.currentTimeMillis()

        analyticsTracker.trackLifecycleEvent(
            EVENT_APP_SESSION_END,
            mapOf(
                "session_duration_seconds" to sessionDurationSeconds,
                "screens_viewed" to screensViewed.distinct().joinToString(","),
            ),
        )
    }

    /**
     * Called by screen-level composables or ViewModels to register screen views
     * for the session-level screens_viewed parameter.
     */
    fun recordScreenViewed(screenName: String) {
        screensViewed.add(screenName)
    }

    companion object {
        private const val EVENT_APP_SESSION_START = "app_session_start"
        private const val EVENT_APP_SESSION_END = "app_session_end"
    }
}
