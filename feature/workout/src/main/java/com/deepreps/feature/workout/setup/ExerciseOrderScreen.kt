package com.deepreps.feature.workout.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.deepreps.core.domain.model.enums.MuscleGroup
import com.deepreps.core.ui.component.DeepRepsButton
import com.deepreps.core.ui.component.DeepRepsCard
import com.deepreps.core.ui.component.DragDropState
import com.deepreps.core.ui.component.rememberDragDropState
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Exercise Order screen: shows the auto-ordered exercise list with drag-to-reorder.
 *
 * Design spec: design-system.md Section 4.6 (simplified -- pre-plan ordering).
 * - Drag handle on left side for reordering
 * - Exercise name, equipment, difficulty for each item
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

    val lazyListState = rememberLazyListState()
    val dragDropState = rememberDragDropState(
        lazyListState = lazyListState,
        onMove = onMoveExercise,
    )
    val hapticFeedback = LocalHapticFeedback.current

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
                "Exercises auto-ordered by type and difficulty. Drag to reorder."
            },
            style = typography.bodyMedium,
            color = colors.onSurfaceSecondary,
            modifier = Modifier.padding(horizontal = spacing.space4, vertical = spacing.space2),
        )

        // Exercise list with drag-to-reorder
        ExerciseOrderList(
            exercises = exercises,
            lazyListState = lazyListState,
            dragDropState = dragDropState,
            onHapticFeedback = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            },
            modifier = Modifier.weight(1f),
        )

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
 * LazyColumn containing draggable exercise cards.
 *
 * Separated to keep [ExerciseOrderScreen] focused on overall layout.
 * Items use graphicsLayer for drag visual feedback (translation, scale, opacity).
 */
@Suppress("LongMethod")
@Composable
private fun ExerciseOrderList(
    exercises: List<ExerciseOrderItem>,
    lazyListState: LazyListState,
    dragDropState: DragDropState,
    onHapticFeedback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = DeepRepsTheme.spacing

    LazyColumn(
        state = lazyListState,
        modifier = modifier,
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
            val isDragged = dragDropState.isDragging && dragDropState.draggedItemIndex == index
            val dragOffset = if (isDragged) dragDropState.dragOffset else 0f
            val currentIndex by rememberUpdatedState(index)

            Box(
                modifier = Modifier
                    .zIndex(if (isDragged) 1f else 0f)
                    .graphicsLayer {
                        translationY = dragOffset
                        scaleX = if (isDragged) 1.05f else 1f
                        scaleY = if (isDragged) 1.05f else 1f
                        alpha = if (isDragged) 0.9f else 1f
                        shadowElevation = if (isDragged) 8f else 0f
                    }
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = {
                                dragDropState.onDragStart(currentIndex)
                                onHapticFeedback()
                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                dragDropState.onDrag(dragAmount)
                            },
                            onDragEnd = {
                                onHapticFeedback()
                                dragDropState.onDragEnd()
                            },
                            onDragCancel = {
                                dragDropState.onDragCancel()
                            },
                        )
                    },
            ) {
                ExerciseOrderCardContent(
                    exercise = exercise,
                    index = index,
                    isDragged = isDragged,
                )
            }
        }
    }
}

/**
 * Visual content of a single exercise card in the ordering list.
 *
 * Layout: [DragHandle] [OrderNumber] [ExerciseInfo]
 */
@Suppress("LongMethod")
@Composable
private fun ExerciseOrderCardContent(
    exercise: ExerciseOrderItem,
    index: Int,
    isDragged: Boolean,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing

    val accessibilityText = "Drag to reorder ${exercise.name}, position ${index + 1}, " +
        "${exercise.equipment}, ${exercise.difficulty}"

    val muscleGroup = MuscleGroup.entries.getOrNull((exercise.primaryGroupId - 1).toInt())
    val groupColor = if (muscleGroup != null) {
        colors.colorForMuscleGroup(muscleGroup)
    } else {
        colors.onSurfaceTertiary
    }
    val radius = DeepRepsTheme.radius

    DeepRepsCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = accessibilityText },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Muscle group color indicator
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(
                        color = groupColor,
                        shape = RoundedCornerShape(
                            topStart = radius.md,
                            bottomStart = radius.md,
                        ),
                    ),
            )

            // Drag handle
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag to reorder ${exercise.name}",
                    modifier = Modifier.size(24.dp),
                    tint = if (isDragged) {
                        colors.accentPrimary
                    } else {
                        colors.onSurfaceTertiary
                    },
                )
            }

            // Order number
            Text(
                text = "${index + 1}",
                style = typography.headlineSmall,
                color = colors.accentPrimary,
                modifier = Modifier.width(32.dp),
            )

            Spacer(modifier = Modifier.width(spacing.space1))

            // Exercise info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = spacing.space3, end = spacing.space3, bottom = spacing.space3),
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
        }
    }
}
