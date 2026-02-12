@file:Suppress("MatchingDeclarationName")

package com.deepreps.core.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Visual variant for [DeepRepsButton].
 */
enum class ButtonVariant {
    /** Filled accent-primary background. Main CTAs. */
    Primary,

    /** Outlined with accent-primary border. Secondary actions. */
    Secondary,

    /** Filled status-error background. Destructive actions. */
    Destructive,
}

/**
 * Standard button for Deep Reps. Minimum touch target: 48dp.
 *
 * @param text Button label.
 * @param onClick Click handler.
 * @param variant Visual style.
 * @param enabled Interactive state.
 * @param modifier External modifier.
 */
@Suppress("LongMethod")
@Composable
fun DeepRepsButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    enabled: Boolean = true,
) {
    val colors = DeepRepsTheme.colors
    val radius = DeepRepsTheme.radius
    val shape = RoundedCornerShape(radius.md)

    when (variant) {
        ButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .heightIn(min = 48.dp),
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accentPrimary,
                    contentColor = colors.onSurfacePrimary,
                    disabledContainerColor = colors.surfaceHighest,
                    disabledContentColor = colors.onSurfaceTertiary,
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Text(
                    text = text,
                    style = DeepRepsTheme.typography.labelLarge,
                )
            }
        }

        ButtonVariant.Secondary -> {
            OutlinedButton(
                onClick = onClick,
                enabled = enabled,
                modifier = modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .heightIn(min = 48.dp),
                shape = shape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.accentPrimary,
                    disabledContentColor = colors.onSurfaceTertiary,
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Text(
                    text = text,
                    style = DeepRepsTheme.typography.labelLarge,
                )
            }
        }

        ButtonVariant.Destructive -> {
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .heightIn(min = 48.dp),
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.statusError,
                    contentColor = colors.onSurfacePrimary,
                    disabledContainerColor = colors.surfaceHighest,
                    disabledContentColor = colors.onSurfaceTertiary,
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Text(
                    text = text,
                    style = DeepRepsTheme.typography.labelLarge,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Primary - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun PrimaryDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        DeepRepsButton(text = "Start Workout", onClick = {})
    }
}

@Preview(name = "Primary - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PrimaryLightPreview() {
    DeepRepsTheme(darkTheme = false) {
        DeepRepsButton(text = "Start Workout", onClick = {})
    }
}

@Preview(name = "Secondary - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun SecondaryDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        DeepRepsButton(text = "Cancel", onClick = {}, variant = ButtonVariant.Secondary)
    }
}

@Preview(name = "Destructive - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun DestructiveDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        DeepRepsButton(text = "Delete", onClick = {}, variant = ButtonVariant.Destructive)
    }
}

@Preview(name = "Disabled - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun DisabledDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        DeepRepsButton(text = "Next", onClick = {}, enabled = false)
    }
}
