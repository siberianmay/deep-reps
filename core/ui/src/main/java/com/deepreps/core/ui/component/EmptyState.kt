package com.deepreps.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Reusable empty state composable with icon and message.
 *
 * @param title Heading text.
 * @param message Descriptive subtitle.
 * @param icon Icon displayed above the text. Defaults to an inbox icon.
 * @param modifier External modifier.
 * @param action Optional action composable (e.g., a button).
 */
@Suppress("LongMethod")
@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Inbox,
    action: @Composable (() -> Unit)? = null,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = colors.onSurfaceTertiary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = typography.headlineSmall,
                color = colors.onSurfacePrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = typography.bodyMedium,
                color = colors.onSurfaceSecondary,
                textAlign = TextAlign.Center,
            )

            if (action != null) {
                Spacer(modifier = Modifier.height(24.dp))
                action()
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Empty - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun EmptyDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        EmptyState(
            title = "No workouts yet",
            message = "Complete your first workout to see it here",
        )
    }
}

@Preview(name = "Empty with action - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun EmptyWithActionDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        EmptyState(
            title = "No templates yet",
            message = "Save a workout as a template to reuse it",
            action = {
                DeepRepsButton(
                    text = "Create Template",
                    onClick = {},
                    variant = ButtonVariant.Secondary,
                )
            },
        )
    }
}

@Preview(name = "Empty - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun EmptyLightPreview() {
    DeepRepsTheme(darkTheme = false) {
        EmptyState(
            title = "No workouts yet",
            message = "Complete your first workout to see it here",
            action = {
                DeepRepsButton(
                    text = "Start Workout",
                    onClick = {},
                    variant = ButtonVariant.Secondary,
                )
            },
        )
    }
}
