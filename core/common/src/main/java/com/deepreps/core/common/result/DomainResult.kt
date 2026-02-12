package com.deepreps.core.common.result

/**
 * Wrapper for domain layer operation results.
 *
 * Use cases return [DomainResult] instead of throwing exceptions.
 * ViewModels map [Error] variants to UI error states without try-catch.
 */
sealed interface DomainResult<out T> {

    /** Operation completed successfully with [data]. */
    data class Success<T>(val data: T) : DomainResult<T>

    /** Operation failed with a typed [error]. */
    data class Error(val error: DomainError) : DomainResult<Nothing>

    /** Operation is currently in progress. */
    data object Loading : DomainResult<Nothing>
}

/**
 * Typed domain errors. ViewModels map these to user-facing string resources.
 * No raw exception messages should reach the UI layer.
 */
sealed interface DomainError {
    data object NetworkUnavailable : DomainError
    data object NetworkTimeout : DomainError
    data object AiProviderError : DomainError
    data object DatabaseError : DomainError
    data object NotFound : DomainError
    data class Unknown(val message: String) : DomainError
}
