package com.deepreps.core.data.export

/**
 * Abstraction over Room's `withTransaction` to enable unit testing.
 *
 * Room's `withTransaction` is a `suspend inline` extension function that cannot be
 * mocked with MockK. This interface allows tests to supply a pass-through
 * implementation while production code delegates to the real Room transaction.
 */
interface TransactionRunner {
    suspend fun runInTransaction(block: suspend () -> Unit)
}
