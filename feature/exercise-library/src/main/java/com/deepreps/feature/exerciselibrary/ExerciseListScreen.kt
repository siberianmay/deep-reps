package com.deepreps.feature.exerciselibrary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deepreps.core.domain.model.enums.MuscleGroup
import com.deepreps.core.ui.component.DeepRepsTextField
import com.deepreps.core.ui.component.EmptyState
import com.deepreps.core.ui.component.ErrorState
import com.deepreps.core.ui.component.LoadingIndicator
import com.deepreps.core.ui.component.MuscleGroupChip
import com.deepreps.core.ui.theme.DeepRepsTheme
import com.deepreps.feature.exerciselibrary.components.ExerciseListItem

/**
 * Exercise list screen: browse exercises filtered by muscle group with search.
 *
 * Design spec: design-system.md Section 4.3/4.4.
 * - Top app bar with back navigation and title
 * - Muscle group chips for filtering
 * - Search bar
 * - LazyColumn of exercise rows
 */
@Composable
fun ExerciseListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (exerciseId: Long) -> Unit,
    viewModel: ExerciseListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is ExerciseListSideEffect.NavigateToDetail -> {
                    onNavigateToDetail(effect.exerciseId)
                }
            }
        }
    }

    ExerciseListContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}

@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun ExerciseListContent(
    state: ExerciseListUiState,
    onIntent: (ExerciseListIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val spacing = DeepRepsTheme.spacing

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Exercise Library",
                    style = DeepRepsTheme.typography.headlineMedium,
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
            onValueChange = { onIntent(ExerciseListIntent.Search(it)) },
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
                    isSelected = group == state.selectedGroup,
                    onClick = { onIntent(ExerciseListIntent.SelectGroup(group)) },
                )
            }
        }

        HorizontalDivider(color = colors.borderSubtle, thickness = 1.dp)

        // Content area
        when {
            state.isLoading -> {
                LoadingIndicator(message = "Loading exercises...")
            }

            state.errorType != null -> {
                ErrorState(
                    message = when (state.errorType) {
                        ExerciseListError.LoadFailed -> "Failed to load exercises."
                    },
                    onRetry = { onIntent(ExerciseListIntent.Retry) },
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
                                onIntent(ExerciseListIntent.NavigateToDetail(exercise.id))
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
}
