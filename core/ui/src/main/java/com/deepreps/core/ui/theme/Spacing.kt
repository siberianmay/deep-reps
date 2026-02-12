package com.deepreps.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ---------------------------------------------------------------------------
// Spacing -- design-system.md Section 2.3 (4dp grid)
// ---------------------------------------------------------------------------

@Immutable
data class DeepRepsSpacing(
    /** 0dp - No spacing */
    val space0: Dp = 0.dp,
    /** 4dp - Tight inline spacing (icon-to-text) */
    val space1: Dp = 4.dp,
    /** 8dp - Default inline spacing, list item internal padding */
    val space2: Dp = 8.dp,
    /** 12dp - Card internal padding (compact) */
    val space3: Dp = 12.dp,
    /** 16dp - Card internal padding (standard), screen horizontal margin */
    val space4: Dp = 16.dp,
    /** 20dp - Section spacing within a card */
    val space5: Dp = 20.dp,
    /** 24dp - Between cards/sections */
    val space6: Dp = 24.dp,
    /** 32dp - Major section dividers */
    val space7: Dp = 32.dp,
    /** 40dp - Screen top/bottom safe areas */
    val space8: Dp = 40.dp,
    /** 48dp - Large visual breaks */
    val space9: Dp = 48.dp,
    /** 64dp - Hero section spacing */
    val space10: Dp = 64.dp,
)

// ---------------------------------------------------------------------------
// Border radius -- design-system.md Section 2.4
// ---------------------------------------------------------------------------

@Immutable
data class DeepRepsRadius(
    /** 0dp - Square edges (progress bar fills) */
    val none: Dp = 0.dp,
    /** 4dp - Set row containers, inline tags */
    val xs: Dp = 4.dp,
    /** 8dp - Input fields, chips */
    val sm: Dp = 8.dp,
    /** 12dp - Cards, buttons */
    val md: Dp = 12.dp,
    /** 16dp - Bottom sheets, dialogs */
    val lg: Dp = 16.dp,
    /** 24dp - FABs, pill-shaped elements */
    val xl: Dp = 24.dp,
    /** 9999dp - Circular elements (avatars, badges) */
    val full: Dp = 9999.dp,
)

// ---------------------------------------------------------------------------
// Elevation -- design-system.md Section 2.5
// M3 tonal elevation in dark theme; shadow in light theme.
// ---------------------------------------------------------------------------

@Immutable
data class DeepRepsElevation(
    /** Level 0 - Flat content on background */
    val level0: Dp = 0.dp,
    /** Level 1 - Cards at rest */
    val level1: Dp = 1.dp,
    /** Level 2 - Raised cards, FAB at rest */
    val level2: Dp = 3.dp,
    /** Level 3 - Bottom navigation, app bars */
    val level3: Dp = 6.dp,
    /** Level 4 - FAB pressed, dragged items */
    val level4: Dp = 8.dp,
    /** Level 5 - Dialogs, modals */
    val level5: Dp = 12.dp,
)

// ---------------------------------------------------------------------------
// Touch targets -- design-system.md Section 2.6
// ---------------------------------------------------------------------------

@Immutable
data class DeepRepsTouchTargets(
    /** 48dp x 48dp - Absolute minimum per M3 spec */
    val minimum: Dp = 48.dp,
    /** 56dp x 56dp - Default for workout screen interactive elements */
    val standard: Dp = 56.dp,
    /** 64dp x 64dp - Set done checkbox, weight/rep steppers during active workout */
    val large: Dp = 64.dp,
    /** 72dp width x 56dp height - Weight and rep input fields */
    val numberInputWidth: Dp = 72.dp,
    /** 56dp height for number input fields */
    val numberInputHeight: Dp = 56.dp,
)

// ---------------------------------------------------------------------------
// CompositionLocals
// ---------------------------------------------------------------------------

val LocalDeepRepsSpacing = staticCompositionLocalOf { DeepRepsSpacing() }
val LocalDeepRepsRadius = staticCompositionLocalOf { DeepRepsRadius() }
val LocalDeepRepsElevation = staticCompositionLocalOf { DeepRepsElevation() }
val LocalDeepRepsTouchTargets = staticCompositionLocalOf { DeepRepsTouchTargets() }
