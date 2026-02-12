package com.deepreps.feature.exerciselibrary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deepreps.core.domain.model.enums.MuscleGroup
import com.deepreps.core.ui.component.DeepRepsButton
import com.deepreps.core.ui.component.DeepRepsTextField
import com.deepreps.core.ui.component.EmptyState
import com.deepreps.core.ui.component.ErrorState
import com.deepreps.core.ui.component.LoadingIndicator
import com.deepreps.core.ui.component.MuscleGroupChip
import com.deepreps.core.ui.theme.DeepRepsTheme
import com.deepreps.feature.exerciselibrary.components.ExerciseListItem

/**
 * Multi-select exercise picker screen.
 *
 * Design spec: design-system.md Section 4.4.
 * - Top app bar with back arrow, title, and selected count badge
 * - Muscle group tabs/chips for filtering
 * - Exercise list with checkboxes
 * - Search functionality
 * - Bottom action area: selected count + confirm button
 */
@Composable
fun ExerciseSelectionScreen(
    onNavigateBack: () -> Unit,
    onSelectionConfirmed: (Set<Long>) -> Unit,
    onNavigateToDetail: (exerciseId: Long) -> Unit,
    viewModel: ExerciseSelectionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is ExerciseSelectionSideEffect.SelectionConfirmed -> {
                    onSelectionConfirmed(effect.exerciseIds)
                }
                is ExerciseSelectionSideEffect.NavigateToDetail -> {
                    onNavigateToDetail(effect.exerciseId)
                }
            }
        }
    }

    ExerciseSelectionContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun ExerciseSelectionContent(
    state: ExerciseSelectionUiState,
    onIntent: (ExerciseSelectionIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val spacing = DeepRepsTheme.spacing
    val typography = DeepRepsTheme.typography

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "Select Exercises",
                    style = typography.headlineMedium,
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back",
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colors.surfaceLowest,
                titleContentColor = colors.onSurfacePrimary,
                navigationIconContentColor = colors.onSurfacePrimary,
            ),
        )

        // Search bar
        DeepRepsTextField(
            value = state.searchQuery,
            onValueChange = { onIntent(ExerciseSelectionIntent.Search(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.space4, vertical = spacing.space2),
            placeholder = "Search exercises...",
            singleLine = true,
        )

        // Muscle group chips
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.space4, vertical = spacing.space2),
            horizontalArrangement = Arrangement.spacedBy(spacing.space2),
            verticalArrangement = Arrangement.spacedBy(spacing.space2),
        ) {
            MuscleGroup.entries.forEach { group ->
                MuscleGroupChip(
                    muscleGroup = group,
                    isSelected = group == state.activeGroup,
                    onClick = { onIntent(ExerciseSelectionIntent.SelectGroup(group)) },
                )
            }
        }

        HorizontalDivider(color = colors.borderSubtle, thickness = 1.dp)

        // Content area
        Box(modifier = Modifier.weight(1f)) {
            when {
                state.isLoading -> {
                    LoadingIndicator(message = "Loading exercises...")
                }

                state.errorType != null -> {
                    ErrorState(
                        message = when (state.errorType) {
                            ExerciseSelectionError.LoadFailed -> "Failed to load exercises."
                        },
                        onRetry = { onIntent(ExerciseSelectionIntent.Retry) },
                    )
                }

                state.exercises.isEmpty() -> {
                    EmptyState(
                        title = "No exercises found",
                        message = if (state.searchQuery.isNotBlank()) {
                            "No exercises match \"${state.searchQuery}\""
                        } else {
                            "No exercises available for this muscle group"
                        },
                        icon = Icons.Filled.Search,
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = spacing.space2),
                    ) {
                        items(
                            items = state.exercises,
                            key = { it.id },
                        ) { exercise ->
                            ExerciseListItem(
                                exercise = exercise,
                                onClick = {
                                    onIntent(
                                        ExerciseSelectionIntent.ViewDetail(exercise.id),
                                    )
                                },
                                isCheckable = true,
                                isChecked = exercise.id in state.selectedExerciseIds,
                                onCheckedChange = {
                                    onIntent(
                                        ExerciseSelectionIntent.ToggleExercise(exercise.id),
                                    )
                                },
                            )
                            HorizontalDivider(
                                color = colors.borderSubtle,
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = spacing.space4),
                            )
                        }
                    }
                }
            }
        }

        // Bottom action area per design-system.md Section 4.4
        HorizontalDivider(color = colors.borderSubtle, thickness = 1.dp)
        BottomActionBar(
            selectedCount = state.selectedCount,
            onConfirm = { onIntent(ExerciseSelectionIntent.ConfirmSelection) },
        )
    }
}

/**
 * Bottom action bar with selected count and confirm button.
 *
 * Design spec: 80dp tall, surface-low background with top border.
 * Button disabled when 0 exercises selected.
 */
@Composable
private fun BottomActionBar(
    selectedCount: Int,
    onConfirm: () -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(colors.surfaceLow)
            .padding(horizontal = spacing.space4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "$selectedCount exercise${if (selectedCount != 1) "s" else ""} selected",
            style = typography.bodyLarge,
            color = colors.onSurfacePrimary,
        )

        DeepRepsButton(
            text = "Confirm",
            onClick = onConfirm,
            enabled = selectedCount > 0,
        )
    }
}
