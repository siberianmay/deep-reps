package com.deepreps.core.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Standard text field for Deep Reps. Supports label, placeholder, and error state.
 *
 * Uses design-system colors: `surface-high` container, `border-focus` on focus,
 * `status-error` on error.
 *
 * @param value Current text value.
 * @param onValueChange Callback when text changes.
 * @param label Optional label above the field.
 * @param placeholder Optional hint text.
 * @param errorMessage If non-null, displays in error state with this message.
 * @param singleLine Whether to restrict to a single line.
 * @param modifier External modifier.
 */
@Composable
fun DeepRepsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    errorMessage: String? = null,
    singleLine: Boolean = true,
) {
    val colors = DeepRepsTheme.colors
    val radius = DeepRepsTheme.radius
    val typography = DeepRepsTheme.typography

    val isError = errorMessage != null

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = label?.let {
                { Text(text = it, style = typography.bodyMedium) }
            },
            placeholder = placeholder?.let {
                {
                    Text(
                        text = it,
                        style = typography.bodyLarge,
                        color = colors.onSurfaceTertiary,
                    )
                }
            },
            isError = isError,
            singleLine = singleLine,
            textStyle = typography.bodyLarge.copy(color = colors.onSurfacePrimary),
            shape = RoundedCornerShape(radius.sm),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.surfaceHigh,
                unfocusedContainerColor = colors.surfaceHigh,
                errorContainerColor = colors.surfaceHigh,
                focusedBorderColor = colors.borderFocus,
                unfocusedBorderColor = colors.borderSubtle,
                errorBorderColor = colors.statusError,
                focusedLabelColor = colors.accentPrimary,
                unfocusedLabelColor = colors.onSurfaceSecondary,
                errorLabelColor = colors.statusError,
                cursorColor = colors.accentPrimary,
                errorCursorColor = colors.statusError,
            ),
        )

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = typography.bodySmall,
                color = colors.statusError,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Default - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun DefaultDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        var text by remember { mutableStateOf("") }
        DeepRepsTextField(
            value = text,
            onValueChange = { text = it },
            label = "Template Name",
            placeholder = "e.g., Push Day A",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Filled - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun FilledDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        DeepRepsTextField(
            value = "Push Day A",
            onValueChange = {},
            label = "Template Name",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Error - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun ErrorDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        DeepRepsTextField(
            value = "",
            onValueChange = {},
            label = "Template Name",
            errorMessage = "Template name required",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Default - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun DefaultLightPreview() {
    DeepRepsTheme(darkTheme = false) {
        DeepRepsTextField(
            value = "Upper Body B",
            onValueChange = {},
            label = "Template Name",
            modifier = Modifier.padding(16.dp),
        )
    }
}
