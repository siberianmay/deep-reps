package com.deepreps.core.domain.usecase

import com.deepreps.core.domain.model.WorkoutSession
import javax.inject.Inject

/**
 * Orchestrates the full session recovery flow on app startup.
 *
 * Execution order:
 * 1. [CleanupStaleSessionsUseCase] -- mark 24h+ sessions as ABANDONED
 * 2. [DetectAbandonedSessionUseCase] -- check for active/paused sessions
 *
 * Returns a [SessionRecoveryResult] that the UI layer uses to decide
 * whether to show the "Resume or Discard?" dialog.
 */
class SessionRecoveryUseCase @Inject constructor(
    private val cleanupStaleSessionsUseCase: CleanupStaleSessionsUseCase,
    private val detectAbandonedSessionUseCase: DetectAbandonedSessionUseCase,
) {

    /**
     * @param currentTimeMillis injectable for testing.
     * @return [SessionRecoveryResult] describing the recovery action needed.
     */
    suspend operator fun invoke(
        currentTimeMillis: Long = System.currentTimeMillis(),
    ): SessionRecoveryResult {
        // Step 1: Clean up sessions older than 24 hours
        cleanupStaleSessionsUseCase(currentTimeMillis)

        // Step 2: Check for recoverable sessions
        val activeSession = detectAbandonedSessionUseCase()
            ?: return SessionRecoveryResult.NoRecoveryNeeded

        return SessionRecoveryResult.RecoverableSessionFound(activeSession)
    }
}

/**
 * Outcome of the startup session recovery check.
 */
sealed interface SessionRecoveryResult {
    /** No active/paused sessions found. Normal app startup. */
    data object NoRecoveryNeeded : SessionRecoveryResult

    /** An active or paused session was found. Show resume/discard dialog. */
    @Suppress("ForbiddenPublicDataClass")
    data class RecoverableSessionFound(val session: WorkoutSession) : SessionRecoveryResult
}
