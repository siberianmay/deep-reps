package com.deepreps.core.domain.model

/**
 * Result of deload need detection.
 *
 * Per exercise-science.md Section 3.4, deloads are triggered proactively
 * (scheduled), reactively (regression), or by user request.
 */
enum class DeloadStatus {
    /** No deload needed. */
    NOT_NEEDED,

    /** Scheduled deload window reached (beginner: 6wk, intermediate: 4wk, advanced: 5wk). */
    PROACTIVE_RECOMMENDED,

    /** Performance regression detected (2+ consecutive sessions with failed reps). */
    REACTIVE_RECOMMENDED,

    /** User manually requested a deload. */
    USER_REQUESTED,
}
