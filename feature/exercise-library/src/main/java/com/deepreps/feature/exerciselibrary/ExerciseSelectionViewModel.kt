package com.deepreps.feature.exerciselibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepreps.core.domain.model.Exercise
import com.deepreps.core.domain.model.enums.MuscleGroup
import com.deepreps.core.domain.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * ViewModel for the multi-select exercise picker screen.
 *
 * Manages selected exercises across group tab switches. Selection state
 * persists when the user switches tabs -- toggling exercises in one group
 * does not clear selections in another.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExerciseSelectionViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ExerciseSelectionUiState())
    val state: StateFlow<ExerciseSelectionUiState> = _state.asStateFlow()

    private val _sideEffect = Channel<ExerciseSelectionSideEffect>(Channel.BUFFERED)
    val sideEffect: Flow<ExerciseSelectionSideEffect> = _sideEffect.receiveAsFlow()

    private val activeGroup = MutableStateFlow(MuscleGroup.CHEST)
    private val searchQuery = MutableStateFlow("")

    init {
        observeExercises()
    }

    fun onIntent(intent: ExerciseSelectionIntent) {
        when (intent) {
            is ExerciseSelectionIntent.ToggleExercise -> handleToggle(intent.exerciseId)
            is ExerciseSelectionIntent.SelectGroup -> handleSelectGroup(intent.group)
            is ExerciseSelectionIntent.Search -> handleSearch(intent.query)
            is ExerciseSelectionIntent.ClearSearch -> handleClearSearch()
            is ExerciseSelectionIntent.ConfirmSelection -> handleConfirm()
            is ExerciseSelectionIntent.ViewDetail -> handleViewDetail(intent.exerciseId)
            is ExerciseSelectionIntent.Retry -> handleRetry()
        }
    }

    private fun handleToggle(exerciseId: Long) {
        _state.update { current ->
            val updated = if (exerciseId in current.selectedExerciseIds) {
                current.selectedExerciseIds - exerciseId
            } else {
                current.selectedExerciseIds + exerciseId
            }
            current.copy(selectedExerciseIds = updated)
        }
    }

    private fun handleSelectGroup(group: MuscleGroup) {
        activeGroup.value = group
        _state.update { it.copy(activeGroup = group) }
    }

    private fun handleSearch(query: String) {
        searchQuery.value = query
        _state.update { it.copy(searchQuery = query) }
    }

    private fun handleClearSearch() {
        searchQuery.value = ""
        _state.update { it.copy(searchQuery = "") }
    }

    private fun handleConfirm() {
        val selectedIds = _state.value.selectedExerciseIds
        _sideEffect.trySend(ExerciseSelectionSideEffect.SelectionConfirmed(selectedIds))
    }

    private fun handleViewDetail(exerciseId: Long) {
        _sideEffect.trySend(ExerciseSelectionSideEffect.NavigateToDetail(exerciseId))
    }

    private fun handleRetry() {
        _state.update { it.copy(errorType = null, isLoading = true) }
        observeExercises()
    }

    private fun observeExercises() {
        combine(
            activeGroup.flatMapLatest { group ->
                exerciseRepository.getExercisesByGroup(
                    ExerciseListViewModel.groupIdFor(group),
                )
            },
            searchQuery,
        ) { exercises, query ->
            filterByQuery(exercises, query)
        }
            .onStart { _state.update { it.copy(isLoading = true) } }
            .onEach { filtered ->
                _state.update { current ->
                    current.copy(
                        exercises = filtered.map { it.toSelectionUi() },
                        isLoading = false,
                        errorType = null,
                    )
                }
            }
            .catch {
                _state.update { current ->
                    current.copy(
                        isLoading = false,
                        errorType = ExerciseSelectionError.LoadFailed,
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}

private fun filterByQuery(exercises: List<Exercise>, query: String): List<Exercise> {
    if (query.isBlank()) return exercises
    val lowerQuery = query.lowercase()
    return exercises.filter { it.name.lowercase().contains(lowerQuery) }
}

private fun Exercise.toSelectionUi(): ExerciseUi = ExerciseUi(
    id = id,
    name = name,
    equipment = equipment,
    movementType = movementType,
    difficulty = difficulty,
    primaryGroupId = primaryGroupId,
)
