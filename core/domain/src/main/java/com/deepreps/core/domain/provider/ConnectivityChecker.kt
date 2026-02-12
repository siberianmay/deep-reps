package com.deepreps.core.domain.provider

/**
 * Abstraction over network connectivity detection.
 *
 * Used by the plan generation fallback chain to decide whether to attempt an API call.
 * Implementation in :core:data wraps Android ConnectivityManager.
 */
interface ConnectivityChecker {

    /** Returns true if the device currently has an active network connection. */
    fun isOnline(): Boolean
}
