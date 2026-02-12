package com.deepreps.core.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Weight/reps stepper with +/- buttons and direct entry.
 *
 * Design spec: design-system.md Section 5.4.
 * - Stepper buttons: 64dp x 56dp
 * - Number input field: 72dp x 56dp
 * - Long press for rapid increment
 *
 * @param value Current numeric value.
 * @param onValueChange Callback when value changes.
 * @param step Increment/decrement amount (e.g., 2.5 for barbell weight, 1.0 for reps).
 * @param minValue Minimum allowed value (inclusive).
 * @param maxValue Maximum allowed value (inclusive).
 * @param isDecimal Whether decimal input is allowed (true for weight, false for reps).
 * @param label Accessibility label (e.g., "Weight", "Reps").
 * @param modifier External modifier.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NumberInput(
    value: Double,
    onValueChange: (Double) -> Unit,
    step: Double,
    minValue: Double,
    maxValue: Double,
    modifier: Modifier = Modifier,
    isDecimal: Boolean = true,
    label: String = "",
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val radius = DeepRepsTheme.radius

    val canDecrement = value - step >= minValue
    val canIncrement = value + step <= maxValue

    var isEditing by remember { mutableStateOf(false) }
    var editText by remember(value) {
        mutableStateOf(formatDisplayValue(value, isDecimal))
    }

    // Long-press step multiplier: jumps by 5x on long-press
    // TODO: Implement true rapid-fire with custom gesture detection (pointerInput)
    //       per design-system.md Section 5.4: hold 500ms -> 1 step/300ms accelerating to 1 step/100ms
    val longPressStep = step * 5

    Row(
        modifier = modifier.semantics {
            contentDescription = "$label, current value ${formatDisplayValue(value, isDecimal)}"
        },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // [-] Button
        Surface(
            modifier = Modifier
                .defaultMinSize(minWidth = 64.dp, minHeight = 56.dp)
                .combinedClickable(
                    enabled = canDecrement,
                    onClick = {
                        val newValue = (value - step).coerceAtLeast(minValue)
                        onValueChange(newValue)
                    },
                    onLongClick = {
                        val newValue = (value - longPressStep).coerceAtLeast(minValue)
                        onValueChange(newValue)
                    },
                ),
            shape = RoundedCornerShape(
                topStart = radius.sm,
                bottomStart = radius.sm,
                topEnd = 0.dp,
                bottomEnd = 0.dp,
            ),
            color = colors.surfaceHigh,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "\u2212", // minus sign
                    style = typography.headlineMedium,
                    color = if (canDecrement) {
                        colors.onSurfacePrimary
                    } else {
                        colors.onSurfaceTertiary.copy(alpha = 0.4f)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }

        // Value display / direct entry
        Surface(
            modifier = Modifier.defaultMinSize(minWidth = 72.dp, minHeight = 56.dp),
            color = colors.surfaceHigh,
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isEditing) {
                    BasicTextField(
                        value = editText,
                        onValueChange = { input ->
                            // Filter invalid characters
                            val filtered = if (isDecimal) {
                                input.filter { it.isDigit() || it == '.' }
                            } else {
                                input.filter { it.isDigit() }
                            }
                            editText = filtered
                        },
                        modifier = Modifier
                            .widthIn(min = 72.dp)
                            .padding(horizontal = 8.dp, vertical = 12.dp)
                            .onFocusChanged { state ->
                                if (!state.isFocused && isEditing) {
                                    isEditing = false
                                    commitEditValue(
                                        editText, isDecimal, minValue, maxValue, value, onValueChange,
                                    )
                                }
                            },
                        textStyle = typography.numberMedium.copy(
                            color = colors.onSurfacePrimary,
                            textAlign = TextAlign.Center,
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (isDecimal) {
                                KeyboardType.Decimal
                            } else {
                                KeyboardType.Number
                            },
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                isEditing = false
                                commitEditValue(
                                    editText, isDecimal, minValue, maxValue, value, onValueChange,
                                )
                            },
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(colors.accentPrimary),
                    )
                } else {
                    Text(
                        text = formatDisplayValue(value, isDecimal),
                        style = typography.numberMedium,
                        color = colors.onSurfacePrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 72.dp)
                            .combinedClickable(
                                onClick = {
                                    isEditing = true
                                    editText = formatDisplayValue(value, isDecimal)
                                },
                                onLongClick = {
                                    isEditing = true
                                    editText = formatDisplayValue(value, isDecimal)
                                },
                            )
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                    )
                }
            }
        }

        // [+] Button
        Surface(
            modifier = Modifier
                .defaultMinSize(minWidth = 64.dp, minHeight = 56.dp)
                .combinedClickable(
                    enabled = canIncrement,
                    onClick = {
                        val newValue = (value + step).coerceAtMost(maxValue)
                        onValueChange(newValue)
                    },
                    onLongClick = {
                        val newValue = (value + longPressStep).coerceAtMost(maxValue)
                        onValueChange(newValue)
                    },
                ),
            shape = RoundedCornerShape(
                topStart = 0.dp,
                bottomStart = 0.dp,
                topEnd = radius.sm,
                bottomEnd = radius.sm,
            ),
            color = colors.surfaceHigh,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "+",
                    style = typography.headlineMedium,
                    color = if (canIncrement) {
                        colors.onSurfacePrimary
                    } else {
                        colors.onSurfaceTertiary.copy(alpha = 0.4f)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun formatDisplayValue(value: Double, isDecimal: Boolean): String {
    return if (isDecimal) {
        if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            // Show up to 1 decimal place
            "%.1f".format(value)
        }
    } else {
        value.toInt().toString()
    }
}

private fun commitEditValue(
    text: String,
    isDecimal: Boolean,
    minValue: Double,
    maxValue: Double,
    fallback: Double,
    onValueChange: (Double) -> Unit,
) {
    val parsed = text.toDoubleOrNull()
    if (parsed == null || parsed < minValue || parsed > maxValue) {
        // Revert to fallback (previous value)
        onValueChange(fallback)
        return
    }
    val rounded = if (!isDecimal) {
        parsed.toInt().toDouble()
    } else {
        // Snap to 0.5kg increments for weight
        (Math.round(parsed * 2) / 2.0)
    }
    onValueChange(rounded.coerceIn(minValue, maxValue))
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Weight Input - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun WeightInputDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        var value by remember { mutableStateOf(80.0) }
        NumberInput(
            value = value,
            onValueChange = { value = it },
            step = 2.5,
            minValue = 0.5,
            maxValue = 500.0,
            isDecimal = true,
            label = "Weight",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Rep Input - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun RepInputDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        var value by remember { mutableStateOf(8.0) }
        NumberInput(
            value = value,
            onValueChange = { value = it },
            step = 1.0,
            minValue = 1.0,
            maxValue = 100.0,
            isDecimal = false,
            label = "Reps",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Min Value - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun MinValueDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        NumberInput(
            value = 0.5,
            onValueChange = {},
            step = 2.5,
            minValue = 0.5,
            maxValue = 500.0,
            isDecimal = true,
            label = "Weight at minimum",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Weight Input - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun WeightInputLightPreview() {
    DeepRepsTheme(darkTheme = false) {
        NumberInput(
            value = 60.0,
            onValueChange = {},
            step = 5.0,
            minValue = 0.5,
            maxValue = 500.0,
            isDecimal = true,
            label = "Weight",
            modifier = Modifier.padding(16.dp),
        )
    }
}
