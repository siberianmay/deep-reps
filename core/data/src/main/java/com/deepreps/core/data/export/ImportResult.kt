package com.deepreps.core.data.export

/**
 * Result of a data import operation.
 *
 * On failure, [error] contains a user-friendly message. On success,
 * counts reflect the number of rows inserted for each category.
 */
@Suppress("ForbiddenPublicDataClass")
data class ImportResult(
    val success: Boolean,
    val sessionsImported: Int = 0,
    val templatesImported: Int = 0,
    val personalRecordsImported: Int = 0,
    val bodyWeightEntriesImported: Int = 0,
    val error: String? = null,
)
