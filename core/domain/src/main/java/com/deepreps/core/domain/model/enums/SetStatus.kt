package com.deepreps.core.domain.model.enums

/**
 * Visual and logical states of a single set during workout logging.
 *
 * - [PLANNED]: Not yet started.
 * - [IN_PROGRESS]: Currently being logged (focused).
 * - [COMPLETED]: User marked as done.
 * - [SKIPPED]: User chose to skip this set.
 */
enum class SetStatus(val value: String) {
    PLANNED("planned"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    SKIPPED("skipped");

    companion object {
        fun fromValue(value: String): SetStatus =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown set status: $value")
    }
}
