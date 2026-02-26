package com.deepreps.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepreps.core.domain.model.TemplateWithCount
import com.deepreps.core.domain.model.WorkoutSession
import com.deepreps.core.domain.model.enums.SessionStatus
import com.deepreps.core.domain.repository.TemplateRepository
import com.deepreps.core.domain.repository.WorkoutSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val templateRepository: TemplateRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        observeHomeData()
    }

    private fun observeHomeData() {
        combine(
            workoutSessionRepository.getCompletedSessions(),
            workoutSessionRepository.observeActiveSession(),
            templateRepository.getAllWithExerciseCount(),
        ) { completedSessions, activeSession, templates ->
            val lastWorkout = completedSessions.firstOrNull()?.let { session ->
                mapToLastWorkoutInfo(session)
            }

            val recentTemplates = templates.take(MAX_RECENT_TEMPLATES).map { template ->
                mapToTemplateInfo(template)
            }

            val isActive = activeSession != null &&
                (
                    activeSession.status == SessionStatus.ACTIVE ||
                    activeSession.status == SessionStatus.PAUSED
                )

            HomeUiState(
                lastWorkout = lastWorkout,
                recentTemplates = recentTemplates,
                hasActiveSession = isActive,
                activeSessionId = if (isActive) activeSession?.id else null,
                isLoading = false,
            )
        }
            .onEach { newState -> _state.update { newState } }
            .catch {
                _state.update { current -> current.copy(isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    private fun mapToLastWorkoutInfo(session: WorkoutSession): LastWorkoutInfo {
        val durationMinutes = ((session.durationSeconds ?: 0L) / SECONDS_PER_MINUTE).toInt()
        val dateText = formatSessionDate(session.completedAt ?: session.startedAt)

        return LastWorkoutInfo(
            sessionId = session.id,
            dateText = dateText,
            durationMinutes = durationMinutes,
        )
    }

    private fun mapToTemplateInfo(template: TemplateWithCount): TemplateInfo {
        return TemplateInfo(
            id = template.id,
            name = template.name,
            exerciseCount = template.exerciseCount,
        )
    }

    companion object {
        private const val MAX_RECENT_TEMPLATES = 5
        private const val SECONDS_PER_MINUTE = 60

        internal fun formatSessionDate(epochMillis: Long): String {
            val now = System.currentTimeMillis()
            val diffMs = now - epochMillis
            val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()

            return when {
                diffDays < 1 -> "Today"
                diffDays == 1 -> "Yesterday"
                diffDays < 7 -> "$diffDays days ago"
                diffDays < 30 -> "${diffDays / 7} weeks ago"
                else -> "${diffDays / 30} months ago"
            }
        }
    }
}

/**
 * UI state for the Home dashboard screen.
 */
@Suppress("ForbiddenPublicDataClass")
data class HomeUiState(
    val lastWorkout: LastWorkoutInfo? = null,
    val recentTemplates: List<TemplateInfo> = emptyList(),
    val hasActiveSession: Boolean = false,
    val activeSessionId: Long? = null,
    val isLoading: Boolean = true,
)

/**
 * Summary of the most recent completed workout.
 */
@Suppress("ForbiddenPublicDataClass")
data class LastWorkoutInfo(
    val sessionId: Long,
    val dateText: String,
    val durationMinutes: Int,
)

/**
 * Minimal template info for the home dashboard.
 */
@Suppress("ForbiddenPublicDataClass")
data class TemplateInfo(
    val id: Long,
    val name: String,
    val exerciseCount: Int,
)
