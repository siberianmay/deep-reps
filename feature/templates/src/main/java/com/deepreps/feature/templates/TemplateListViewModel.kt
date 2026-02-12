package com.deepreps.feature.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepreps.core.domain.model.Template
import com.deepreps.core.domain.model.enums.MuscleGroup
import com.deepreps.core.domain.repository.ExerciseRepository
import com.deepreps.core.domain.repository.TemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the template manager screen.
 *
 * Loads all templates with their exercise metadata, handles delete with
 * confirmation, and dispatches navigation to workout setup or template edit.
 */
@HiltViewModel
class TemplateListViewModel @Inject constructor(
    private val templateRepository: TemplateRepository,
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(TemplateListUiState())
    val state: StateFlow<TemplateListUiState> = _state.asStateFlow()

    private val _sideEffect = Channel<TemplateListSideEffect>(Channel.BUFFERED)
    val sideEffect: Flow<TemplateListSideEffect> = _sideEffect.receiveAsFlow()

    init {
        observeTemplates()
    }

    fun onIntent(intent: TemplateListIntent) {
        when (intent) {
            is TemplateListIntent.LoadTemplate -> handleLoadTemplate(intent.templateId)
            is TemplateListIntent.RequestDelete -> handleRequestDelete(
                intent.templateId,
                intent.templateName,
            )
            is TemplateListIntent.ConfirmDelete -> handleConfirmDelete()
            is TemplateListIntent.DismissDelete -> handleDismissDelete()
            is TemplateListIntent.CreateTemplate -> handleCreateTemplate()
            is TemplateListIntent.EditTemplate -> handleEditTemplate(intent.templateId)
            is TemplateListIntent.Retry -> handleRetry()
        }
    }

    private fun handleLoadTemplate(templateId: Long) {
        _sideEffect.trySend(TemplateListSideEffect.NavigateToWorkoutSetup(templateId))
    }

    private fun handleRequestDelete(templateId: Long, templateName: String) {
        _state.update {
            it.copy(
                showDeleteConfirmation = DeleteConfirmation(
                    templateId = templateId,
                    templateName = templateName,
                ),
            )
        }
    }

    private fun handleConfirmDelete() {
        val confirmation = _state.value.showDeleteConfirmation ?: return
        _state.update { it.copy(showDeleteConfirmation = null) }

        viewModelScope.launch {
            try {
                val template = templateRepository.getById(confirmation.templateId) ?: return@launch
                templateRepository.delete(template)
                _sideEffect.trySend(
                    TemplateListSideEffect.ShowSnackbar("\"${confirmation.templateName}\" deleted"),
                )
            } catch (_: Exception) {
                _state.update { it.copy(errorType = TemplateListError.DeleteFailed) }
            }
        }
    }

    private fun handleDismissDelete() {
        _state.update { it.copy(showDeleteConfirmation = null) }
    }

    private fun handleCreateTemplate() {
        _sideEffect.trySend(TemplateListSideEffect.NavigateToCreateTemplate)
    }

    private fun handleEditTemplate(templateId: Long) {
        _sideEffect.trySend(TemplateListSideEffect.NavigateToEditTemplate(templateId))
    }

    private fun handleRetry() {
        _state.update { it.copy(errorType = null, isLoading = true) }
        observeTemplates()
    }

    private fun observeTemplates() {
        templateRepository.getAll()
            .onStart { _state.update { it.copy(isLoading = true) } }
            .onEach { templates ->
                val templateUiList = templates.map { template ->
                    mapToUi(template)
                }
                _state.update { current ->
                    current.copy(
                        templates = templateUiList,
                        isLoading = false,
                        errorType = null,
                    )
                }
            }
            .catch {
                _state.update { current ->
                    current.copy(
                        isLoading = false,
                        errorType = TemplateListError.LoadFailed,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun mapToUi(template: Template): TemplateUi {
        val exercises = templateRepository.getExercisesForTemplate(template.id)
        val exerciseIds = exercises.map { it.exerciseId }
        val exerciseDetails = if (exerciseIds.isNotEmpty()) {
            exerciseRepository.getExercisesByIds(exerciseIds)
        } else {
            emptyList()
        }

        val exerciseNames = exerciseDetails.map { it.name }
        val preview = when {
            exerciseNames.isEmpty() -> "No exercises"
            exerciseNames.size <= 3 -> exerciseNames.joinToString(", ")
            else -> "${exerciseNames.take(3).joinToString(", ")}, ..."
        }

        val muscleGroupNames = template.muscleGroups.mapNotNull { groupId ->
            muscleGroupNameFromId(groupId)
        }

        return TemplateUi(
            id = template.id,
            name = template.name,
            muscleGroupNames = muscleGroupNames,
            exerciseCount = exercises.size,
            exercisePreview = preview,
            lastUsedText = formatLastUsed(template.updatedAt),
        )
    }

    companion object {

        /**
         * Maps a 1-indexed database ID to a display name.
         * IDs follow MuscleGroup enum ordinal + 1.
         */
        internal fun muscleGroupNameFromId(groupId: Long): String? {
            val index = (groupId - 1).toInt()
            return MuscleGroup.entries.getOrNull(index)?.let { group ->
                when (group) {
                    MuscleGroup.LEGS -> "Legs"
                    MuscleGroup.LOWER_BACK -> "Lower Back"
                    MuscleGroup.CHEST -> "Chest"
                    MuscleGroup.BACK -> "Back"
                    MuscleGroup.SHOULDERS -> "Shoulders"
                    MuscleGroup.ARMS -> "Arms"
                    MuscleGroup.CORE -> "Core"
                }
            }
        }

        /**
         * Formats an epoch millis timestamp into a relative "last used" string.
         */
        internal fun formatLastUsed(epochMillis: Long): String {
            val now = System.currentTimeMillis()
            val diffMs = now - epochMillis
            val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()

            return when {
                diffDays < 1 -> "Used today"
                diffDays == 1 -> "Used yesterday"
                diffDays < 7 -> "Used $diffDays days ago"
                diffDays < 30 -> "Used ${diffDays / 7} weeks ago"
                diffDays < 365 -> "Used ${diffDays / 30} months ago"
                else -> "Used ${diffDays / 365} years ago"
            }
        }
    }
}
