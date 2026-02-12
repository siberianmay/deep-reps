package com.deepreps.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ---------------------------------------------------------------------------
// M3 color scheme mappings
// ---------------------------------------------------------------------------

private val DarkM3ColorScheme = darkColorScheme(
    primary = DarkAccentPrimary,
    onPrimary = DarkOnSurfacePrimary,
    primaryContainer = DarkAccentPrimaryContainer,
    onPrimaryContainer = DarkOnSurfacePrimary,
    secondary = DarkAccentSecondary,
    onSecondary = DarkOnSurfacePrimary,
    background = DarkSurfaceLowest,
    onBackground = DarkOnSurfacePrimary,
    surface = DarkSurfaceLow,
    onSurface = DarkOnSurfacePrimary,
    surfaceVariant = DarkSurfaceMedium,
    onSurfaceVariant = DarkOnSurfaceSecondary,
    error = DarkStatusError,
    onError = DarkOnSurfacePrimary,
    errorContainer = DarkStatusErrorContainer,
    onErrorContainer = DarkOnSurfacePrimary,
    outline = DarkBorderSubtle,
    outlineVariant = DarkBorderSubtle,
    inverseSurface = LightSurfaceLow,
    inverseOnSurface = LightOnSurfacePrimary,
    inversePrimary = LightAccentPrimary,
    scrim = DarkOverlayScrim,
    surfaceTint = DarkAccentPrimary,
)

private val LightM3ColorScheme = lightColorScheme(
    primary = LightAccentPrimary,
    onPrimary = LightSurfaceLowest,
    primaryContainer = LightAccentPrimaryContainer,
    onPrimaryContainer = LightOnSurfacePrimary,
    secondary = LightAccentSecondary,
    onSecondary = LightSurfaceLowest,
    background = LightSurfaceLowest,
    onBackground = LightOnSurfacePrimary,
    surface = LightSurfaceLow,
    onSurface = LightOnSurfacePrimary,
    surfaceVariant = LightSurfaceMedium,
    onSurfaceVariant = LightOnSurfaceSecondary,
    error = LightStatusError,
    onError = LightSurfaceLowest,
    errorContainer = LightStatusErrorContainer,
    onErrorContainer = LightOnSurfacePrimary,
    outline = LightBorderSubtle,
    outlineVariant = LightBorderSubtle,
    inverseSurface = DarkSurfaceLow,
    inverseOnSurface = DarkOnSurfacePrimary,
    inversePrimary = DarkAccentPrimary,
    scrim = LightOverlayScrim,
    surfaceTint = LightAccentPrimary,
)

// ---------------------------------------------------------------------------
// Theme composable
// ---------------------------------------------------------------------------

/**
 * Deep Reps application theme. Dark theme is the primary surface.
 *
 * Provides:
 * - M3 [MaterialTheme] with mapped color scheme and typography
 * - [DeepRepsColorScheme] via [LocalDeepRepsColors] (full design token palette)
 * - [DeepRepsTypography] via [LocalDeepRepsTypography] (including number-* tokens)
 * - [DeepRepsSpacing], [DeepRepsRadius], [DeepRepsElevation], [DeepRepsTouchTargets]
 *
 * Access via [DeepRepsTheme] object helpers.
 */
@Composable
fun DeepRepsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val deepRepsColors = if (darkTheme) DarkDeepRepsColorScheme else LightDeepRepsColorScheme
    val m3ColorScheme = if (darkTheme) DarkM3ColorScheme else LightM3ColorScheme

    // Tint the status bar to match the surface
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = deepRepsColors.surfaceLowest.toArgb()
            window.navigationBarColor = deepRepsColors.surfaceLow.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalDeepRepsColors provides deepRepsColors,
        LocalDeepRepsTypography provides DeepRepsTypographyTokens,
        LocalDeepRepsSpacing provides DeepRepsSpacing(),
        LocalDeepRepsRadius provides DeepRepsRadius(),
        LocalDeepRepsElevation provides DeepRepsElevation(),
        LocalDeepRepsTouchTargets provides DeepRepsTouchTargets(),
    ) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            typography = deepRepsM3Typography(),
            content = content,
        )
    }
}

/**
 * Accessor object for Deep Reps design tokens within @Composable scope.
 *
 * Usage:
 * ```
 * val colors = DeepRepsTheme.colors
 * val spacing = DeepRepsTheme.spacing
 * ```
 */
object DeepRepsTheme {
    val colors: DeepRepsColorScheme
        @Composable get() = LocalDeepRepsColors.current

    val typography: DeepRepsTypography
        @Composable get() = LocalDeepRepsTypography.current

    val spacing: DeepRepsSpacing
        @Composable get() = LocalDeepRepsSpacing.current

    val radius: DeepRepsRadius
        @Composable get() = LocalDeepRepsRadius.current

    val elevation: DeepRepsElevation
        @Composable get() = LocalDeepRepsElevation.current

    val touchTargets: DeepRepsTouchTargets
        @Composable get() = LocalDeepRepsTouchTargets.current
}
