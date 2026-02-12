package com.deepreps.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Reusable error state composable with icon, message, and optional retry button.
 *
 * @param message User-facing error description.
 * @param modifier External modifier.
 * @param onRetry If non-null, a retry button is displayed.
 */
@Suppress("LongMethod")
@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
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
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = colors.statusError,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                style = typography.bodyLarge,
                color = colors.onSurfaceSecondary,
                textAlign = TextAlign.Center,
            )

            if (onRetry != null) {
                Spacer(modifier = Modifier.height(24.dp))
                DeepRepsButton(
                    text = "Retry",
                    onClick = onRetry,
                    variant = ButtonVariant.Secondary,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Error with retry - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun ErrorRetryDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        ErrorState(
            message = "Failed to load exercises.\nCheck your connection and try again.",
            onRetry = {},
        )
    }
}

@Preview(name = "Error no retry - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun ErrorNoRetryDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        ErrorState(message = "Something went wrong.")
    }
}

@Preview(name = "Error - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ErrorLightPreview() {
    DeepRepsTheme(darkTheme = false) {
        ErrorState(
            message = "Could not generate workout plan.",
            onRetry = {},
        )
    }
}
