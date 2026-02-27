package com.deepreps.feature.progress

/**
 * One-shot side effects emitted by the session detail ViewModel.
 */
sealed interface SessionDetailSideEffect {

    /** Navigate back after successful session deletion. */
    data object NavigateBackAfterDelete : SessionDetailSideEffect
}
