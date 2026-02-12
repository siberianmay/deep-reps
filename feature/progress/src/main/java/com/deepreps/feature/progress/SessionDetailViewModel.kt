package com.deepreps.feature.progress

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepreps.core.domain.model.enums.SetStatus
import com.deepreps.core.domain.repository.ExerciseRepository
import com.deepreps.core.domain.repository.UserProfileRepository
import com.deepreps.core.domain.repository.WorkoutSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the read-only session detail screen.
 *
 * Loads a past workout session with all exercises and sets.
 * Navigation argument "sessionId" is required.
 */
@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val exerciseRepository: ExerciseRepository,
    private val userProfileRepository: UserProfileRepository,
) : ViewModel() {

    private val sessionId: Long = checkNotNull(savedStateHandle.get<Long>(SESSION_ID_ARG))

    private val _state = MutableStateFlow(SessionDetailUiState())
    val state: StateFlow<SessionDetailUiState> = _state.asStateFlow()

    init {
        loadWeightUnit()
        loadSession()
    }

    private fun loadWeightUnit() {
        viewModelScope.launch {
            try {
                val profile = userProfileRepository.get()
                if (profile != null) {
                    _state.update { it.copy(weightUnit = profile.preferredUnit) }
                }
            } catch (_: Exception) {
                // Keep default kg
            }
        }
    }

    @Suppress("LongMethod")
    private fun loadSession() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val session = workoutSessionRepository.getSession(sessionId)
                if (session == null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorType = SessionDetailError.NotFound,
                        )
                    }
                    return@launch
                }

                val workoutExercises = workoutSessionRepository
                    .getExercisesForSession(sessionId)
                    .first()

                val exerciseUiList = mutableListOf<SessionExerciseUi>()
                var totalVolume = 0.0
                var totalSets = 0

                for (workoutExercise in workoutExercises) {
                    val exerciseDetail = exerciseRepository
                        .getExerciseById(workoutExercise.exerciseId)

                    val sets = workoutSessionRepository
                        .getSetsForExercise(workoutExercise.id)
                        .first()

                    val setUiList = sets.map { set ->
                        val weight = set.actualWeightKg
                        val reps = set.actualReps
                        if (set.status == SetStatus.COMPLETED &&
                            weight != null &&
                            reps != null
                        ) {
                            totalVolume += weight * reps
                            totalSets++
                        }
                        SessionSetUi(
                            setNumber = set.setNumber,
                            type = set.type,
                            weightKg = set.actualWeightKg,
                            reps = set.actualReps,
                            isPersonalRecord = set.isPersonalRecord,
                        )
                    }

                    exerciseUiList.add(
                        SessionExerciseUi(
                            exerciseId = workoutExercise.exerciseId,
                            exerciseName = exerciseDetail?.name ?: "Unknown Exercise",
                            sets = setUiList,
                            notes = workoutExercise.notes,
                        ),
                    )
                }

                _state.update { current ->
                    current.copy(
                        dateText = ProgressDashboardViewModel.formatDate(session.startedAt),
                        durationText = ProgressDashboardViewModel.formatDuration(
                            session.durationSeconds,
                        ),
                        totalVolumeKg = totalVolume,
                        totalSets = totalSets,
                        exercises = exerciseUiList,
                        notes = session.notes,
                        isLoading = false,
                        errorType = null,
                    )
                }
            } catch (_: Exception) {
                _state.update { current ->
                    current.copy(
                        isLoading = false,
                        errorType = SessionDetailError.LoadFailed,
                    )
                }
            }
        }
    }

    companion object {
        const val SESSION_ID_ARG = "sessionId"
    }
}
