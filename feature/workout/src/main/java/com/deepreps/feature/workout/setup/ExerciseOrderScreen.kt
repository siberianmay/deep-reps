package com.deepreps.feature.workout.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.deepreps.core.ui.component.DeepRepsButton
import com.deepreps.core.ui.component.DeepRepsCard
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Exercise Order screen: shows the auto-ordered exercise list with reorder controls.
 *
 * Design spec: design-system.md Section 4.6 (simplified -- pre-plan ordering).
 * Uses simple move up/down buttons instead of drag-to-reorder for initial implementation.
 * - Exercise name, equipment, difficulty for each item
 * - Up/Down arrow buttons for manual reordering
 * - "Generate Plan" CTA at bottom
 */
@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseOrderScreen(
    exercises: List<ExerciseOrderItem>,
    isFromTemplate: Boolean,
    templateName: String?,
    onMoveExercise: (fromIndex: Int, toIndex: Int) -> Unit,
    onGeneratePlan: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = if (isFromTemplate && templateName != null) {
                        "Review: $templateName"
                    } else {
                        "Exercise Order"
                    },
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

        // Subtitle
        Text(
            text = if (isFromTemplate) {
                "Reorder exercises if needed, then generate your plan."
            } else {
                "Exercises auto-ordered by type and difficulty. Reorder if needed."
            },
            style = typography.bodyMedium,
            color = colors.onSurfaceSecondary,
            modifier = Modifier.padding(horizontal = spacing.space4, vertical = spacing.space2),
        )

        // Exercise list with reorder controls
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.space2),
            contentPadding = PaddingValues(
                horizontal = spacing.space4,
                vertical = spacing.space2,
            ),
        ) {
            itemsIndexed(
                items = exercises,
                key = { _, item -> item.exerciseId },
            ) { index, exercise ->
                ExerciseOrderCard(
                    exercise = exercise,
                    index = index,
                    isFirst = index == 0,
                    isLast = index == exercises.lastIndex,
                    onMoveUp = { onMoveExercise(index, index - 1) },
                    onMoveDown = { onMoveExercise(index, index + 1) },
                )
            }
        }

        // Bottom action area
        HorizontalDivider(color = colors.borderSubtle, thickness = 1.dp)

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
                text = "${exercises.size} exercise${if (exercises.size != 1) "s" else ""}",
                style = typography.bodyLarge,
                color = colors.onSurfacePrimary,
            )

            DeepRepsButton(
                text = "Generate Plan",
                onClick = onGeneratePlan,
                enabled = exercises.isNotEmpty(),
            )
        }
    }
}

/**
 * Individual exercise card in the ordering list.
 *
 * Shows exercise name, equipment, difficulty, and move up/down buttons.
 */
@Suppress("LongMethod")
@Composable
private fun ExerciseOrderCard(
    exercise: ExerciseOrderItem,
    index: Int,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing

    val accessibilityText = "${exercise.name}, position ${index + 1}, " +
        "${exercise.equipment}, ${exercise.difficulty}"

    DeepRepsCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = accessibilityText },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.space3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Order number
            Text(
                text = "${index + 1}",
                style = typography.headlineSmall,
                color = colors.accentPrimary,
                modifier = Modifier.width(32.dp),
            )

            // Exercise info
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = exercise.name,
                    style = typography.bodyLarge,
                    color = colors.onSurfacePrimary,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.space2),
                ) {
                    Text(
                        text = exercise.equipment,
                        style = typography.labelMedium,
                        color = colors.onSurfaceSecondary,
                    )
                    Text(
                        text = exercise.difficulty,
                        style = typography.labelMedium,
                        color = colors.onSurfaceTertiary,
                    )
                }
            }

            // Reorder buttons
            Column {
                IconButton(
                    onClick = onMoveUp,
                    enabled = !isFirst,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Move up",
                        tint = if (!isFirst) {
                            colors.onSurfacePrimary
                        } else {
                            colors.onSurfaceTertiary
                        },
                    )
                }

                IconButton(
                    onClick = onMoveDown,
                    enabled = !isLast,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Move down",
                        tint = if (!isLast) {
                            colors.onSurfacePrimary
                        } else {
                            colors.onSurfaceTertiary
                        },
                    )
                }
            }
        }
    }
}
