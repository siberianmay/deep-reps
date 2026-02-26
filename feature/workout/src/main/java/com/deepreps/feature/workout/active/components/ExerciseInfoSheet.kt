package com.deepreps.feature.workout.active.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deepreps.core.domain.model.Exercise
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Bottom sheet displaying exercise details: description, form cues, muscles, equipment.
 */
@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseInfoSheet(
    exercise: Exercise,
    onDismiss: () -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceMedium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            // Exercise name
            Text(
                text = exercise.name,
                style = typography.headlineLarge,
                color = colors.onSurfacePrimary,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Equipment + Movement type + Difficulty
            Row {
                Text(
                    text = exercise.equipment.value,
                    style = typography.labelMedium,
                    color = colors.onSurfaceSecondary,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = exercise.movementType.value,
                    style = typography.labelMedium,
                    color = colors.onSurfaceSecondary,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = exercise.difficulty.value,
                    style = typography.labelMedium,
                    color = colors.onSurfaceSecondary,
                )
            }

            if (exercise.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = exercise.description,
                    style = typography.bodyMedium,
                    color = colors.onSurfacePrimary,
                )
            }

            // Form cues / tips
            if (exercise.tips.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Form Cues",
                    style = typography.headlineMedium,
                    color = colors.onSurfacePrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                exercise.tips.forEach { tip ->
                    Text(
                        text = "\u2022 $tip",
                        style = typography.bodyMedium,
                        color = colors.onSurfaceSecondary,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }

            // Primary muscle group
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Target Muscles",
                style = typography.headlineMedium,
                color = colors.onSurfacePrimary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (exercise.secondaryMuscles.isNotEmpty()) {
                Text(
                    text = exercise.secondaryMuscles.joinToString(", "),
                    style = typography.bodyMedium,
                    color = colors.onSurfaceSecondary,
                )
            }

            // Pros
            if (exercise.pros.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Benefits",
                    style = typography.headlineMedium,
                    color = colors.onSurfacePrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                exercise.pros.forEach { pro ->
                    Text(
                        text = "\u2022 $pro",
                        style = typography.bodyMedium,
                        color = colors.onSurfaceSecondary,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }

            // Bottom spacing
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
