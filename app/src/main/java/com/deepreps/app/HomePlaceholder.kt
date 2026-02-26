package com.deepreps.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deepreps.core.ui.component.DeepRepsButton
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Home dashboard screen. Replaces the former placeholder.
 *
 * Shows active workout banner, start/template CTAs, recent templates,
 * and last workout summary.
 */
@Suppress("LongMethod")
@Composable
fun HomeScreen(
    onStartWorkout: () -> Unit,
    onFromTemplate: () -> Unit,
    onResumeWorkout: (Long) -> Unit,
    onSessionDetail: (Long) -> Unit,
    onTemplateSelected: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing
    val radius = DeepRepsTheme.radius

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.space4),
    ) {
        Spacer(modifier = Modifier.height(spacing.space8))

        // 1. Title
        Text(
            text = "Deep Reps",
            style = typography.headlineLarge,
            color = colors.onSurfacePrimary,
        )

        Spacer(modifier = Modifier.height(spacing.space6))

        // 2. Active workout banner
        val activeSessionId = uiState.activeSessionId
        if (uiState.hasActiveSession && activeSessionId != null) {
            ActiveWorkoutBanner(
                onResume = { onResumeWorkout(activeSessionId) },
            )
            Spacer(modifier = Modifier.height(spacing.space4))
        }

        // 3. Start Workout CTA
        Card(
            onClick = onStartWorkout,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(radius.md),
            colors = CardDefaults.cardColors(
                containerColor = colors.accentPrimary,
            ),
        ) {
            Column(
                modifier = Modifier.padding(spacing.space4),
            ) {
                Text(
                    text = "Start Workout",
                    style = typography.headlineSmall,
                    color = colors.onSurfacePrimary,
                )
                Spacer(modifier = Modifier.height(spacing.space1))
                Text(
                    text = "Select muscle groups",
                    style = typography.bodyMedium,
                    color = colors.onSurfacePrimary.copy(alpha = 0.8f),
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.space3))

        // 4. From Template CTA
        Card(
            onClick = onFromTemplate,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(radius.md),
            colors = CardDefaults.cardColors(
                containerColor = colors.surfaceMedium,
            ),
        ) {
            Column(
                modifier = Modifier.padding(spacing.space4),
            ) {
                Text(
                    text = "From Template",
                    style = typography.headlineSmall,
                    color = colors.onSurfacePrimary,
                )
            }
        }

        // 5. Recent Templates
        if (uiState.recentTemplates.isNotEmpty()) {
            Spacer(modifier = Modifier.height(spacing.space6))

            RecentTemplatesSection(
                templates = uiState.recentTemplates,
                onTemplateSelected = onTemplateSelected,
            )
        }

        // 6. Last Workout
        val lastWorkout = uiState.lastWorkout
        if (lastWorkout != null) {
            Spacer(modifier = Modifier.height(spacing.space6))

            LastWorkoutSection(
                lastWorkout = lastWorkout,
                onClick = { onSessionDetail(lastWorkout.sessionId) },
            )
        }

        Spacer(modifier = Modifier.height(spacing.space8))
    }
}

/**
 * Banner shown when an active or paused workout session exists.
 */
@Composable
private fun ActiveWorkoutBanner(
    onResume: () -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing
    val radius = DeepRepsTheme.radius

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(radius.md),
        colors = CardDefaults.cardColors(
            containerColor = colors.statusWarningContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.space4),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Workout in progress",
                style = typography.bodyLarge,
                color = colors.statusWarning,
                modifier = Modifier.weight(1f),
            )

            DeepRepsButton(
                text = "Resume",
                onClick = onResume,
            )
        }
    }
}

/**
 * Horizontal scrolling row of recent template cards.
 */
@Composable
private fun RecentTemplatesSection(
    templates: List<TemplateInfo>,
    onTemplateSelected: (Long) -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing

    Text(
        text = "Recent Templates",
        style = typography.headlineSmall,
        color = colors.onSurfacePrimary,
    )

    Spacer(modifier = Modifier.height(spacing.space3))

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(spacing.space3),
        contentPadding = PaddingValues(end = spacing.space4),
    ) {
        items(
            items = templates,
            key = { it.id },
        ) { template ->
            RecentTemplateCard(
                template = template,
                onClick = { onTemplateSelected(template.id) },
            )
        }
    }
}

/**
 * Single template card in the recent templates row.
 */
@Composable
private fun RecentTemplateCard(
    template: TemplateInfo,
    onClick: () -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing
    val radius = DeepRepsTheme.radius

    Card(
        onClick = onClick,
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(radius.md),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(spacing.space3),
        ) {
            Text(
                text = template.name,
                style = typography.labelLarge,
                color = colors.onSurfacePrimary,
                maxLines = 2,
            )

            if (template.exerciseCount > 0) {
                Spacer(modifier = Modifier.height(spacing.space1))
                Text(
                    text = "${template.exerciseCount} exercises",
                    style = typography.bodySmall,
                    color = colors.onSurfaceSecondary,
                )
            }
        }
    }
}

/**
 * Card showing the most recent completed workout.
 */
@Composable
private fun LastWorkoutSection(
    lastWorkout: LastWorkoutInfo,
    onClick: () -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing
    val radius = DeepRepsTheme.radius

    Text(
        text = "Last Workout",
        style = typography.headlineSmall,
        color = colors.onSurfacePrimary,
    )

    Spacer(modifier = Modifier.height(spacing.space3))

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(radius.md),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(spacing.space4),
        ) {
            Text(
                text = lastWorkout.dateText,
                style = typography.labelLarge,
                color = colors.onSurfacePrimary,
            )

            if (lastWorkout.durationMinutes > 0) {
                Spacer(modifier = Modifier.height(spacing.space1))
                Text(
                    text = "${lastWorkout.durationMinutes} min",
                    style = typography.bodyMedium,
                    color = colors.onSurfaceSecondary,
                )
            }
        }
    }
}
