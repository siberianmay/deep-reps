package com.deepreps.feature.templates

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deepreps.core.ui.component.EmptyState
import com.deepreps.core.ui.component.ErrorState
import com.deepreps.core.ui.component.LoadingIndicator
import com.deepreps.core.ui.theme.DeepRepsTheme
import com.deepreps.feature.templates.components.TemplateCard

/**
 * Template manager screen: view, load, edit, and delete saved templates.
 *
 * Design spec: design-system.md Section 4.11.
 */
@Composable
fun TemplateListScreen(
    onNavigateToWorkoutSetup: (templateId: Long) -> Unit,
    onNavigateToCreateTemplate: () -> Unit,
    onNavigateToEditTemplate: (templateId: Long) -> Unit,
    viewModel: TemplateListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is TemplateListSideEffect.NavigateToWorkoutSetup -> {
                    onNavigateToWorkoutSetup(effect.templateId)
                }
                is TemplateListSideEffect.NavigateToCreateTemplate -> {
                    onNavigateToCreateTemplate()
                }
                is TemplateListSideEffect.NavigateToEditTemplate -> {
                    onNavigateToEditTemplate(effect.templateId)
                }
                is TemplateListSideEffect.ShowSnackbar -> {
                    // Snackbar host is managed at the scaffold level in the host activity/screen
                }
            }
        }
    }

    TemplateListContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TemplateListContent(
    state: TemplateListUiState,
    onIntent: (TemplateListIntent) -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val spacing = DeepRepsTheme.spacing

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = spacing.space4,
                end = spacing.space4,
                top = 64.dp, // Below top app bar
                bottom = spacing.space4,
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.space3),
        ) {
            // Content area based on state
            when {
                state.isLoading -> {
                    item {
                        LoadingIndicator(
                            message = "Loading templates...",
                            modifier = Modifier.fillParentMaxSize(),
                        )
                    }
                }

                state.errorType != null -> {
                    item {
                        ErrorState(
                            message = when (state.errorType) {
                                TemplateListError.LoadFailed -> "Failed to load templates."
                                TemplateListError.DeleteFailed -> "Failed to delete template."
                            },
                            onRetry = { onIntent(TemplateListIntent.Retry) },
                            modifier = Modifier.fillParentMaxSize(),
                        )
                    }
                }

                state.templates.isEmpty() -> {
                    item {
                        EmptyState(
                            title = "No templates yet",
                            message = "Save a workout as a template to reuse it",
                            modifier = Modifier.fillParentMaxSize(),
                            action = {
                                com.deepreps.core.ui.component.DeepRepsButton(
                                    text = "Create Template",
                                    onClick = { onIntent(TemplateListIntent.CreateTemplate) },
                                    variant = com.deepreps.core.ui.component.ButtonVariant.Secondary,
                                )
                            },
                        )
                    }
                }

                else -> {
                    items(
                        items = state.templates,
                        key = { it.id },
                    ) { template ->
                        SwipeToDeleteTemplateItem(
                            template = template,
                            onTap = {
                                onIntent(TemplateListIntent.LoadTemplate(template.id))
                            },
                            onSwipeDelete = {
                                onIntent(
                                    TemplateListIntent.RequestDelete(
                                        templateId = template.id,
                                        templateName = template.name,
                                    ),
                                )
                            },
                        )
                    }
                }
            }
        }

        // Top app bar overlaid
        TopAppBar(
            title = {
                Text(
                    text = "Templates",
                    style = DeepRepsTheme.typography.headlineMedium,
                )
            },
            actions = {
                IconButton(
                    onClick = { onIntent(TemplateListIntent.CreateTemplate) },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Create new template",
                        tint = colors.accentPrimary,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colors.surfaceLowest,
                titleContentColor = colors.onSurfacePrimary,
            ),
        )
    }

    // Delete confirmation dialog
    if (state.showDeleteConfirmation != null) {
        DeleteConfirmationDialog(
            templateName = state.showDeleteConfirmation.templateName,
            onConfirm = { onIntent(TemplateListIntent.ConfirmDelete) },
            onDismiss = { onIntent(TemplateListIntent.DismissDelete) },
        )
    }
}

/**
 * Template card with swipe-to-delete behavior.
 *
 * Design spec: design-system.md Section 5.2.
 * - Swipe right-to-left to reveal delete action
 * - Red background with trash icon
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteTemplateItem(
    template: TemplateUi,
    onTap: () -> Unit,
    onSwipeDelete: () -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onSwipeDelete()
                true
            } else {
                false
            }
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val backgroundColor by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    colors.statusError
                } else {
                    colors.surfaceLowest
                },
                animationSpec = tween(durationMillis = 200),
                label = "swipe_bg_color",
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete template",
                    tint = colors.onSurfacePrimary,
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
    ) {
        TemplateCard(
            template = template,
            onClick = onTap,
        )
    }
}

/**
 * Confirmation dialog for template deletion.
 *
 * Design spec: Section 1.1 - "Every destructive action requires confirmation."
 */
@Composable
private fun DeleteConfirmationDialog(
    templateName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Template",
                style = typography.headlineMedium,
                color = colors.onSurfacePrimary,
            )
        },
        text = {
            Text(
                text = "Delete \"$templateName\"? This cannot be undone.",
                style = typography.bodyLarge,
                color = colors.onSurfaceSecondary,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Delete",
                    style = typography.labelLarge,
                    color = colors.statusError,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    style = typography.labelLarge,
                    color = colors.onSurfaceSecondary,
                )
            }
        },
        containerColor = colors.surfaceHigh,
    )
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Template List - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun TemplateListDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        TemplateListContent(
            state = TemplateListUiState(
                templates = listOf(
                    TemplateUi(
                        id = 1,
                        name = "Push Day A",
                        muscleGroupNames = listOf("Chest", "Shoulders", "Arms"),
                        exerciseCount = 5,
                        exercisePreview = "Bench Press, OHP, Lateral Raise, ...",
                        lastUsedText = "Used 3 days ago",
                    ),
                    TemplateUi(
                        id = 2,
                        name = "Pull Day",
                        muscleGroupNames = listOf("Back", "Arms"),
                        exerciseCount = 4,
                        exercisePreview = "Deadlift, Barbell Row, Pull-up, Curl",
                        lastUsedText = "Used yesterday",
                    ),
                ),
                isLoading = false,
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "Empty State - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun EmptyStateDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        TemplateListContent(
            state = TemplateListUiState(
                templates = emptyList(),
                isLoading = false,
            ),
            onIntent = {},
        )
    }
}
