package com.deepreps.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.deepreps.core.domain.model.enums.MuscleGroup

/**
 * Extended color scheme that carries all Deep Reps design tokens
 * beyond what M3 [ColorScheme] provides.
 *
 * Accessed via [LocalDeepRepsColors].
 */
@Suppress("ForbiddenPublicDataClass")
@Immutable
data class DeepRepsColorScheme(
    // Surface
    val surfaceLowest: Color,
    val surfaceLow: Color,
    val surfaceMedium: Color,
    val surfaceHigh: Color,
    val surfaceHighest: Color,

    // On-surface
    val onSurfacePrimary: Color,
    val onSurfaceSecondary: Color,
    val onSurfaceTertiary: Color,

    // Accent
    val accentPrimary: Color,
    val accentPrimaryVariant: Color,
    val accentPrimaryContainer: Color,
    val accentSecondary: Color,

    // Status
    val statusSuccess: Color,
    val statusSuccessContainer: Color,
    val statusWarning: Color,
    val statusWarningContainer: Color,
    val statusError: Color,
    val statusErrorContainer: Color,

    // Border
    val borderSubtle: Color,
    val borderFocus: Color,

    // Set type
    val warmUpSet: Color,
    val workingSet: Color,

    // Overlay
    val overlayScrim: Color,

    // Muscle groups
    val muscleLegs: Color,
    val muscleLowerBack: Color,
    val muscleChest: Color,
    val muscleBack: Color,
    val muscleShoulders: Color,
    val muscleArms: Color,
    val muscleCore: Color,
) {
    /**
     * Returns the designated color for the given [muscleGroup].
     */
    fun colorForMuscleGroup(muscleGroup: MuscleGroup): Color = when (muscleGroup) {
        MuscleGroup.LEGS -> muscleLegs
        MuscleGroup.LOWER_BACK -> muscleLowerBack
        MuscleGroup.CHEST -> muscleChest
        MuscleGroup.BACK -> muscleBack
        MuscleGroup.SHOULDERS -> muscleShoulders
        MuscleGroup.ARMS -> muscleArms
        MuscleGroup.CORE -> muscleCore
    }
}

val DarkDeepRepsColorScheme: DeepRepsColorScheme = DeepRepsColorScheme(
    surfaceLowest = DarkSurfaceLowest,
    surfaceLow = DarkSurfaceLow,
    surfaceMedium = DarkSurfaceMedium,
    surfaceHigh = DarkSurfaceHigh,
    surfaceHighest = DarkSurfaceHighest,
    onSurfacePrimary = DarkOnSurfacePrimary,
    onSurfaceSecondary = DarkOnSurfaceSecondary,
    onSurfaceTertiary = DarkOnSurfaceTertiary,
    accentPrimary = DarkAccentPrimary,
    accentPrimaryVariant = DarkAccentPrimaryVariant,
    accentPrimaryContainer = DarkAccentPrimaryContainer,
    accentSecondary = DarkAccentSecondary,
    statusSuccess = DarkStatusSuccess,
    statusSuccessContainer = DarkStatusSuccessContainer,
    statusWarning = DarkStatusWarning,
    statusWarningContainer = DarkStatusWarningContainer,
    statusError = DarkStatusError,
    statusErrorContainer = DarkStatusErrorContainer,
    borderSubtle = DarkBorderSubtle,
    borderFocus = DarkBorderFocus,
    warmUpSet = DarkWarmUpSet,
    workingSet = DarkWorkingSet,
    overlayScrim = DarkOverlayScrim,
    muscleLegs = DarkMuscleLegs,
    muscleLowerBack = DarkMuscleLowerBack,
    muscleChest = DarkMuscleChest,
    muscleBack = DarkMuscleBack,
    muscleShoulders = DarkMuscleShoulders,
    muscleArms = DarkMuscleArms,
    muscleCore = DarkMuscleCore,
)

val LightDeepRepsColorScheme: DeepRepsColorScheme = DeepRepsColorScheme(
    surfaceLowest = LightSurfaceLowest,
    surfaceLow = LightSurfaceLow,
    surfaceMedium = LightSurfaceMedium,
    surfaceHigh = LightSurfaceHigh,
    surfaceHighest = LightSurfaceHighest,
    onSurfacePrimary = LightOnSurfacePrimary,
    onSurfaceSecondary = LightOnSurfaceSecondary,
    onSurfaceTertiary = LightOnSurfaceTertiary,
    accentPrimary = LightAccentPrimary,
    accentPrimaryVariant = LightAccentPrimaryVariant,
    accentPrimaryContainer = LightAccentPrimaryContainer,
    accentSecondary = LightAccentSecondary,
    statusSuccess = LightStatusSuccess,
    statusSuccessContainer = LightStatusSuccessContainer,
    statusWarning = LightStatusWarning,
    statusWarningContainer = LightStatusWarningContainer,
    statusError = LightStatusError,
    statusErrorContainer = LightStatusErrorContainer,
    borderSubtle = LightBorderSubtle,
    borderFocus = LightBorderFocus,
    warmUpSet = LightWarmUpSet,
    workingSet = LightWorkingSet,
    overlayScrim = LightOverlayScrim,
    muscleLegs = LightMuscleLegs,
    muscleLowerBack = LightMuscleLowerBack,
    muscleChest = LightMuscleChest,
    muscleBack = LightMuscleBack,
    muscleShoulders = LightMuscleShoulders,
    muscleArms = LightMuscleArms,
    muscleCore = LightMuscleCore,
)

/**
 * CompositionLocal for Deep Reps extended color scheme.
 * Access via [DeepRepsTheme.colors].
 */
val LocalDeepRepsColors: ProvidableCompositionLocal<DeepRepsColorScheme> =
    staticCompositionLocalOf { DarkDeepRepsColorScheme }
