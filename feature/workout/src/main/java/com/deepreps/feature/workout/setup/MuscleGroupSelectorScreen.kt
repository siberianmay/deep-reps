package com.deepreps.feature.workout.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.deepreps.core.domain.model.enums.MuscleGroup
import com.deepreps.core.ui.component.DeepRepsButton
import com.deepreps.core.ui.component.muscleGroupDisplayName
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Muscle Group Selector screen for workout setup.
 *
 * Design spec: design-system.md Section 4.3 + Section 3.3.
 * - 2-column grid of 7 muscle groups
 * - Each group card: 100dp tall, muscle group color accents
 * - 7th item spans full width at bottom
 * - Bottom action area: selected count + Next button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuscleGroupSelectorScreen(
    selectedGroups: Set<MuscleGroup>,
    onToggleGroup: (MuscleGroup) -> Unit,
    onNextClicked: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val spacing = DeepRepsTheme.spacing
    val typography = DeepRepsTheme.typography

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "Select Muscle Groups",
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

        // Grid content
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = spacing.space4),
            contentPadding = PaddingValues(top = spacing.space4, bottom = spacing.space4),
            horizontalArrangement = Arrangement.spacedBy(spacing.space4),
            verticalArrangement = Arrangement.spacedBy(spacing.space4),
        ) {
            val groups = MuscleGroup.entries.toList()
            val regularGroups = groups.dropLast(1)
            val lastGroup = groups.last()

            items(
                items = regularGroups,
                key = { it.name },
            ) { group ->
                MuscleGroupCard(
                    group = group,
                    isSelected = group in selectedGroups,
                    onClick = { onToggleGroup(group) },
                )
            }

            // Last item spans full width per design spec Section 3.3
            item(
                key = lastGroup.name,
                span = { GridItemSpan(2) },
            ) {
                MuscleGroupCard(
                    group = lastGroup,
                    isSelected = lastGroup in selectedGroups,
                    onClick = { onToggleGroup(lastGroup) },
                )
            }
        }

        // Bottom action area
        HorizontalDivider(color = colors.borderSubtle, thickness = 1.dp)

        BottomGroupActionBar(
            selectedCount = selectedGroups.size,
            onNext = onNextClicked,
        )
    }
}

/**
 * Individual muscle group card for the selector grid.
 *
 * Per design spec Section 3.3:
 * - 100dp height, radius-md
 * - Unselected: surface-low, border-subtle 1dp
 * - Selected: group color 15% opacity, group color 2dp border, checkmark badge
 */
@Composable
private fun MuscleGroupCard(
    group: MuscleGroup,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val radius = DeepRepsTheme.radius
    val spacing = DeepRepsTheme.spacing

    val groupColor = colors.colorForMuscleGroup(group)
    val displayName = remember(group) { muscleGroupDisplayName(group) }

    val containerColor = if (isSelected) {
        groupColor.copy(alpha = 0.15f)
    } else {
        colors.surfaceLow
    }

    val border = if (isSelected) {
        BorderStroke(2.dp, groupColor)
    } else {
        BorderStroke(1.dp, colors.borderSubtle)
    }

    val accessibilityText =
        "$displayName, ${if (isSelected) "selected" else "not selected"}"

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .semantics { contentDescription = accessibilityText },
        shape = RoundedCornerShape(radius.md),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = border,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing.space3),
        ) {
            // TODO: Replace with custom muscle group icon when assets are available
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = displayName,
                    style = typography.labelLarge,
                    color = if (isSelected) groupColor else colors.onSurfaceSecondary,
                )
            }

            // Checkmark badge when selected
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp),
                    tint = groupColor,
                )
            }
        }
    }
}

/**
 * Bottom action bar: selected count + Next button.
 *
 * Design spec Section 4.3: 80dp tall, surface-low, Next button 56dp x 120dp.
 */
@Composable
private fun BottomGroupActionBar(
    selectedCount: Int,
    onNext: () -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing

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
            text = "$selectedCount group${if (selectedCount != 1) "s" else ""} selected",
            style = typography.bodyLarge,
            color = colors.onSurfacePrimary,
        )

        DeepRepsButton(
            text = "Next",
            onClick = onNext,
            enabled = selectedCount > 0,
            modifier = Modifier
                .width(120.dp)
                .height(56.dp),
        )
    }
}
