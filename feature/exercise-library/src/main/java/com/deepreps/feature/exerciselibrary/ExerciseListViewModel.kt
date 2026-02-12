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
 * ViewModel for the exercise list (browse by muscle group) screen.
 *
 * Exposes a single [state] flow and a [sideEffect] channel per MVI convention.
 * The group-to-database-ID mapping assumes muscle_groups table IDs match
 * [MuscleGroup] enum ordinals + 1 (1-indexed, insertion order in prepopulate).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExerciseListViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ExerciseListUiState())
    val state: StateFlow<ExerciseListUiState> = _state.asStateFlow()

    private val _sideEffect = Channel<ExerciseListSideEffect>(Channel.BUFFERED)
    val sideEffect: Flow<ExerciseListSideEffect> = _sideEffect.receiveAsFlow()

    private val selectedGroup = MutableStateFlow(MuscleGroup.CHEST)
    private val searchQuery = MutableStateFlow("")

    init {
        observeExercises()
    }

    fun onIntent(intent: ExerciseListIntent) {
        when (intent) {
            is ExerciseListIntent.SelectGroup -> handleSelectGroup(intent.group)
            is ExerciseListIntent.Search -> handleSearch(intent.query)
            is ExerciseListIntent.ClearSearch -> handleClearSearch()
            is ExerciseListIntent.NavigateToDetail -> handleNavigateToDetail(intent.exerciseId)
            is ExerciseListIntent.Retry -> handleRetry()
        }
    }

    private fun handleSelectGroup(group: MuscleGroup) {
        selectedGroup.value = group
        _state.update { it.copy(selectedGroup = group) }
    }

    private fun handleSearch(query: String) {
        searchQuery.value = query
        _state.update { it.copy(searchQuery = query) }
    }

    private fun handleClearSearch() {
        searchQuery.value = ""
        _state.update { it.copy(searchQuery = "") }
    }

    private fun handleNavigateToDetail(exerciseId: Long) {
        _sideEffect.trySend(ExerciseListSideEffect.NavigateToDetail(exerciseId))
    }

    private fun handleRetry() {
        _state.update { it.copy(errorType = null, isLoading = true) }
        observeExercises()
    }

    private fun observeExercises() {
        combine(
            selectedGroup.flatMapLatest { group ->
                exerciseRepository.getExercisesByGroup(groupIdFor(group))
            },
            searchQuery,
        ) { exercises, query ->
            filterExercises(exercises, query)
        }
            .onStart { _state.update { it.copy(isLoading = true) } }
            .onEach { filtered ->
                _state.update { current ->
                    current.copy(
                        exercises = filtered.map { it.toUi() },
                        isLoading = false,
                        errorType = null,
                    )
                }
            }
            .catch {
                _state.update { current ->
                    current.copy(
                        isLoading = false,
                        errorType = ExerciseListError.LoadFailed,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    companion object {
        /**
         * Maps [MuscleGroup] enum to its database row ID.
         *
         * The muscle_groups table is pre-populated in insertion order matching
         * the enum declaration order. IDs are 1-indexed (Room autoGenerate).
         */
        internal fun groupIdFor(group: MuscleGroup): Long = (group.ordinal + 1).toLong()
    }
}

private fun filterExercises(exercises: List<Exercise>, query: String): List<Exercise> {
    if (query.isBlank()) return exercises
    val lowerQuery = query.lowercase()
    return exercises.filter { it.name.lowercase().contains(lowerQuery) }
}

private fun Exercise.toUi(): ExerciseUi = ExerciseUi(
    id = id,
    name = name,
    equipment = equipment,
    movementType = movementType,
    difficulty = difficulty,
    primaryGroupId = primaryGroupId,
)
