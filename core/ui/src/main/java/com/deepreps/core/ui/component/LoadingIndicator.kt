package com.deepreps.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Branded loading state using the theme accent color.
 *
 * @param modifier External modifier.
 * @param message Optional message below the spinner.
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography

    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = message ?: "Loading" },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = colors.accentPrimary,
                trackColor = colors.surfaceHigh,
                strokeWidth = 4.dp,
            )
            if (message != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    style = typography.bodyMedium,
                    color = colors.onSurfaceSecondary,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Loading - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun LoadingDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        LoadingIndicator()
    }
}

@Preview(name = "Loading with message - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun LoadingMessageDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        LoadingIndicator(message = "Generating workout plan...")
    }
}

@Preview(name = "Loading - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun LoadingLightPreview() {
    DeepRepsTheme(darkTheme = false) {
        LoadingIndicator(message = "Loading exercises...")
    }
}
