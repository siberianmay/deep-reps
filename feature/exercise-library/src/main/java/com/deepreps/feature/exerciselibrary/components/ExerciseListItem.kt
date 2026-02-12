package com.deepreps.feature.exerciselibrary.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deepreps.core.domain.model.enums.Difficulty
import com.deepreps.core.domain.model.enums.Equipment
import com.deepreps.core.domain.model.enums.MovementType
import com.deepreps.core.ui.theme.DeepRepsTheme
import com.deepreps.feature.exerciselibrary.ExerciseUi

/**
 * Reusable row component for exercise lists.
 *
 * Displays exercise name, equipment tag, movement type tag, and color-coded difficulty chip.
 * Supports a checkable variant for the selection screen.
 *
 * Design spec: design-system.md Section 4.4.
 * - Each item: 72dp height, full width.
 * - Checkbox variant: checkbox on left (48dp touch target).
 *
 * @param exercise The exercise UI model to display.
 * @param onClick Called when the row body is tapped (navigate to detail).
 * @param modifier External modifier.
 * @param isCheckable Whether to show a checkbox (selection screen variant).
 * @param isChecked Whether the checkbox is checked. Ignored if [isCheckable] is false.
 * @param onCheckedChange Called when the checkbox is toggled. Ignored if [isCheckable] is false.
 */
@Suppress("LongMethod")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseListItem(
    exercise: ExerciseUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCheckable: Boolean = false,
    isChecked: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing

    val accessibilityText = buildString {
        append(exercise.name)
        append(", ${equipmentDisplayName(exercise.equipment)}")
        append(", ${difficultyDisplayName(exercise.difficulty)}")
        if (isCheckable) {
            append(if (isChecked) ", selected" else ", not selected")
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.space4)
            .semantics { contentDescription = accessibilityText },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isCheckable) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = colors.accentPrimary,
                    uncheckedColor = colors.onSurfaceSecondary,
                    checkmarkColor = colors.surfaceLowest,
                ),
            )
            Spacer(modifier = Modifier.width(spacing.space2))
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = exercise.name,
                style = typography.bodyLarge,
                color = colors.onSurfacePrimary,
                maxLines = 1,
            )

            Spacer(modifier = Modifier.height(spacing.space1))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(spacing.space2),
                verticalArrangement = Arrangement.spacedBy(spacing.space1),
            ) {
                SmallTagChip(
                    text = equipmentDisplayName(exercise.equipment),
                    containerColor = colors.surfaceHighest,
                    contentColor = colors.onSurfaceSecondary,
                )
                SmallTagChip(
                    text = movementTypeDisplayName(exercise.movementType),
                    containerColor = colors.surfaceHighest,
                    contentColor = colors.onSurfaceSecondary,
                )
                DifficultyChip(difficulty = exercise.difficulty)
            }
        }
    }
}

/**
 * Small tag chip used within exercise list items.
 */
@Composable
private fun SmallTagChip(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(DeepRepsTheme.radius.xs),
        color = containerColor,
    ) {
        Text(
            text = text,
            style = DeepRepsTheme.typography.labelMedium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

/**
 * Color-coded difficulty chip per design-system.md Section 4.5:
 * - Beginner: [statusSuccess] tint
 * - Intermediate: [statusWarning] tint
 * - Advanced: [statusError] tint
 */
@Composable
private fun DifficultyChip(
    difficulty: Difficulty,
    modifier: Modifier = Modifier,
) {
    val colors = DeepRepsTheme.colors

    val (containerColor, contentColor) = when (difficulty) {
        Difficulty.BEGINNER -> colors.statusSuccess.copy(alpha = 0.15f) to colors.statusSuccess
        Difficulty.INTERMEDIATE -> colors.statusWarning.copy(alpha = 0.15f) to colors.statusWarning
        Difficulty.ADVANCED -> colors.statusError.copy(alpha = 0.15f) to colors.statusError
    }

    SmallTagChip(
        text = difficultyDisplayName(difficulty),
        containerColor = containerColor,
        contentColor = contentColor,
        modifier = modifier,
    )
}

// ---------------------------------------------------------------------------
// Display name helpers
// ---------------------------------------------------------------------------

internal fun equipmentDisplayName(equipment: Equipment): String = when (equipment) {
    Equipment.BARBELL -> "Barbell"
    Equipment.DUMBBELL -> "Dumbbell"
    Equipment.CABLE -> "Cable"
    Equipment.MACHINE -> "Machine"
    Equipment.BODYWEIGHT -> "Bodyweight"
    Equipment.KETTLEBELL -> "Kettlebell"
    Equipment.BAND -> "Band"
    Equipment.EZ_BAR -> "EZ Bar"
    Equipment.TRAP_BAR -> "Trap Bar"
}

internal fun movementTypeDisplayName(movementType: MovementType): String = when (movementType) {
    MovementType.COMPOUND -> "Compound"
    MovementType.ISOLATION -> "Isolation"
}

internal fun difficultyDisplayName(difficulty: Difficulty): String = when (difficulty) {
    Difficulty.BEGINNER -> "Beginner"
    Difficulty.INTERMEDIATE -> "Intermediate"
    Difficulty.ADVANCED -> "Advanced"
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Exercise item - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun ExerciseItemDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        ExerciseListItem(
            exercise = ExerciseUi(
                id = 1,
                name = "Barbell Squat",
                equipment = Equipment.BARBELL,
                movementType = MovementType.COMPOUND,
                difficulty = Difficulty.INTERMEDIATE,
                primaryGroupId = 1,
            ),
            onClick = {},
        )
    }
}

@Preview(name = "Checkable selected - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun CheckableSelectedDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        ExerciseListItem(
            exercise = ExerciseUi(
                id = 2,
                name = "Bench Press",
                equipment = Equipment.BARBELL,
                movementType = MovementType.COMPOUND,
                difficulty = Difficulty.BEGINNER,
                primaryGroupId = 3,
            ),
            onClick = {},
            isCheckable = true,
            isChecked = true,
            onCheckedChange = {},
        )
    }
}

@Preview(name = "Advanced exercise - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun AdvancedExerciseDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        ExerciseListItem(
            exercise = ExerciseUi(
                id = 3,
                name = "Muscle-Up",
                equipment = Equipment.BODYWEIGHT,
                movementType = MovementType.COMPOUND,
                difficulty = Difficulty.ADVANCED,
                primaryGroupId = 4,
            ),
            onClick = {},
        )
    }
}
