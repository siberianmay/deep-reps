package com.deepreps.core.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Bottom sheet wrapper around [NumberInput] for editing weight or reps.
 *
 * The value is held as local state inside the sheet so that changes are batched
 * until the user presses Done. Only [onConfirm] propagates the final value.
 */
@Suppress("LongParameterList", "LongMethod")
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
    var localValue by remember { mutableDoubleStateOf(value) }

    val displayTitle = if (unitLabel.isNotEmpty()) "$title ($unitLabel)" else title

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepRepsTheme.colors.surfaceLow,
        contentColor = DeepRepsTheme.colors.onSurfacePrimary,
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
                style = DeepRepsTheme.typography.headlineMedium,
                color = DeepRepsTheme.colors.onSurfacePrimary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            NumberInput(
                value = localValue,
                onValueChange = { localValue = it },
                step = step,
                minValue = minValue,
                maxValue = maxValue,
                isDecimal = isDecimal,
                label = title,
            )

            Spacer(modifier = Modifier.height(24.dp))

            DeepRepsButton(
                text = "Done",
                onClick = { onConfirm(localValue) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
            )
        }
    }
}
