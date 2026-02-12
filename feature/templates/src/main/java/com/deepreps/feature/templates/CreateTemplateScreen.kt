package com.deepreps.feature.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deepreps.core.ui.component.DeepRepsTextField
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Template creation/edit screen.
 *
 * Design spec: design-system.md Section 4.14.
 * - Top app bar: [X] close, title, [Save] action
 * - Name text field with validation
 * - Auto-computed muscle group chips (non-interactive)
 * - Reorderable exercise list with drag handles and delete buttons
 */
@Composable
fun CreateTemplateScreen(
    onNavigateBack: () -> Unit,
    onTemplateSaved: (message: String) -> Unit,
    viewModel: CreateTemplateViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is CreateTemplateSideEffect.TemplateSaved -> {
                    onTemplateSaved(effect.message)
                    onNavigateBack()
                }
                is CreateTemplateSideEffect.ShowError -> {
                    // Error snackbar handled by scaffold
                }
                is CreateTemplateSideEffect.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    CreateTemplateContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun CreateTemplateContent(
    state: CreateTemplateUiState,
    onIntent: (CreateTemplateIntent) -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val spacing = DeepRepsTheme.spacing
    val typography = DeepRepsTheme.typography

    Column(modifier = Modifier.fillMaxSize()) {
        // Top app bar
        TopAppBar(
            title = {
                Text(
                    text = if (state.isEditing) "Edit Template" else "Create Template",
                    style = typography.headlineLarge,
                )
            },
            navigationIcon = {
                IconButton(onClick = { onIntent(CreateTemplateIntent.Close) }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                    )
                }
            },
            actions = {
                TextButton(
                    onClick = { onIntent(CreateTemplateIntent.Save) },
                    enabled = state.canSave,
                ) {
                    Text(
                        text = "Save",
                        style = typography.labelLarge,
                        color = if (state.canSave) {
                            colors.accentPrimary
                        } else {
                            colors.onSurfaceTertiary
                        },
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colors.surfaceLowest,
                titleContentColor = colors.onSurfacePrimary,
                navigationIconContentColor = colors.onSurfacePrimary,
            ),
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = spacing.space4,
                vertical = spacing.space4,
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.space4),
        ) {
            // Name input
            item {
                DeepRepsTextField(
                    value = state.name,
                    onValueChange = { onIntent(CreateTemplateIntent.UpdateName(it)) },
                    label = "Template Name",
                    placeholder = "e.g., Push Day A",
                    errorMessage = state.nameError,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Muscle group chips (auto-computed, non-interactive)
            if (state.muscleGroupNames.isNotEmpty()) {
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(spacing.space2),
                        verticalArrangement = Arrangement.spacedBy(spacing.space1),
                    ) {
                        state.muscleGroupNames.forEach { name ->
                            MuscleGroupDisplayChip(name = name)
                        }
                    }
                }
            }

            // Exercise error
            if (state.exerciseError != null) {
                item {
                    Text(
                        text = state.exerciseError,
                        style = typography.bodySmall,
                        color = colors.statusError,
                    )
                }
            }

            // Exercise list header
            if (state.exercises.isNotEmpty()) {
                item {
                    Text(
                        text = "Exercises (${state.exercises.size})",
                        style = typography.headlineSmall,
                        color = colors.onSurfacePrimary,
                    )
                }
            }

            // Exercise items
            itemsIndexed(
                items = state.exercises,
                key = { _, item -> item.exerciseId },
            ) { _, exercise ->
                TemplateExerciseRow(
                    exercise = exercise,
                    onRemove = {
                        onIntent(CreateTemplateIntent.RemoveExercise(exercise.exerciseId))
                    },
                )
            }

            // Empty exercises prompt
            if (state.exercises.isEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(spacing.space7))
                    Text(
                        text = "No exercises added yet",
                        style = typography.bodyMedium,
                        color = colors.onSurfaceTertiary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

/**
 * A single exercise row in the template editor.
 *
 * Design spec: Section 4.14 - 72dp height, drag handle left, name center, delete right.
 */
@Suppress("LongMethod")
@Composable
private fun TemplateExerciseRow(
    exercise: TemplateExerciseUi,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val radius = DeepRepsTheme.radius

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(radius.sm),
        color = colors.surfaceLow,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Drag handle
            Icon(
                imageVector = Icons.Filled.DragHandle,
                contentDescription = "Reorder",
                modifier = Modifier.size(24.dp),
                tint = colors.onSurfaceTertiary,
            )

            // Exercise name
            Text(
                text = exercise.name,
                style = typography.bodyLarge,
                color = colors.onSurfacePrimary,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            )

            // Delete button
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove ${exercise.name}",
                    modifier = Modifier.size(24.dp),
                    tint = colors.onSurfaceTertiary,
                )
            }
        }
    }
}

/**
 * Non-interactive chip displaying a muscle group name.
 *
 * Design spec: Section 4.14 - "Auto-computed from selected exercises, non-interactive display."
 */
@Composable
private fun MuscleGroupDisplayChip(
    name: String,
    modifier: Modifier = Modifier,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val radius = DeepRepsTheme.radius

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(radius.xl),
        color = colors.accentPrimaryContainer,
    ) {
        Text(
            text = name,
            style = typography.labelMedium,
            color = colors.accentPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Create Template - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun CreateTemplateDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        CreateTemplateContent(
            state = CreateTemplateUiState(
                name = "Push Day A",
                exercises = listOf(
                    TemplateExerciseUi(exerciseId = 1, name = "Bench Press", orderIndex = 0),
                    TemplateExerciseUi(exerciseId = 2, name = "Overhead Press", orderIndex = 1),
                    TemplateExerciseUi(exerciseId = 3, name = "Lateral Raise", orderIndex = 2),
                ),
                muscleGroupNames = listOf("Chest", "Shoulders"),
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "Create Template Empty - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun CreateTemplateEmptyDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        CreateTemplateContent(
            state = CreateTemplateUiState(),
            onIntent = {},
        )
    }
}

@Preview(
    name = "Create Template Validation Error - Dark",
    showBackground = true,
    backgroundColor = 0xFF0A0A0F,
)
@Composable
private fun CreateTemplateValidationErrorDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        CreateTemplateContent(
            state = CreateTemplateUiState(
                nameError = "Template name required",
                exerciseError = "Add at least one exercise",
            ),
            onIntent = {},
        )
    }
}
