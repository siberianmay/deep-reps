package com.deepreps.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.deepreps.core.domain.model.enums.WeightUnit
import com.deepreps.core.ui.component.DeepRepsButton
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Screen 3: Unit Preference + Optional Profile Fields.
 *
 * Design spec: design-system.md Section 4.1, Screen 3.
 * - Title: "Preferred weight unit"
 * - Two large toggle buttons: kg and lbs
 * - Optional profile fields below (collapsible section)
 * - [Start Training] button at bottom
 *
 * All profile fields are optional. The button is always enabled.
 */
@Suppress("LongMethod")
@Composable
internal fun UnitAndProfileStepScreen(
    state: OnboardingUiState,
    onIntent: (OnboardingIntent) -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.space4),
    ) {
        Spacer(modifier = Modifier.height(spacing.space7))

        Text(
            text = "Preferred weight unit",
            style = typography.displaySmall,
            color = colors.onSurfacePrimary,
        )

        Spacer(modifier = Modifier.height(spacing.space6))

        // Unit toggle buttons
        UnitToggleRow(
            selectedUnit = state.weightUnit,
            onUnitSelected = { onIntent(OnboardingIntent.SetWeightUnit(it)) },
        )

        Spacer(modifier = Modifier.height(spacing.space7))

        HorizontalDivider(color = colors.borderSubtle, thickness = 1.dp)

        Spacer(modifier = Modifier.height(spacing.space6))

        // Optional profile section
        Text(
            text = "Optional: improve AI accuracy",
            style = typography.headlineSmall,
            color = colors.onSurfaceSecondary,
        )

        Spacer(modifier = Modifier.height(spacing.space2))

        Text(
            text = "All fields are optional. You can add these later in Settings.",
            style = typography.bodySmall,
            color = colors.onSurfaceTertiary,
        )

        Spacer(modifier = Modifier.height(spacing.space4))

        // Age
        ProfileNumberField(
            value = state.age,
            onValueChange = { onIntent(OnboardingIntent.SetAge(it)) },
            label = "Age",
            placeholder = "e.g., 28",
            isDecimal = false,
        )

        Spacer(modifier = Modifier.height(spacing.space4))

        // Body weight
        ProfileNumberField(
            value = state.bodyWeightKg,
            onValueChange = { onIntent(OnboardingIntent.SetBodyWeightKg(it)) },
            label = "Body weight (${state.weightUnit.value})",
            placeholder = if (state.weightUnit == WeightUnit.KG) "e.g., 75" else "e.g., 165",
            isDecimal = true,
        )

        Spacer(modifier = Modifier.height(spacing.space4))

        // Height
        ProfileNumberField(
            value = state.heightCm,
            onValueChange = { onIntent(OnboardingIntent.SetHeightCm(it)) },
            label = "Height (cm)",
            placeholder = "e.g., 175",
            isDecimal = true,
        )

        Spacer(modifier = Modifier.height(spacing.space4))

        // Gender
        Text(
            text = "Gender",
            style = typography.bodyMedium,
            color = colors.onSurfaceSecondary,
        )

        Spacer(modifier = Modifier.height(spacing.space2))

        GenderSelector(
            selected = state.genderDisplayOption,
            onSelected = { onIntent(OnboardingIntent.SetGender(it)) },
        )

        Spacer(modifier = Modifier.height(spacing.space7))

        DeepRepsButton(
            text = "Start Training",
            onClick = { onIntent(OnboardingIntent.Complete) },
            enabled = state.experienceLevel != null && !state.isCompleting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.space8),
        )
    }
}

/**
 * Two large toggle buttons for kg/lbs selection.
 *
 * Per design spec: each (screen width - 48dp) / 2 wide, 72dp tall.
 * Selected: accent-primary fill, white text. Unselected: surface-high, on-surface-secondary.
 */
@Composable
private fun UnitToggleRow(
    selectedUnit: WeightUnit,
    onUnitSelected: (WeightUnit) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        UnitToggleCard(
            label = "kg",
            isSelected = selectedUnit == WeightUnit.KG,
            onClick = { onUnitSelected(WeightUnit.KG) },
            modifier = Modifier.weight(1f),
        )

        UnitToggleCard(
            label = "lbs",
            isSelected = selectedUnit == WeightUnit.LBS,
            onClick = { onUnitSelected(WeightUnit.LBS) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun UnitToggleCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val radius = DeepRepsTheme.radius

    Card(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(radius.md),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colors.accentPrimary else colors.surfaceHigh,
        ),
        border = if (isSelected) null else BorderStroke(1.dp, colors.borderSubtle),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = label,
                style = typography.headlineLarge,
                color = if (isSelected) {
                    colors.onSurfacePrimary
                } else {
                    colors.onSurfaceSecondary
                },
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Number input field for optional profile data.
 */
@Composable
private fun ProfileNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isDecimal: Boolean,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val radius = DeepRepsTheme.radius

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = label, style = typography.bodyMedium) },
        placeholder = {
            Text(
                text = placeholder,
                style = typography.bodyLarge,
                color = colors.onSurfaceTertiary,
            )
        },
        singleLine = true,
        textStyle = typography.bodyLarge.copy(color = colors.onSurfacePrimary),
        shape = RoundedCornerShape(radius.sm),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surfaceHigh,
            unfocusedContainerColor = colors.surfaceHigh,
            focusedBorderColor = colors.borderFocus,
            unfocusedBorderColor = colors.borderSubtle,
            focusedLabelColor = colors.accentPrimary,
            unfocusedLabelColor = colors.onSurfaceSecondary,
            cursorColor = colors.accentPrimary,
        ),
    )
}

/**
 * Segmented control for gender selection.
 *
 * Options: Male / Female / Prefer not to say (per design spec).
 */
@Composable
private fun GenderSelector(
    selected: GenderDisplayOption,
    onSelected: (GenderDisplayOption) -> Unit,
) {
    val options = listOf(
        GenderDisplayOption.MALE to "Male",
        GenderDisplayOption.FEMALE to "Female",
        GenderDisplayOption.PREFER_NOT_TO_SAY to "Prefer not to say",
    )

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (option, label) ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size,
                ),
                onClick = { onSelected(option) },
                selected = selected == option,
                label = {
                    Text(
                        text = label,
                        style = DeepRepsTheme.typography.labelMedium,
                        maxLines = 1,
                    )
                },
            )
        }
    }
}
