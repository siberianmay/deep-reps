package com.deepreps.core.data.timer

/**
 * Immutable snapshot of the rest timer's current state.
 *
 * Exposed via [RestTimerManager.state] as a StateFlow.
 * The UI layer reads this to drive the countdown display and bottom sheet visibility.
 */
data class RestTimerState(
    /** Seconds remaining on the countdown. Zero when finished or inactive. */
    val remainingSeconds: Int,
    /** Total seconds the timer was started with (used for progress ring calculation). */
    val totalSeconds: Int,
    /** Whether the timer is actively counting down. */
    val isActive: Boolean,
) {
    /** Progress from 0.0 (expired) to 1.0 (just started). */
    val progress: Float
        get() = if (totalSeconds <= 0) 0f else remainingSeconds.toFloat() / totalSeconds.toFloat()

    /** Whether the timer has finished (was active, reached zero). */
    val isFinished: Boolean
        get() = !isActive && totalSeconds > 0 && remainingSeconds <= 0

    companion object {
        val IDLE = RestTimerState(
            remainingSeconds = 0,
            totalSeconds = 0,
            isActive = false,
        )
    }
}
