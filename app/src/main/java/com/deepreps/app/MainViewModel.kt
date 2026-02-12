package com.deepreps.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepreps.core.domain.model.enums.SessionStatus
import com.deepreps.core.domain.provider.AnalyticsTracker
import com.deepreps.core.domain.repository.WorkoutSessionRepository
import com.deepreps.core.domain.usecase.SessionRecoveryResult
import com.deepreps.core.domain.usecase.SessionRecoveryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for MainActivity handling app-level concerns:
 * - Session recovery on cold startup (crash detection, abandoned session dialog)
 * - app_crash_detected analytics event when a crashed session is found
 *
 * This ViewModel runs the recovery sequence once on initialization.
 * The UI observes [state] to determine whether to show the recovery dialog.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionRecoveryUseCase: SessionRecoveryUseCase,
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    init {
        checkForRecoverableSession()
    }

    private fun checkForRecoverableSession() {
        viewModelScope.launch {
            val result = sessionRecoveryUseCase()

            when (result) {
                is SessionRecoveryResult.NoRecoveryNeeded -> {
                    _state.update { it.copy(recoveryCheckComplete = true) }
                }

                is SessionRecoveryResult.RecoverableSessionFound -> {
                    // Track app_crash_detected for sessions that were active
                    // (i.e., the app was killed/crashed mid-workout)
                    if (result.session.status == SessionStatus.ACTIVE) {
                        analyticsTracker.trackLifecycleEvent(
                            EVENT_APP_CRASH_DETECTED,
                            mapOf(
                                "had_active_workout" to true,
                            ),
                        )
                    }

                    _state.update {
                        it.copy(
                            recoveryCheckComplete = true,
                            recoverableSession = RecoverableSessionInfo(
                                sessionId = result.session.id,
                                startedAt = result.session.startedAt,
                                status = result.session.status,
                            ),
                        )
                    }
                }
            }
        }
    }

    fun onResumeSession() {
        val sessionId = _state.value.recoverableSession?.sessionId ?: return
        _state.update {
            it.copy(
                recoverableSession = null,
                navigateToWorkout = sessionId,
            )
        }
    }

    fun onDiscardSession() {
        val session = _state.value.recoverableSession ?: return
        viewModelScope.launch {
            // Mark as CRASHED if it was active (interrupted by crash/kill),
            // or DISCARDED if the user explicitly discards a paused session.
            val newStatus = if (session.status == SessionStatus.ACTIVE) {
                SessionStatus.CRASHED
            } else {
                SessionStatus.DISCARDED
            }

            workoutSessionRepository.updateStatus(
                id = session.sessionId,
                status = newStatus.value,
                completedAt = null,
            )

            // Track workout_abandoned when user discards
            analyticsTracker.trackUserAction(
                EVENT_WORKOUT_ABANDONED,
                mapOf(
                    "session_status" to session.status.value,
                    "discarded_by_user" to true,
                ),
            )

            _state.update {
                it.copy(recoverableSession = null)
            }
        }
    }

    fun onNavigationConsumed() {
        _state.update { it.copy(navigateToWorkout = null) }
    }

    companion object {
        private const val EVENT_APP_CRASH_DETECTED = "app_crash_detected"
        private const val EVENT_WORKOUT_ABANDONED = "workout_abandoned"
    }
}

/**
 * Main activity UI state. Minimal -- only handles recovery dialog.
 */
data class MainUiState(
    /** True once the recovery check has completed (show splash until then). */
    val recoveryCheckComplete: Boolean = false,
    /** Non-null if a recoverable session was found. */
    val recoverableSession: RecoverableSessionInfo? = null,
    /** Non-null when the user chose to resume. Consumed by navigation. */
    val navigateToWorkout: Long? = null,
)

/**
 * Minimal info about a recoverable session for the dialog.
 */
data class RecoverableSessionInfo(
    val sessionId: Long,
    val startedAt: Long,
    val status: SessionStatus,
)
