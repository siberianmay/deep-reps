package com.deepreps.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------
// Font family
// Design system specifies Inter (variable weight) for tabular numerals.
// Placeholder: Add Inter .ttf files to core/ui/src/main/res/font/ and construct
//       a FontFamily from Font(R.font.inter_regular, ...) etc.
//       Until then, FontFamily.Default (Roboto on most devices) is used.
// ---------------------------------------------------------------------------

/**
 * Font family for Deep Reps. Defaults to system sans-serif (Roboto)
 * until Inter font resources are bundled.
 */
val InterFontFamily: FontFamily = FontFamily.Default

// ---------------------------------------------------------------------------
// Extended typography tokens -- design-system.md Section 2.2
// ---------------------------------------------------------------------------

/**
 * All 15 typography tokens from the design system.
 * The three `number-*` tokens are custom (not part of M3 Typography)
 * and are accessed via [DeepRepsTheme.typography].
 */
@Suppress("ForbiddenPublicDataClass")
@Immutable
data class DeepRepsTypography(
    val displayLarge: TextStyle,
    val displayMedium: TextStyle,
    val displaySmall: TextStyle,
    val headlineLarge: TextStyle,
    val headlineMedium: TextStyle,
    val headlineSmall: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val labelLarge: TextStyle,
    val labelMedium: TextStyle,
    val labelSmall: TextStyle,
    val numberLarge: TextStyle,
    val numberMedium: TextStyle,
    val numberSmall: TextStyle,
)

/** Canonical Deep Reps typography scale. */
val DeepRepsTypographyTokens: DeepRepsTypography = DeepRepsTypography(
    displayLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.25).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
    ),
    numberLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 52.sp,
        letterSpacing = (-1.0).sp,
    ),
    numberMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    numberSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
)

/**
 * Maps the Deep Reps design tokens onto the M3 [Typography] object
 * used by [MaterialTheme]. Custom tokens (number-*) are not representable
 * in M3 Typography and live in [DeepRepsTypography] instead.
 */
internal fun deepRepsM3Typography(): Typography = Typography(
    displayLarge = DeepRepsTypographyTokens.displayLarge,
    displayMedium = DeepRepsTypographyTokens.displayMedium,
    displaySmall = DeepRepsTypographyTokens.displaySmall,
    headlineLarge = DeepRepsTypographyTokens.headlineLarge,
    headlineMedium = DeepRepsTypographyTokens.headlineMedium,
    headlineSmall = DeepRepsTypographyTokens.headlineSmall,
    bodyLarge = DeepRepsTypographyTokens.bodyLarge,
    bodyMedium = DeepRepsTypographyTokens.bodyMedium,
    bodySmall = DeepRepsTypographyTokens.bodySmall,
    labelLarge = DeepRepsTypographyTokens.labelLarge,
    labelMedium = DeepRepsTypographyTokens.labelMedium,
    labelSmall = DeepRepsTypographyTokens.labelSmall,
    // M3 title slots -- mapped to closest design-system tokens
    titleLarge = DeepRepsTypographyTokens.headlineLarge,
    titleMedium = DeepRepsTypographyTokens.headlineMedium,
    titleSmall = DeepRepsTypographyTokens.headlineSmall,
)

val LocalDeepRepsTypography: ProvidableCompositionLocal<DeepRepsTypography> =
    staticCompositionLocalOf { DeepRepsTypographyTokens }
