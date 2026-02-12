package com.deepreps.feature.workout.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepreps.core.domain.usecase.CalculatePersonalRecordsUseCase
import com.deepreps.core.domain.usecase.GetWorkoutSummaryUseCase
import com.deepreps.core.domain.repository.UserProfileRepository
import com.deepreps.core.domain.repository.WorkoutSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the post-workout summary bottom sheet.
 *
 * Loads summary data and detects personal records for the completed session.
 * Navigation argument "sessionId" is required.
 */
@HiltViewModel
class WorkoutSummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getWorkoutSummaryUseCase: GetWorkoutSummaryUseCase,
    private val calculatePersonalRecordsUseCase: CalculatePersonalRecordsUseCase,
    private val userProfileRepository: UserProfileRepository,
    private val workoutSessionRepository: WorkoutSessionRepository,
) : ViewModel() {

    private val sessionId: Long = checkNotNull(savedStateHandle.get<Long>(SESSION_ID_ARG))

    private val _state = MutableStateFlow(WorkoutSummaryUiState())
    val state: StateFlow<WorkoutSummaryUiState> = _state.asStateFlow()

    private val _sideEffect = Channel<WorkoutSummarySideEffect>(Channel.BUFFERED)
    val sideEffect: Flow<WorkoutSummarySideEffect> = _sideEffect.receiveAsFlow()

    /** Cached exercise IDs for "Save as Template" flow. */
    private var exerciseIds: List<Long> = emptyList()

    init {
        loadWeightUnit()
        loadSummary()
    }

    fun onIntent(intent: WorkoutSummaryIntent) {
        when (intent) {
            is WorkoutSummaryIntent.Dismiss -> handleDismiss()
            is WorkoutSummaryIntent.SaveAsTemplate -> handleSaveAsTemplate()
            is WorkoutSummaryIntent.Retry -> handleRetry()
        }
    }

    private fun handleDismiss() {
        _sideEffect.trySend(WorkoutSummarySideEffect.NavigateToHome)
    }

    private fun handleSaveAsTemplate() {
        if (exerciseIds.isNotEmpty()) {
            _sideEffect.trySend(WorkoutSummarySideEffect.NavigateToCreateTemplate(exerciseIds))
        }
    }

    private fun handleRetry() {
        _state.update { it.copy(errorMessage = null, isLoading = true) }
        loadSummary()
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

    private fun loadSummary() {
        viewModelScope.launch {
            try {
                val summary = getWorkoutSummaryUseCase(sessionId)
                if (summary == null) {
                    _state.update {
                        it.copy(isLoading = false, errorMessage = "Session not found")
                    }
                    return@launch
                }

                // Detect and persist PRs
                val detectedPrs = calculatePersonalRecordsUseCase(sessionId)

                // Cache exercise IDs for template creation
                val exercises = workoutSessionRepository
                    .getExercisesForSession(sessionId)
                    .first()
                exerciseIds = exercises.map { it.exerciseId }

                val prUiList = detectedPrs.map { pr ->
                    PersonalRecordUi(
                        exerciseName = pr.exerciseName,
                        weightKg = pr.weightKg,
                        reps = pr.reps,
                        recordType = pr.recordType,
                    )
                }

                val groupVolumeUi = summary.perGroupVolume.map { gv ->
                    GroupVolumeUi(
                        groupName = gv.groupName,
                        workingSets = gv.workingSets,
                        tonnageKg = gv.tonnageKg,
                    )
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        durationText = formatDuration(summary.durationSeconds),
                        exerciseCount = summary.exerciseCount,
                        totalWorkingSets = summary.totalWorkingSets,
                        totalTonnageKg = summary.totalTonnageKg,
                        perGroupVolume = groupVolumeUi,
                        personalRecords = prUiList,
                        errorMessage = null,
                    )
                }
            } catch (_: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load workout summary",
                    )
                }
            }
        }
    }

    companion object {
        const val SESSION_ID_ARG = "sessionId"

        internal fun formatDuration(seconds: Long): String {
            if (seconds <= 0) return "--"
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            return if (hours > 0) {
                "${hours}h ${minutes}m"
            } else {
                "${minutes}m"
            }
        }
    }
}
