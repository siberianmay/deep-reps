package com.deepreps.core.network.gemini.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for the JSON structure returned by Gemini in the plan generation response.
 *
 * Matches exercise-science.md Section 5.3 schema:
 * ```json
 * {
 *   "exercise_plans": [...],
 *   "session_summary": {...}
 * }
 * ```
 */
@Serializable
data class GeminiPlanDto(
    @SerialName("exercise_plans")
    val exercisePlans: List<GeminiExercisePlanDto> = emptyList(),
    @SerialName("session_summary")
    val sessionSummary: GeminiSessionSummaryDto? = null,
)

@Serializable
data class GeminiExercisePlanDto(
    @SerialName("exercise_id")
    val exerciseId: String = "",
    @SerialName("warmup_sets")
    val warmupSets: List<GeminiPlannedSetDto> = emptyList(),
    @SerialName("working_sets")
    val workingSets: List<GeminiPlannedSetDto> = emptyList(),
    @SerialName("rest_seconds")
    val restSeconds: Int = 90,
    val notes: String? = null,
)

@Serializable
data class GeminiPlannedSetDto(
    val weight: Double = 0.0,
    val reps: Int = 0,
    @SerialName("set_number")
    val setNumber: Int = 0,
)

@Serializable
data class GeminiSessionSummaryDto(
    @SerialName("total_working_sets")
    val totalWorkingSets: Int = 0,
    @SerialName("estimated_duration_minutes")
    val estimatedDurationMinutes: Int = 0,
    @SerialName("volume_check")
    val volumeCheck: String = "ok",
)
