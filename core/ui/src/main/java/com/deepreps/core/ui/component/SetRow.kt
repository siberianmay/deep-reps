package com.deepreps.core.ui.component

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deepreps.core.domain.model.WorkoutSet
import com.deepreps.core.domain.model.enums.SetStatus
import com.deepreps.core.domain.model.enums.SetType
import com.deepreps.core.ui.theme.DeepRepsTheme
import com.deepreps.core.ui.theme.PrGold
import java.util.Locale

/**
 * The atomic unit of workout logging.
 *
 * Design spec: design-system.md Section 3.1.
 * - Total height: 64dp
 * - Set number column: 32dp wide
 * - Type indicator: 8dp wide color bar on left edge
 * - Weight field: 80dp wide, 48dp tall, right-aligned text
 * - "x" separator: 16dp wide
 * - Reps field: 64dp wide, 48dp tall, right-aligned text
 * - Done checkbox: 56dp x 56dp touch target
 * - Horizontal padding between elements: 8dp
 *
 * Tapping weight/reps fields in interactive states triggers
 * [onWeightFieldClick]/[onRepsFieldClick] for the parent to open a stepper or numpad.
 *
 * @param set The workout set data.
 * @param onWeightFieldClick Opens the weight editor (stepper/numpad).
 * @param onRepsFieldClick Opens the reps editor (stepper/numpad).
 * @param onDoneClick Marks the set complete / toggles completion.
 * @param modifier External modifier.
 * @param onSkipSet Callback when user selects "Skip this set" from context menu. Null hides the option.
 * @param onUnskipSet Callback when user selects "Unskip set" from context menu. Null hides the option.
 * @param onDeleteSet Callback when user selects "Delete set" from context menu. Null hides the option.
 */
@OptIn(ExperimentalFoundationApi::class)
@Suppress("LongMethod", "CyclomaticComplexMethod")
@Composable
fun SetRow(
    set: WorkoutSet,
    onWeightFieldClick: () -> Unit,
    onRepsFieldClick: () -> Unit,
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSkipSet: (() -> Unit)? = null,
    onUnskipSet: (() -> Unit)? = null,
    onDeleteSet: (() -> Unit)? = null,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val radius = DeepRepsTheme.radius
    val view = LocalView.current
    var showContextMenu by remember { mutableStateOf(false) }

    // State-driven colors
    val backgroundColor by animateColorAsState(
        targetValue = when (set.status) {
            SetStatus.PLANNED -> colors.surfaceHigh
            SetStatus.IN_PROGRESS -> colors.accentPrimaryContainer
            SetStatus.COMPLETED -> if (set.isPersonalRecord) {
                colors.statusSuccessContainer
            } else {
                colors.surfaceLow
            }
            SetStatus.SKIPPED -> colors.surfaceLow
        },
        animationSpec = tween(durationMillis = 200),
        label = "setRowBackground",
    )

    val textColor = when (set.status) {
        SetStatus.PLANNED -> colors.onSurfaceSecondary
        SetStatus.IN_PROGRESS -> colors.onSurfacePrimary
        SetStatus.COMPLETED -> colors.onSurfacePrimary
        SetStatus.SKIPPED -> colors.onSurfaceTertiary
    }

    val textAlpha = when (set.status) {
        SetStatus.COMPLETED -> 0.85f
        else -> 1f
    }

    val textDecoration = if (set.status == SetStatus.SKIPPED) TextDecoration.LineThrough else null

    val typeIndicatorColor = when (set.type) {
        SetType.WARMUP -> colors.warmUpSet
        SetType.WORKING -> colors.workingSet
    }

    val isInteractive = set.status == SetStatus.PLANNED || set.status == SetStatus.IN_PROGRESS

    // Display values
    val displayWeight = set.actualWeightKg ?: set.plannedWeightKg ?: 0.0
    val displayReps = set.actualReps ?: set.plannedReps ?: 0

    val accessibilityText = remember(set) {
        val typeLabel = if (set.type == SetType.WARMUP) "warm-up" else "working"
        val statusLabel = when (set.status) {
            SetStatus.PLANNED -> "not completed"
            SetStatus.IN_PROGRESS -> "in progress"
            SetStatus.COMPLETED -> "completed"
            SetStatus.SKIPPED -> "skipped"
        }
        val prLabel = if (set.isPersonalRecord) ", personal record" else ""
        "Set ${set.setNumber}, $typeLabel, $displayWeight kilograms, $displayReps reps, $statusLabel$prLabel"
    }

    val hasContextMenu = onSkipSet != null || onUnskipSet != null || onDeleteSet != null
    val longPressModifier = if (hasContextMenu && set.status != SetStatus.COMPLETED) {
        Modifier.combinedClickable(
            onClick = {},
            onLongClick = {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                showContextMenu = true
            },
        )
    } else {
        Modifier
    }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(radius.xs))
                .background(backgroundColor)
                .then(longPressModifier)
                .padding(vertical = 4.dp)
                .semantics { contentDescription = accessibilityText },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Type indicator bar (8dp wide)
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(topStart = radius.xs, bottomStart = radius.xs))
                    .background(typeIndicatorColor),
            )

            // Set number + optional PR star (32dp wide)
            Box(
                modifier = Modifier.width(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (set.isPersonalRecord) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Personal record",
                            modifier = Modifier.size(12.dp),
                            tint = PrGold,
                        )
                    }
                    Text(
                        text = set.setNumber.toString(),
                        style = typography.labelLarge,
                        color = textColor,
                        modifier = Modifier.alpha(textAlpha),
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Weight field (80dp wide, 48dp tall)
            ValueCell(
                value = formatWeight(displayWeight),
                isInteractive = isInteractive,
                isFocused = set.status == SetStatus.IN_PROGRESS,
                textColor = textColor,
                textAlpha = textAlpha,
                textDecoration = textDecoration,
                onClick = onWeightFieldClick,
                modifier = Modifier.width(80.dp),
            )

            // "x" separator (16dp wide)
            Box(
                modifier = Modifier.width(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "\u00D7",
                    style = typography.bodyMedium,
                    color = colors.onSurfaceTertiary,
                )
            }

            // Reps field (64dp wide, 48dp tall)
            ValueCell(
                value = displayReps.toString(),
                isInteractive = isInteractive,
                isFocused = set.status == SetStatus.IN_PROGRESS,
                textColor = textColor,
                textAlpha = textAlpha,
                textDecoration = textDecoration,
                onClick = onRepsFieldClick,
                modifier = Modifier.width(64.dp),
            )

            Spacer(modifier = Modifier.weight(1f))

            // Done checkbox (56dp x 56dp touch target)
            IconButton(
                onClick = onDoneClick,
                modifier = Modifier.defaultMinSize(minWidth = 56.dp, minHeight = 56.dp),
            ) {
                when (set.status) {
                    SetStatus.COMPLETED -> {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Mark set ${set.setNumber} incomplete",
                            modifier = Modifier.size(24.dp),
                            tint = colors.statusSuccess,
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "Mark set ${set.setNumber} as complete, double tap to activate",
                            modifier = Modifier.size(24.dp),
                            tint = colors.onSurfaceSecondary,
                        )
                    }
                }
            }
        }

        SetRowContextMenu(
            expanded = showContextMenu,
            onDismiss = { showContextMenu = false },
            set = set,
            onSkipSet = onSkipSet,
            onUnskipSet = onUnskipSet,
            onDeleteSet = onDeleteSet,
        )
    }
}

/**
 * Context menu for set row long-press actions: skip, unskip, delete.
 */
@Suppress("LongMethod")
@Composable
private fun SetRowContextMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    set: WorkoutSet,
    onSkipSet: (() -> Unit)?,
    onUnskipSet: (() -> Unit)?,
    onDeleteSet: (() -> Unit)?,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        if (set.status == SetStatus.SKIPPED && onUnskipSet != null) {
            DropdownMenuItem(
                text = { Text("Unskip set") },
                onClick = {
                    onDismiss()
                    onUnskipSet()
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Undo,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                },
            )
        }
        if (set.status != SetStatus.SKIPPED && set.status != SetStatus.COMPLETED && onSkipSet != null) {
            DropdownMenuItem(
                text = { Text("Skip this set") },
                onClick = {
                    onDismiss()
                    onSkipSet()
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.SkipNext,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                },
            )
        }
        if (set.status != SetStatus.COMPLETED && onDeleteSet != null) {
            DropdownMenuItem(
                text = {
                    Text(
                        "Delete set",
                        color = DeepRepsTheme.colors.statusError,
                    )
                },
                onClick = {
                    onDismiss()
                    onDeleteSet()
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = DeepRepsTheme.colors.statusError,
                    )
                },
            )
        }
    }
}

/**
 * Tappable value cell used for weight and reps display in [SetRow].
 *
 * When interactive, shows a tappable surface with focus ring when focused.
 * When non-interactive, shows static text.
 */
@Suppress("LongMethod")
@Composable
private fun ValueCell(
    value: String,
    isInteractive: Boolean,
    isFocused: Boolean,
    textColor: Color,
    textAlpha: Float,
    textDecoration: TextDecoration?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val radius = DeepRepsTheme.radius

    val cellModifier = modifier
        .height(48.dp)
        .clip(RoundedCornerShape(radius.xs))
        .then(
            if (isFocused) {
                Modifier.border(
                    width = 2.dp,
                    color = colors.borderFocus,
                    shape = RoundedCornerShape(radius.xs),
                )
            } else {
                Modifier
            },
        )
        .then(
            if (isInteractive) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            },
        )

    Box(
        modifier = cellModifier,
        contentAlignment = Alignment.CenterEnd,
    ) {
        Text(
            text = value,
            style = typography.numberSmall,
            color = textColor,
            textDecoration = textDecoration,
            textAlign = TextAlign.End,
            modifier = Modifier
                .alpha(textAlpha)
                .padding(horizontal = 4.dp),
        )
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun formatWeight(kg: Double): String {
    return if (kg % 1.0 == 0.0) {
        "${kg.toInt()}"
    } else {
        String.format(Locale.US, "%.1f", kg)
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Planned - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun PlannedDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        SetRow(
            set = WorkoutSet(
                setNumber = 1,
                type = SetType.WARMUP,
                status = SetStatus.PLANNED,
                plannedWeightKg = 40.0,
                plannedReps = 12,
                actualWeightKg = null,
                actualReps = null,
            ),
            onWeightFieldClick = {},
            onRepsFieldClick = {},
            onDoneClick = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(name = "In Progress - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun InProgressDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        SetRow(
            set = WorkoutSet(
                setNumber = 2,
                type = SetType.WORKING,
                status = SetStatus.IN_PROGRESS,
                plannedWeightKg = 80.0,
                plannedReps = 8,
                actualWeightKg = 80.0,
                actualReps = 8,
            ),
            onWeightFieldClick = {},
            onRepsFieldClick = {},
            onDoneClick = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(name = "Completed - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun CompletedDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        SetRow(
            set = WorkoutSet(
                setNumber = 3,
                type = SetType.WORKING,
                status = SetStatus.COMPLETED,
                plannedWeightKg = 80.0,
                plannedReps = 8,
                actualWeightKg = 80.0,
                actualReps = 8,
            ),
            onWeightFieldClick = {},
            onRepsFieldClick = {},
            onDoneClick = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(name = "PR - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun PrDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        SetRow(
            set = WorkoutSet(
                setNumber = 4,
                type = SetType.WORKING,
                status = SetStatus.COMPLETED,
                plannedWeightKg = 100.0,
                plannedReps = 8,
                actualWeightKg = 100.0,
                actualReps = 8,
                isPersonalRecord = true,
            ),
            onWeightFieldClick = {},
            onRepsFieldClick = {},
            onDoneClick = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(name = "Skipped - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun SkippedDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        SetRow(
            set = WorkoutSet(
                setNumber = 5,
                type = SetType.WORKING,
                status = SetStatus.SKIPPED,
                plannedWeightKg = 80.0,
                plannedReps = 8,
                actualWeightKg = null,
                actualReps = null,
            ),
            onWeightFieldClick = {},
            onRepsFieldClick = {},
            onDoneClick = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(
    name = "All States Column - Dark",
    showBackground = true,
    backgroundColor = 0xFF0A0A0F,
)
@Suppress("LongMethod")
@Composable
private fun AllStatesDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(8.dp)) {
            SetRow(
                set = WorkoutSet(
                    setNumber = 1,
                    type = SetType.WARMUP,
                    status = SetStatus.COMPLETED,
                    plannedWeightKg = 40.0,
                    plannedReps = 12,
                    actualWeightKg = 40.0,
                    actualReps = 12,
                ),
                onWeightFieldClick = {},
                onRepsFieldClick = {},
                onDoneClick = {},
            )
            Spacer(modifier = Modifier.height(4.dp))
            SetRow(
                set = WorkoutSet(
                    setNumber = 2,
                    type = SetType.WORKING,
                    status = SetStatus.COMPLETED,
                    plannedWeightKg = 80.0,
                    plannedReps = 8,
                    actualWeightKg = 80.0,
                    actualReps = 8,
                ),
                onWeightFieldClick = {},
                onRepsFieldClick = {},
                onDoneClick = {},
            )
            Spacer(modifier = Modifier.height(4.dp))
            SetRow(
                set = WorkoutSet(
                    setNumber = 3,
                    type = SetType.WORKING,
                    status = SetStatus.IN_PROGRESS,
                    plannedWeightKg = 80.0,
                    plannedReps = 8,
                    actualWeightKg = 80.0,
                    actualReps = 8,
                ),
                onWeightFieldClick = {},
                onRepsFieldClick = {},
                onDoneClick = {},
            )
            Spacer(modifier = Modifier.height(4.dp))
            SetRow(
                set = WorkoutSet(
                    setNumber = 4,
                    type = SetType.WORKING,
                    status = SetStatus.PLANNED,
                    plannedWeightKg = 80.0,
                    plannedReps = 8,
                    actualWeightKg = null,
                    actualReps = null,
                ),
                onWeightFieldClick = {},
                onRepsFieldClick = {},
                onDoneClick = {},
            )
        }
    }
}

@Preview(name = "Completed - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CompletedLightPreview() {
    DeepRepsTheme(darkTheme = false) {
        SetRow(
            set = WorkoutSet(
                setNumber = 1,
                type = SetType.WORKING,
                status = SetStatus.COMPLETED,
                plannedWeightKg = 60.0,
                plannedReps = 10,
                actualWeightKg = 60.0,
                actualReps = 10,
            ),
            onWeightFieldClick = {},
            onRepsFieldClick = {},
            onDoneClick = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}
