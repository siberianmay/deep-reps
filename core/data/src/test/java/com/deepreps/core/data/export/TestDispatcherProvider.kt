package com.deepreps.core.data.export

import com.deepreps.core.common.dispatcher.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Test-only [DispatcherProvider] that routes all dispatchers to the given [dispatcher].
 */
internal class TestDispatcherProvider(
    private val dispatcher: CoroutineDispatcher,
) : DispatcherProvider {
    override val main: CoroutineDispatcher get() = dispatcher
    override val io: CoroutineDispatcher get() = dispatcher
    override val default: CoroutineDispatcher get() = dispatcher
    override val unconfined: CoroutineDispatcher get() = dispatcher
}
