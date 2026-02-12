package com.deepreps.core.common.dispatcher

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Abstraction over coroutine dispatchers to enable testing with [UnconfinedTestDispatcher].
 *
 * Every repository and use case that launches coroutines must inject this interface
 * rather than referencing [Dispatchers] directly. This is a non-negotiable project convention.
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}
