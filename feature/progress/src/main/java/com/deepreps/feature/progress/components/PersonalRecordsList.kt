package com.deepreps.feature.progress.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deepreps.core.domain.model.enums.MuscleGroup
import com.deepreps.core.domain.model.enums.WeightUnit
import com.deepreps.core.ui.component.EmptyState
import com.deepreps.core.ui.component.LoadingIndicator
import com.deepreps.core.ui.theme.DeepRepsTheme
import com.deepreps.feature.progress.PrSummaryUi
import java.util.Locale

private const val KG_TO_LBS = 2.20462

/**
 * Content for the Records tab on the progress dashboard.
 *
 * Shows personal records grouped by muscle group with expand/collapse sections.
 * Each PR row navigates to the exercise progress screen on tap.
 */
@Composable
fun PersonalRecordsContent(
    records: Map<MuscleGroup, List<PrSummaryUi>>,
    weightUnit: WeightUnit,
    isLoading: Boolean,
    onExerciseClick: (exerciseId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        isLoading -> {
            LoadingIndicator(message = "Loading records...")
        }

        records.isEmpty() -> {
            EmptyState(
                title = "No records yet",
                message = "Complete your first workout to start tracking records",
            )
        }

        else -> {
            PersonalRecordsList(
                records = records,
                weightUnit = weightUnit,
                onExerciseClick = onExerciseClick,
                modifier = modifier,
            )
        }
    }
}

@Suppress("LongMethod")
@Composable
private fun PersonalRecordsList(
    records: Map<MuscleGroup, List<PrSummaryUi>>,
    weightUnit: WeightUnit,
    onExerciseClick: (exerciseId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val expandedState = remember { mutableStateMapOf<MuscleGroup, Boolean>() }
    val spacing = DeepRepsTheme.spacing

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = spacing.space4,
            vertical = spacing.space3,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.space2),
    ) {
        records.forEach { (muscleGroup, prList) ->
            val isExpanded = expandedState.getOrElse(muscleGroup) { true }

            item(key = "header_$muscleGroup") {
                MuscleGroupHeader(
                    muscleGroup = muscleGroup,
                    isExpanded = isExpanded,
                    onToggle = {
                        expandedState[muscleGroup] = !isExpanded
                    },
                )
            }

            item(key = "content_$muscleGroup") {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(spacing.space2),
                    ) {
                        prList.forEach { pr ->
                            PrRow(
                                pr = pr,
                                weightUnit = weightUnit,
                                onClick = { onExerciseClick(pr.exerciseId) },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Section header for a muscle group in the PR list.
 * 48dp tall with color bar, group name, and expand/collapse chevron.
 */
@Composable
private fun MuscleGroupHeader(
    muscleGroup: MuscleGroup,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing
    val groupColor = colors.colorForMuscleGroup(muscleGroup)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(48.dp)
                .background(groupColor),
        )

        Spacer(modifier = Modifier.width(spacing.space3))

        Text(
            text = muscleGroupDisplayName(muscleGroup),
            style = typography.headlineSmall,
            color = colors.onSurfacePrimary,
            modifier = Modifier.weight(1f),
        )

        Icon(
            imageVector = if (isExpanded) {
                Icons.Filled.ExpandLess
            } else {
                Icons.Filled.ExpandMore
            },
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            modifier = Modifier.size(24.dp),
            tint = colors.onSurfaceTertiary,
        )
    }
}

/**
 * Single PR row: exercise name, weight, date, and navigation chevron.
 * 64dp tall with muscle group color bar on the left edge.
 */
@Suppress("LongMethod")
@Composable
private fun PrRow(
    pr: PrSummaryUi,
    weightUnit: WeightUnit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val radius = DeepRepsTheme.radius
    val spacing = DeepRepsTheme.spacing
    val groupColor = colors.colorForMuscleGroup(pr.muscleGroup)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(radius.md),
        color = colors.surfaceLow,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(64.dp)
                    .background(groupColor),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = spacing.space3),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = pr.exerciseName,
                    style = typography.bodyLarge,
                    color = colors.onSurfacePrimary,
                )

                Text(
                    text = "${formatWeight(pr.bestWeightKg, weightUnit)} \u00B7 ${pr.achievedAtText}",
                    style = typography.bodySmall,
                    color = colors.onSurfaceSecondary,
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View exercise progress",
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = spacing.space3),
                tint = colors.onSurfaceTertiary,
            )

            Spacer(modifier = Modifier.width(spacing.space3))
        }
    }
}

private fun formatWeight(weightKg: Double, unit: WeightUnit): String {
    val value = when (unit) {
        WeightUnit.KG -> weightKg
        WeightUnit.LBS -> weightKg * KG_TO_LBS
    }
    return String.format(Locale.US, "%.1f%s", value, unit.value)
}

private fun muscleGroupDisplayName(muscleGroup: MuscleGroup): String = when (muscleGroup) {
    MuscleGroup.LEGS -> "Legs"
    MuscleGroup.LOWER_BACK -> "Lower Back"
    MuscleGroup.CHEST -> "Chest"
    MuscleGroup.BACK -> "Back"
    MuscleGroup.SHOULDERS -> "Shoulders"
    MuscleGroup.ARMS -> "Arms"
    MuscleGroup.CORE -> "Core"
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "PR List - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun PrListDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        PersonalRecordsContent(
            records = mapOf(
                MuscleGroup.CHEST to listOf(
                    PrSummaryUi(
                        exerciseId = 1,
                        exerciseName = "Barbell Bench Press",
                        muscleGroup = MuscleGroup.CHEST,
                        bestWeightKg = 100.0,
                        bestReps = 5,
                        achievedAtText = "Feb 10, 2026",
                    ),
                    PrSummaryUi(
                        exerciseId = 2,
                        exerciseName = "Incline Dumbbell Press",
                        muscleGroup = MuscleGroup.CHEST,
                        bestWeightKg = 40.0,
                        bestReps = 8,
                        achievedAtText = "Feb 8, 2026",
                    ),
                ),
                MuscleGroup.LEGS to listOf(
                    PrSummaryUi(
                        exerciseId = 3,
                        exerciseName = "Barbell Squat",
                        muscleGroup = MuscleGroup.LEGS,
                        bestWeightKg = 140.0,
                        bestReps = 3,
                        achievedAtText = "Feb 12, 2026",
                    ),
                ),
            ),
            weightUnit = WeightUnit.KG,
            isLoading = false,
            onExerciseClick = {},
        )
    }
}

@Preview(name = "PR List Empty - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun PrListEmptyDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        PersonalRecordsContent(
            records = emptyMap(),
            weightUnit = WeightUnit.KG,
            isLoading = false,
            onExerciseClick = {},
        )
    }
}

@Preview(name = "PR List Loading - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun PrListLoadingDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        PersonalRecordsContent(
            records = emptyMap(),
            weightUnit = WeightUnit.KG,
            isLoading = true,
            onExerciseClick = {},
        )
    }
}
