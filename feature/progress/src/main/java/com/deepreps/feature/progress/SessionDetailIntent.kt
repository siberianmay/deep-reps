package com.deepreps.feature.progress

/**
 * User intents for the session detail screen.
 */
sealed interface SessionDetailIntent {

    /** User tapped the delete icon in the top app bar. */
    data object RequestDelete : SessionDetailIntent

    /** User confirmed deletion in the confirmation dialog. */
    data object ConfirmDelete : SessionDetailIntent

    /** User dismissed the deletion dialog. */
    data object DismissDelete : SessionDetailIntent
}
