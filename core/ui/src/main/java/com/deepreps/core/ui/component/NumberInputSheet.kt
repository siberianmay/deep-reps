package com.deepreps.core.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.deepreps.core.ui.theme.DeepRepsTheme
import java.util.Locale

/**
 * Bottom sheet with a direct numeric text field for editing weight or reps.
 *
 * The value is held as local text state inside the sheet so that changes are batched
 * until the user presses Done. Only [onConfirm] propagates the final value.
 *
 * The text field auto-focuses and selects all text on open so the user can
 * immediately start typing a replacement value.
 */
@Suppress("LongParameterList", "LongMethod", "UnusedParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberInputSheet(
    title: String,
    value: Double,
    step: Double,
    minValue: Double,
    maxValue: Double,
    isDecimal: Boolean,
    unitLabel: String = "",
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography

    val initialText = formatSheetValue(value, isDecimal)
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialText,
                selection = TextRange(0, initialText.length),
            ),
        )
    }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val displayTitle = if (unitLabel.isNotEmpty()) "$title ($unitLabel)" else title

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceLow,
        contentColor = colors.onSurfacePrimary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = displayTitle,
                style = typography.headlineMedium,
                color = colors.onSurfacePrimary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            DirectNumericInput(
                textFieldValue = textFieldValue,
                onTextFieldValueChange = { newValue ->
                    val filtered = if (isDecimal) {
                        filterDecimalInput(newValue.text)
                    } else {
                        newValue.text.filter { it.isDigit() }
                    }
                    textFieldValue = newValue.copy(text = filtered)
                },
                unitLabel = unitLabel,
                isDecimal = isDecimal,
                focusRequester = focusRequester,
                onDone = {
                    val parsed = parseAndClamp(
                        text = textFieldValue.text,
                        isDecimal = isDecimal,
                        minValue = minValue,
                        maxValue = maxValue,
                        fallback = value,
                    )
                    onConfirm(parsed)
                },
            )

            Spacer(modifier = Modifier.height(24.dp))

            DeepRepsButton(
                text = "Done",
                onClick = {
                    val parsed = parseAndClamp(
                        text = textFieldValue.text,
                        isDecimal = isDecimal,
                        minValue = minValue,
                        maxValue = maxValue,
                        fallback = value,
                    )
                    onConfirm(parsed)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
            )
        }
    }
}

/**
 * Large numeric text field with unit label, used inside [NumberInputSheet].
 */
@Composable
private fun DirectNumericInput(
    textFieldValue: TextFieldValue,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    unitLabel: String,
    isDecimal: Boolean,
    focusRequester: FocusRequester,
    onDone: () -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Spacer(modifier = Modifier.weight(1f))

        BasicTextField(
            value = textFieldValue,
            onValueChange = onTextFieldValueChange,
            modifier = Modifier
                .focusRequester(focusRequester)
                .heightIn(min = 64.dp),
            textStyle = typography.numberLarge.copy(
                color = colors.onSurfacePrimary,
                textAlign = TextAlign.Center,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number,
            ),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            singleLine = true,
            cursorBrush = SolidColor(colors.accentPrimary),
        )

        if (unitLabel.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = unitLabel,
                style = typography.headlineMedium,
                color = colors.onSurfaceTertiary,
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/** Format a value for display in the sheet text field. */
private fun formatSheetValue(value: Double, isDecimal: Boolean): String {
    return if (isDecimal) {
        if (value % 1.0 == 0.0) value.toInt().toString()
        else String.format(Locale.US, "%.1f", value)
    } else {
        value.toInt().toString()
    }
}

/** Allow only digits and at most one decimal point. */
private fun filterDecimalInput(text: String): String {
    val sb = StringBuilder()
    var hasDot = false
    for (c in text) {
        when {
            c.isDigit() -> sb.append(c)
            c == '.' && !hasDot -> {
                sb.append(c)
                hasDot = true
            }
        }
    }
    return sb.toString()
}

/** Parse text, clamp to range, snap decimals to 0.5 increments. */
@Suppress("LongParameterList")
private fun parseAndClamp(
    text: String,
    isDecimal: Boolean,
    minValue: Double,
    maxValue: Double,
    fallback: Double,
): Double {
    val parsed = text.toDoubleOrNull() ?: return fallback
    val snapped = if (!isDecimal) {
        parsed.toInt().toDouble()
    } else {
        Math.round(parsed * 2) / 2.0
    }
    return snapped.coerceIn(minValue, maxValue)
}
