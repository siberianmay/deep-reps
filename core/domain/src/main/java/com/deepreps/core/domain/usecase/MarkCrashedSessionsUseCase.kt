package com.deepreps.core.domain.usecase

import com.deepreps.core.domain.model.enums.SessionStatus
import com.deepreps.core.domain.repository.WorkoutSessionRepository
import javax.inject.Inject

/**
 * Detects sessions that were left in "active" status after an app crash or force-stop
 * and marks them as [SessionStatus.CRASHED].
 *
 * This runs on cold app startup (Application.onCreate or Activity.onCreate with
 * savedInstanceState == null). The distinction from [DetectAbandonedSessionUseCase]:
 *
 * - This use case MARKS the session status as CRASHED before the UI shows anything.
 * - [DetectAbandonedSessionUseCase] then finds sessions that are still active/paused
 *   (i.e., the ones not yet marked as crashed) for the resume/discard dialog.
 *
 * However, per the spec, crashed sessions SHOULD still show the resume/discard dialog.
 * So this use case marks them as CRASHED for status tracking, but returns the session
 * so the caller can show the dialog.
 *
 * Decision: We do NOT mark as crashed here. Instead, [DetectAbandonedSessionUseCase]
 * finds the session, and the dialog UI marks it as crashed if the user chooses to discard,
 * or resumes it if they choose to continue. The "crashed" status is set only if the user
 * discards after a crash, so we preserve the option to resume.
 *
 * Revised approach: On fresh launch (no SavedStateHandle context), if an active session
 * is found, we note it was interrupted. The UI shows the dialog. If the user discards,
 * we mark it as CRASHED (not DISCARDED, to distinguish crash-discard from user-discard).
 *
 * Final implementation: Simply detect and return. Status transition is caller's choice.
 */
class MarkCrashedSessionsUseCase @Inject constructor(
    private val workoutSessionRepository: WorkoutSessionRepository,
) {

    /**
     * Finds an active session that was interrupted by a crash.
     *
     * @param isFreshLaunch true if the app launched without a saved ViewModel state,
     *        indicating the process was killed or crashed.
     * @return the interrupted session, or null if none found.
     */
    suspend operator fun invoke(isFreshLaunch: Boolean): CrashDetectionResult {
        if (!isFreshLaunch) {
            return CrashDetectionResult.NoCrash
        }

        val activeSession = workoutSessionRepository.getActiveSession()
            ?: return CrashDetectionResult.NoCrash

        return CrashDetectionResult.CrashedSessionFound(
            sessionId = activeSession.id,
            startedAt = activeSession.startedAt,
        )
    }
}

/**
 * Result of crash detection on app startup.
 */
sealed interface CrashDetectionResult {
    /** No interrupted session found. */
    data object NoCrash : CrashDetectionResult

    /** An active session was found after a fresh app launch (crash/kill). */
    @Suppress("ForbiddenPublicDataClass")
    data class CrashedSessionFound(
        val sessionId: Long,
        val startedAt: Long,
    ) : CrashDetectionResult
}
