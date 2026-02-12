package com.deepreps.core.network.gemini

import com.deepreps.core.domain.model.ExerciseForPlan
import com.deepreps.core.domain.model.ExercisePlan
import com.deepreps.core.domain.model.GeneratedPlan
import com.deepreps.core.domain.model.PlannedSet
import com.deepreps.core.domain.model.enums.SetType
import com.deepreps.core.domain.provider.AiPlanException
import com.deepreps.core.network.gemini.model.GeminiExercisePlanDto
import com.deepreps.core.network.gemini.model.GeminiPlanDto
import com.deepreps.core.network.gemini.model.GeminiPlannedSetDto
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Parses the Gemini API JSON response text into a [GeneratedPlan] domain model.
 *
 * Handles:
 * - Valid JSON matching exercise-science.md Section 5.3 schema
 * - Malformed JSON (throws [AiPlanException] with descriptive message)
 * - Missing required fields (exercise_id is required; other fields use defaults)
 * - Mapping from AI stable IDs back to Room PKs via the exercise lookup map
 */
class GeminiResponseParser @Inject constructor(
    private val json: Json,
) {

    /**
     * Parses the raw JSON text from Gemini into a [GeneratedPlan].
     *
     * @param responseText the raw JSON string from Gemini's response
     * @param exercises the exercise list from the request, used to map stable IDs to Room PKs
     * @throws AiPlanException if the JSON is unparseable or missing required fields
     */
    fun parse(responseText: String, exercises: List<ExerciseForPlan>): GeneratedPlan {
        val exerciseMap = exercises.associateBy { it.stableId }

        val planDto = try {
            json.decodeFromString(GeminiPlanDto.serializer(), responseText.trim())
        } catch (e: Exception) {
            throw AiPlanException("Failed to parse Gemini response as JSON: ${e.message}", e)
        }

        if (planDto.exercisePlans.isEmpty()) {
            throw AiPlanException("Gemini returned an empty exercise plan list")
        }

        val exercisePlans = planDto.exercisePlans.mapNotNull { dto ->
            mapExercisePlan(dto, exerciseMap)
        }

        if (exercisePlans.isEmpty()) {
            throw AiPlanException(
                "None of the exercises in the Gemini response matched the requested exercises",
            )
        }

        return GeneratedPlan(exercises = exercisePlans)
    }

    private fun mapExercisePlan(
        dto: GeminiExercisePlanDto,
        exerciseMap: Map<String, ExerciseForPlan>,
    ): ExercisePlan? {
        val stableId = dto.exerciseId
        if (stableId.isBlank()) return null

        val exerciseInfo = exerciseMap[stableId]
            ?: return null // Gemini returned an exercise we did not request; skip it

        val warmupSets = dto.warmupSets.mapIndexed { index, set ->
            mapPlannedSet(set, SetType.WARMUP, index)
        }

        val workingSets = dto.workingSets.mapIndexed { index, set ->
            mapPlannedSet(set, SetType.WORKING, index)
        }

        val allSets = warmupSets + workingSets

        return ExercisePlan(
            exerciseId = exerciseInfo.exerciseId,
            stableId = stableId,
            exerciseName = exerciseInfo.name,
            sets = allSets,
            restSeconds = dto.restSeconds.coerceIn(MIN_REST_SECONDS, MAX_REST_SECONDS),
            notes = dto.notes,
        )
    }

    private fun mapPlannedSet(
        dto: GeminiPlannedSetDto,
        setType: SetType,
        fallbackIndex: Int,
    ): PlannedSet = PlannedSet(
        setType = setType,
        weight = dto.weight.coerceAtLeast(0.0),
        reps = dto.reps.coerceIn(MIN_REPS, MAX_REPS),
        restSeconds = DEFAULT_REST_SECONDS,
    )

    companion object {
        private const val MIN_REST_SECONDS = 30
        private const val MAX_REST_SECONDS = 300
        private const val DEFAULT_REST_SECONDS = 90
        private const val MIN_REPS = 1
        private const val MAX_REPS = 50
    }
}
