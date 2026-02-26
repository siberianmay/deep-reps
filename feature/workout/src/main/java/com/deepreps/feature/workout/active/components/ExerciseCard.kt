package com.deepreps.feature.workout.active.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.deepreps.core.domain.model.WorkoutSet
import com.deepreps.core.ui.component.SetRow
import com.deepreps.core.ui.theme.DeepRepsTheme
import com.deepreps.feature.workout.active.WorkoutExerciseUi

/**
 * Collapsible exercise card per design-system.md Section 3.2.
 *
 * States:
 * - Active (current exercise): surface-medium background, expanded, accent-primary left border 3dp
 * - Upcoming: surface-low background, collapsed (header only)
 * - Completed: surface-low background, 70% opacity, collapsed with summary
 *
 * @param exercise The UI model for this exercise.
 * @param isCurrentExercise Whether this is the currently active exercise.
 * @param onToggleExpand Callback when the user taps the collapse/expand toggle.
 * @param onSetDone Callback when the user taps "done" on a SetRow.
 * @param onWeightFieldClick Callback to open weight editor.
 * @param onRepsFieldClick Callback to open reps editor.
 * @param onAddSet Callback to add a new set.
 * @param isNotesExpanded Whether the notes text field is currently visible.
 * @param onToggleNotes Callback when the user taps the notes icon to show/hide the text field.
 * @param onNotesChanged Callback when the user edits the notes text.
 * @param modifier External modifier.
 */
@Suppress("LongMethod", "CyclomaticComplexMethod")
@Composable
fun ExerciseCard(
    exercise: WorkoutExerciseUi,
    isCurrentExercise: Boolean,
    onToggleExpand: () -> Unit,
    onSetDone: (set: WorkoutSet) -> Unit,
    onWeightFieldClick: (set: WorkoutSet) -> Unit,
    onRepsFieldClick: (set: WorkoutSet) -> Unit,
    onAddSet: () -> Unit,
    onSkipSet: (set: WorkoutSet) -> Unit = {},
    onUnskipSet: (set: WorkoutSet) -> Unit = {},
    onDeleteSet: (set: WorkoutSet) -> Unit = {},
    isNotesExpanded: Boolean = false,
    onToggleNotes: () -> Unit = {},
    onNotesChanged: (String) -> Unit = {},
    onInfoClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val radius = DeepRepsTheme.radius
    val spacing = DeepRepsTheme.spacing

    val backgroundColor = when {
        isCurrentExercise -> colors.surfaceMedium
        else -> colors.surfaceLow
    }

    val contentAlpha by animateFloatAsState(
        targetValue = if (exercise.isCompleted && !isCurrentExercise) 0.7f else 1f,
        animationSpec = tween(200),
        label = "exerciseCardAlpha",
    )

    // Accent-primary left border color for active exercise
    val leftBorderColor = if (isCurrentExercise) colors.accentPrimary else backgroundColor

    val accessibilityLabel = buildString {
        append("Exercise: ${exercise.name}")
        if (exercise.isCompleted) append(", completed")
        else if (isCurrentExercise) append(", current exercise")
        append(", ${exercise.completionSummary}")
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(radius.md))
            .background(backgroundColor)
            .drawBehind {
                // Draw 3dp left border for active exercise
                if (isCurrentExercise) {
                    drawRect(
                        color = leftBorderColor,
                        topLeft = Offset.Zero,
                        size = androidx.compose.ui.geometry.Size(
                            width = 3.dp.toPx(),
                            height = size.height,
                        ),
                    )
                }
            }
            .alpha(contentAlpha)
            .semantics { contentDescription = accessibilityLabel },
    ) {
        Column(
            modifier = Modifier.padding(start = if (isCurrentExercise) 3.dp else 0.dp),
        ) {
            // --- Header (56dp) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable(onClick = onToggleExpand)
                    .padding(horizontal = spacing.space4),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Collapse/expand toggle
                Icon(
                    imageVector = if (exercise.isExpanded) {
                        Icons.Filled.ExpandLess
                    } else {
                        Icons.Filled.ExpandMore
                    },
                    contentDescription = if (exercise.isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.size(24.dp),
                    tint = colors.onSurfaceSecondary,
                )

                Spacer(modifier = Modifier.width(spacing.space2))

                // Exercise name
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = typography.headlineLarge,
                        color = colors.onSurfacePrimary,
                        maxLines = 1,
                    )
                    if (!exercise.isExpanded && exercise.isCompleted) {
                        Text(
                            text = exercise.completionSummary,
                            style = typography.bodySmall,
                            color = colors.statusSuccess,
                        )
                    } else if (!exercise.isExpanded) {
                        Text(
                            text = exercise.completionSummary,
                            style = typography.bodySmall,
                            color = colors.onSurfaceTertiary,
                        )
                    }
                }

                // Equipment tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(radius.sm))
                        .background(colors.surfaceHighest)
                        .padding(horizontal = spacing.space2, vertical = spacing.space1),
                ) {
                    Text(
                        text = exercise.equipment,
                        style = typography.labelMedium,
                        color = colors.onSurfaceSecondary,
                    )
                }

                // Info icon
                Spacer(modifier = Modifier.width(spacing.space1))
                IconButton(
                    onClick = onInfoClick,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Exercise information",
                        modifier = Modifier.size(20.dp),
                        tint = colors.onSurfaceTertiary,
                    )
                }

                // Notes toggle icon
                Spacer(modifier = Modifier.width(spacing.space1))
                IconButton(
                    onClick = onToggleNotes,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.StickyNote2,
                        contentDescription = if (isNotesExpanded) "Hide notes" else "Show notes",
                        modifier = Modifier.size(20.dp),
                        tint = if (exercise.notes.isNullOrEmpty()) {
                            colors.onSurfaceTertiary
                        } else {
                            colors.accentPrimary
                        },
                    )
                }
            }

            // --- Body (set rows) ---
            AnimatedVisibility(
                visible = exercise.isExpanded,
                enter = expandVertically(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)),
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = spacing.space4,
                        end = spacing.space4,
                        bottom = spacing.space3,
                    ),
                ) {
                    // Gap between header and set rows: 8dp
                    Spacer(modifier = Modifier.height(spacing.space2))

                    // Notes text field (expandable)
                    AnimatedVisibility(
                        visible = isNotesExpanded,
                        enter = expandVertically(animationSpec = tween(200)),
                        exit = shrinkVertically(animationSpec = tween(200)),
                    ) {
                        Column {
                            OutlinedTextField(
                                value = exercise.notes ?: "",
                                onValueChange = { text ->
                                    if (text.length <= NOTES_MAX_LENGTH) {
                                        onNotesChanged(text)
                                    }
                                },
                                placeholder = {
                                    Text(
                                        text = "Add notes for this exercise...",
                                        style = typography.bodySmall,
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp, max = 120.dp),
                                textStyle = typography.bodySmall.copy(
                                    color = colors.onSurfacePrimary,
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.accentPrimary,
                                    unfocusedBorderColor = colors.surfaceHighest,
                                    cursorColor = colors.accentPrimary,
                                    focusedPlaceholderColor = colors.onSurfaceTertiary,
                                    unfocusedPlaceholderColor = colors.onSurfaceTertiary,
                                ),
                                shape = RoundedCornerShape(radius.sm),
                                maxLines = 4,
                                supportingText = {
                                    val currentLength = exercise.notes?.length ?: 0
                                    Text(
                                        text = "$currentLength/$NOTES_MAX_LENGTH",
                                        style = typography.labelSmall,
                                        color = if (currentLength >= NOTES_MAX_LENGTH) {
                                            colors.statusError
                                        } else {
                                            colors.onSurfaceTertiary
                                        },
                                    )
                                },
                            )
                            Spacer(modifier = Modifier.height(spacing.space2))
                        }
                    }

                    // Set rows with 4dp gap between
                    exercise.sets.forEachIndexed { index, set ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        SetRow(
                            set = set,
                            onWeightFieldClick = { onWeightFieldClick(set) },
                            onRepsFieldClick = { onRepsFieldClick(set) },
                            onDoneClick = { onSetDone(set) },
                            onSkipSet = { onSkipSet(set) },
                            onUnskipSet = { onUnskipSet(set) },
                            onDeleteSet = { onDeleteSet(set) },
                        )
                    }

                    // --- Footer (48dp) ---
                    Spacer(modifier = Modifier.height(spacing.space2))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onAddSet) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = colors.accentPrimary,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add Set",
                                style = typography.labelLarge,
                                color = colors.accentPrimary,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Maximum character count for per-exercise notes. */
private const val NOTES_MAX_LENGTH = 1000
