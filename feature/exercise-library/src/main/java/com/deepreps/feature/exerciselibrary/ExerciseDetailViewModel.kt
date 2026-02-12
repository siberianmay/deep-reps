package com.deepreps.feature.exerciselibrary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepreps.core.domain.model.Exercise
import com.deepreps.core.domain.repository.ExerciseRepository
import com.deepreps.feature.exerciselibrary.navigation.ExerciseLibraryNavigation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the exercise detail screen.
 *
 * Receives the exercise ID from [SavedStateHandle] via the navigation argument.
 * Loads exercise detail from the repository on init.
 */
@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val exerciseId: Long = requireNotNull(
        savedStateHandle.get<Long>(ExerciseLibraryNavigation.EXERCISE_ID_ARG),
        lazyMessage = { "exerciseId argument missing from SavedStateHandle" },
    )

    private val _state = MutableStateFlow(ExerciseDetailUiState())
    val state: StateFlow<ExerciseDetailUiState> = _state.asStateFlow()

    private val _sideEffect = Channel<ExerciseDetailSideEffect>(Channel.BUFFERED)
    val sideEffect: Flow<ExerciseDetailSideEffect> = _sideEffect.receiveAsFlow()

    init {
        loadExercise()
    }

    fun onIntent(intent: ExerciseDetailIntent) {
        when (intent) {
            is ExerciseDetailIntent.Retry -> handleRetry()
        }
    }

    private fun handleRetry() {
        _state.update { it.copy(isLoading = true, errorType = null) }
        loadExercise()
    }

    private fun loadExercise() {
        viewModelScope.launch {
            try {
                val exercise = exerciseRepository.getExerciseById(exerciseId)
                if (exercise != null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            exercise = exercise.toDetailUi(),
                            errorType = null,
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorType = ExerciseDetailError.NotFound,
                        )
                    }
                }
            } catch (_: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorType = ExerciseDetailError.LoadFailed,
                    )
                }
            }
        }
    }
}

private fun Exercise.toDetailUi(): ExerciseDetailUi = ExerciseDetailUi(
    id = id,
    name = name,
    description = description,
    equipment = equipment,
    movementType = movementType,
    difficulty = difficulty,
    primaryGroupId = primaryGroupId,
    secondaryMuscles = secondaryMuscles,
    tips = tips,
    pros = pros,
)
