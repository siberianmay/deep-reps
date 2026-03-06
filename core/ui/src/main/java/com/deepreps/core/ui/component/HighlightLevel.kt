package com.deepreps.core.ui.component

/**
 * Level of highlighting for a muscle group in the anatomy diagram.
 */
enum class HighlightLevel {
    /** Full accent color at 85% opacity — the exercise's primary target. */
    PRIMARY,

    /** Accent color at 30% opacity — secondary / synergist muscle. */
    SECONDARY,
}
