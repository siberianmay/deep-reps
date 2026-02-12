package com.deepreps.feature.exerciselibrary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deepreps.core.domain.model.enums.Difficulty
import com.deepreps.core.ui.component.ErrorState
import com.deepreps.core.ui.component.LoadingIndicator
import com.deepreps.core.ui.theme.DeepRepsTheme
import com.deepreps.feature.exerciselibrary.components.AnatomyDiagram
import com.deepreps.feature.exerciselibrary.components.difficultyDisplayName
import com.deepreps.feature.exerciselibrary.components.equipmentDisplayName
import com.deepreps.feature.exerciselibrary.components.movementTypeDisplayName

/**
 * Exercise detail screen showing full information about a single exercise.
 *
 * Design spec: design-system.md Section 4.5.
 * - Name, tags row (equipment, movement type, difficulty)
 * - Anatomy diagram placeholder
 * - Pros / Key Benefits section
 * - Tips / Cues section
 * - Secondary muscles list
 * - Scrollable content
 */
@Composable
fun ExerciseDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExerciseDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ExerciseDetailContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun ExerciseDetailContent(
    state: ExerciseDetailUiState,
    onIntent: (ExerciseDetailIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = state.exercise?.name ?: "Exercise Detail",
                    style = typography.headlineMedium,
                    maxLines = 1,
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

        when {
            state.isLoading -> {
                LoadingIndicator(message = "Loading exercise...")
            }

            state.errorType != null -> {
                ErrorState(
                    message = when (state.errorType) {
                        ExerciseDetailError.NotFound -> "Exercise not found."
                        ExerciseDetailError.LoadFailed -> "Failed to load exercise details."
                    },
                    onRetry = { onIntent(ExerciseDetailIntent.Retry) },
                )
            }

            state.exercise != null -> {
                ExerciseDetailBody(exercise = state.exercise)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExerciseDetailBody(exercise: ExerciseDetailUi) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.space4),
    ) {
        Spacer(modifier = Modifier.height(spacing.space4))

        // Exercise name
        Text(
            text = exercise.name,
            style = typography.displaySmall,
            color = colors.onSurfacePrimary,
        )

        Spacer(modifier = Modifier.height(spacing.space2))

        // Tags row
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.space2),
            verticalArrangement = Arrangement.spacedBy(spacing.space2),
        ) {
            DetailTagChip(
                text = equipmentDisplayName(exercise.equipment),
                containerColor = colors.surfaceHighest,
                contentColor = colors.onSurfaceSecondary,
            )
            DetailTagChip(
                text = movementTypeDisplayName(exercise.movementType),
                containerColor = colors.surfaceHighest,
                contentColor = colors.onSurfaceSecondary,
            )
            DifficultyDetailChip(difficulty = exercise.difficulty)
        }

        Spacer(modifier = Modifier.height(spacing.space5))

        // Anatomy diagram placeholder (200dp tall per spec)
        AnatomyDiagram(
            exerciseId = exercise.id,
            primaryGroupId = exercise.primaryGroupId,
        )

        Spacer(modifier = Modifier.height(spacing.space5))

        // Description
        if (exercise.description.isNotBlank()) {
            Text(
                text = exercise.description,
                style = typography.bodyMedium,
                color = colors.onSurfaceSecondary,
            )
            Spacer(modifier = Modifier.height(spacing.space5))
        }

        // Pros / Key Benefits
        if (exercise.pros.isNotEmpty()) {
            BulletSection(
                title = "Key Benefits",
                items = exercise.pros,
                icon = Icons.Outlined.CheckCircle,
                iconTint = colors.statusSuccess,
            )
            Spacer(modifier = Modifier.height(spacing.space5))
        }

        // Tips / Cues
        if (exercise.tips.isNotEmpty()) {
            BulletSection(
                title = "Tips",
                items = exercise.tips,
                icon = Icons.Outlined.Lightbulb,
                iconTint = colors.statusWarning,
            )
            Spacer(modifier = Modifier.height(spacing.space5))
        }

        // Secondary muscles
        if (exercise.secondaryMuscles.isNotEmpty()) {
            Text(
                text = "Secondary Muscles",
                style = typography.headlineSmall,
                color = colors.onSurfacePrimary,
            )
            Spacer(modifier = Modifier.height(spacing.space2))
            Text(
                text = exercise.secondaryMuscles.joinToString(", "),
                style = typography.bodyMedium,
                color = colors.onSurfaceSecondary,
            )
        }

        // Bottom spacing for scroll overscroll
        Spacer(modifier = Modifier.height(spacing.space8))
    }
}

/**
 * A section with a title, icon, and bullet-pointed items.
 * Used for Pros and Tips sections per design-system.md Section 4.5.
 */
@Composable
private fun BulletSection(
    title: String,
    items: List<String>,
    icon: ImageVector,
    iconTint: Color,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing

    Text(
        text = title,
        style = typography.headlineSmall,
        color = colors.onSurfacePrimary,
    )

    Spacer(modifier = Modifier.height(spacing.space2))

    items.forEach { item ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = spacing.space1),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = iconTint,
            )
            Spacer(modifier = Modifier.width(spacing.space2))
            Text(
                text = item,
                style = typography.bodyMedium,
                color = colors.onSurfacePrimary,
            )
        }
    }
}

/**
 * Tag chip for the detail screen tags row.
 */
@Composable
private fun DetailTagChip(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(DeepRepsTheme.radius.sm),
        color = containerColor,
    ) {
        Text(
            text = text,
            style = DeepRepsTheme.typography.labelMedium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

/**
 * Color-coded difficulty chip for the detail screen.
 */
@Composable
private fun DifficultyDetailChip(
    difficulty: Difficulty,
    modifier: Modifier = Modifier,
) {
    val colors = DeepRepsTheme.colors

    val (containerColor, contentColor) = when (difficulty) {
        Difficulty.BEGINNER -> colors.statusSuccess.copy(alpha = 0.15f) to colors.statusSuccess
        Difficulty.INTERMEDIATE -> colors.statusWarning.copy(alpha = 0.15f) to colors.statusWarning
        Difficulty.ADVANCED -> colors.statusError.copy(alpha = 0.15f) to colors.statusError
    }

    DetailTagChip(
        text = difficultyDisplayName(difficulty),
        containerColor = containerColor,
        contentColor = contentColor,
        modifier = modifier,
    )
}
