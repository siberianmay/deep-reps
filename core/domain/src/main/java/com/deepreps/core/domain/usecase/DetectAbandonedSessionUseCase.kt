package com.deepreps.core.domain.usecase

import com.deepreps.core.domain.model.WorkoutSession
import com.deepreps.core.domain.repository.WorkoutSessionRepository
import javax.inject.Inject

/**
 * Detects if there is an existing active or paused session that may need recovery.
 *
 * Called on app startup (after onboarding check) to determine if a
 * "Resume or Discard?" dialog should be shown to the user.
 *
 * Returns the session if one exists with status ACTIVE or PAUSED, null otherwise.
 */
class DetectAbandonedSessionUseCase @Inject constructor(
    private val workoutSessionRepository: WorkoutSessionRepository,
) {

    /**
     * Returns the active/paused session if one exists, null otherwise.
     * At most one active/paused session exists at any time (DAO enforces LIMIT 1).
     */
    suspend operator fun invoke(): WorkoutSession? {
        return workoutSessionRepository.getActiveSession()
    }
}
