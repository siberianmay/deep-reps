package com.deepreps.core.domain.usecase

import com.deepreps.core.domain.model.enums.SessionStatus
import com.deepreps.core.domain.repository.WorkoutSessionRepository
import javax.inject.Inject

/**
 * Marks sessions that have been active for longer than [STALE_THRESHOLD_HOURS] hours
 * as [SessionStatus.ABANDONED].
 *
 * A session older than 24 hours with status "active" is definitively stale --
 * no real workout lasts that long. This runs on app startup before the
 * abandoned session detection dialog, so that truly stale sessions do not
 * trigger the "Resume or Discard?" prompt.
 *
 * @see DetectAbandonedSessionUseCase which runs after this to find recoverable sessions
 */
class CleanupStaleSessionsUseCase @Inject constructor(
    private val workoutSessionRepository: WorkoutSessionRepository,
) {

    /**
     * Finds all sessions with status "active" older than [STALE_THRESHOLD_HOURS]
     * hours and marks them as [SessionStatus.ABANDONED].
     *
     * @param currentTimeMillis epoch millis representing "now". Injectable for testing.
     * @return the number of sessions marked as abandoned.
     */
    suspend operator fun invoke(
        currentTimeMillis: Long = System.currentTimeMillis(),
    ): Int {
        val cutoff = currentTimeMillis - STALE_THRESHOLD_MILLIS
        val staleSessions = workoutSessionRepository.getStaleActiveSessions(cutoff)

        staleSessions.forEach { session ->
            workoutSessionRepository.updateStatus(
                id = session.id,
                status = SessionStatus.ABANDONED.value,
                completedAt = null,
            )
        }

        return staleSessions.size
    }

    companion object {
        const val STALE_THRESHOLD_HOURS = 24L
        private const val STALE_THRESHOLD_MILLIS = STALE_THRESHOLD_HOURS * 60 * 60 * 1000
    }
}
