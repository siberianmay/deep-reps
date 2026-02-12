package com.deepreps.core.data.timer

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton rest timer manager that drives the countdown between sets.
 *
 * Uses a coroutine-based tick approach (1-second intervals).
 * Emits state via [state] StateFlow that the UI (RestTimerBottomSheet) and
 * WorkoutForegroundService observe.
 *
 * Vibration on timer completion per design-system.md Section 3.4:
 * - Last 10 seconds: 200ms pulse every second
 * - Time's up: 500ms vibration
 *
 * Lifecycle: Scoped as @Singleton so it survives ViewModel recreation.
 * The timer continues when the app is backgrounded because the foreground
 * service keeps the process alive and observes this same instance.
 */
@Singleton
class RestTimerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val externalScope: CoroutineScope,
) {

    private val _state = MutableStateFlow(RestTimerState.IDLE)
    val state: StateFlow<RestTimerState> = _state.asStateFlow()

    private var tickJob: Job? = null

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    /**
     * Starts a new countdown timer with the given duration.
     * Cancels any existing timer first.
     */
    fun start(durationSeconds: Int) {
        cancel()
        _state.value = RestTimerState(
            remainingSeconds = durationSeconds,
            totalSeconds = durationSeconds,
            isActive = true,
        )
        tickJob = externalScope.launch {
            while (isActive && _state.value.remainingSeconds > 0) {
                delay(1_000L)
                _state.update { current ->
                    val newRemaining = (current.remainingSeconds - 1).coerceAtLeast(0)

                    // Vibrate in the last 10 seconds (200ms pulse per tick)
                    if (newRemaining in 1..10) {
                        vibrateShort()
                    }

                    current.copy(remainingSeconds = newRemaining)
                }
            }
            // Timer reached zero
            if (_state.value.remainingSeconds <= 0) {
                vibrateLong()
                _state.update { it.copy(isActive = false) }
            }
        }
    }

    /**
     * Skips the rest timer, stopping the countdown immediately.
     * Resets state to IDLE.
     */
    fun skip() {
        tickJob?.cancel()
        tickJob = null
        _state.value = RestTimerState.IDLE
    }

    /**
     * Extends the current timer by [additionalSeconds].
     * No-op if no timer is active.
     */
    fun extend(additionalSeconds: Int = 30) {
        _state.update { current ->
            if (!current.isActive) return@update current
            current.copy(
                remainingSeconds = current.remainingSeconds + additionalSeconds,
                totalSeconds = current.totalSeconds + additionalSeconds,
            )
        }
    }

    /**
     * Pauses the timer. The countdown freezes at its current value.
     * Design spec: rest timer pauses when workout is paused (design-system.md 4.7.1).
     */
    fun pause() {
        tickJob?.cancel()
        tickJob = null
        // Keep the state as-is but mark inactive for the tick loop.
        // We do NOT change isActive because the UI still shows the timer as "paused".
        // The foreground service and UI read isActive to know if countdown is running.
        // When we resume, we re-start the tick loop from the current remainingSeconds.
    }

    /**
     * Resumes the timer from its current remaining seconds after a pause.
     */
    fun resume() {
        val current = _state.value
        if (!current.isActive || current.remainingSeconds <= 0) return
        // Re-launch the tick loop
        tickJob = externalScope.launch {
            while (isActive && _state.value.remainingSeconds > 0) {
                delay(1_000L)
                _state.update { state ->
                    val newRemaining = (state.remainingSeconds - 1).coerceAtLeast(0)
                    if (newRemaining in 1..10) {
                        vibrateShort()
                    }
                    state.copy(remainingSeconds = newRemaining)
                }
            }
            if (_state.value.remainingSeconds <= 0) {
                vibrateLong()
                _state.update { it.copy(isActive = false) }
            }
        }
    }

    /**
     * Cancels the timer entirely and resets to IDLE.
     */
    fun cancel() {
        tickJob?.cancel()
        tickJob = null
        _state.value = RestTimerState.IDLE
    }

    private fun vibrateShort() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200L, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200L)
        }
    }

    private fun vibrateLong() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500L, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500L)
        }
    }
}
