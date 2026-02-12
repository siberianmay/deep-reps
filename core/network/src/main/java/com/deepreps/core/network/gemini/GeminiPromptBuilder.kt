package com.deepreps.core.network.gemini

import com.deepreps.core.domain.model.PlanRequest
import com.deepreps.core.domain.usecase.CrossGroupOverlapDetector
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Constructs the Gemini prompt from PlanRequest context.
 *
 * Follows the template specified in architecture.md Section 4.3 and
 * exercise-science.md Section 5.4 exactly.
 *
 * PROMPT_VERSION is tracked as a constant and stored with every cached plan.
 * Changes to this prompt require CSCS sign-off.
 *
 * Token budget: ~2000 input tokens (~8000 characters).
 * If training history would exceed the budget, oldest sessions are truncated first.
 */
class GeminiPromptBuilder @Inject constructor(
    private val overlapDetector: CrossGroupOverlapDetector,
) {

    fun build(request: PlanRequest): String = buildString {
        appendLine(
            "You are a certified strength & conditioning specialist." +
                " Generate a structured workout plan as JSON.",
        )
        appendLine()

        appendUserProfile(request)
        appendProgressionContext(request)
        appendExercises(request)
        appendTrainingHistory(request, remainingBudget = MAX_CHARS - length)
        appendSafetyConstraints(request)
        appendContraindications(request)
        appendCrossGroupFatigue(request)
        appendOutputFormat()
    }

    private fun StringBuilder.appendUserProfile(request: PlanRequest) {
        appendLine("## User Profile")
        appendLine("- Experience level: ${experienceLevelLabel(request.userProfile.experienceLevel)}")
        request.userProfile.bodyWeightKg?.let { appendLine("- Body weight: ${it}kg") }
        request.userProfile.age?.let { appendLine("- Age: $it") }
        request.userProfile.gender?.let { appendLine("- Gender: $it") }
        appendLine()
    }

    private fun StringBuilder.appendProgressionContext(request: PlanRequest) {
        appendLine("## Progression Context")
        appendLine("- Periodization model: ${request.periodizationModel}")
        request.performanceTrend?.let { appendLine("- Performance trend: $it") }
        request.weeksSinceDeload?.let { appendLine("- Weeks since last deload: $it") }
        if (request.deloadRecommended) {
            appendLine("- DELOAD RECOMMENDED: reduce volume by 40-60%, reduce intensity by 10-15%")
        }
        request.currentBlockPhase?.let { phase ->
            appendLine("- Current block phase: $phase (week ${request.currentBlockWeek})")
        }
        appendLine()
    }

    private fun StringBuilder.appendExercises(request: PlanRequest) {
        appendLine("## Exercises (in order)")
        request.exercises.forEachIndexed { index, exercise ->
            appendLine(
                "${index + 1}. ${exercise.name} [stable_id: ${exercise.stableId}] " +
                    "(${exercise.equipment}, ${exercise.movementType}, difficulty: ${exercise.difficulty})",
            )
        }
        appendLine()
    }

    @Suppress("LongMethod")
    private fun StringBuilder.appendTrainingHistory(request: PlanRequest, remainingBudget: Int) {
        if (request.trainingHistory.isEmpty()) {
            appendLine("## Training History")
            val level = request.userProfile.experienceLevel
            appendLine(
                "NO TRAINING HISTORY AVAILABLE. " +
                    "Use baseline tables for experience level $level.",
            )
            appendLine()
            return
        }

        appendLine("## Recent Training History (last 3-5 sessions per exercise)")

        var charsUsed = 0
        val budgetForHistory = (remainingBudget * 0.4).toInt().coerceAtLeast(500)

        for (history in request.trainingHistory) {
            val historyBlock = buildString {
                appendLine("### ${history.exerciseName}")
                // Take last 5 sessions, but may truncate if budget exceeded
                history.sessions.takeLast(MAX_SESSIONS_PER_EXERCISE).forEach { session ->
                    appendLine("  Session (${formatDate(session.date)}):")
                    session.sets.forEach { set ->
                        appendLine("    ${set.weight}kg x ${set.reps} (${set.setType})")
                    }
                }
            }

            if (charsUsed + historyBlock.length > budgetForHistory) {
                // Truncate: include fewer sessions for remaining exercises
                val abbreviated = buildString {
                    appendLine("### ${history.exerciseName}")
                    val lastSession = history.sessions.lastOrNull()
                    if (lastSession != null) {
                        appendLine("  Last session (${formatDate(lastSession.date)}):")
                        lastSession.sets.forEach { set ->
                            appendLine("    ${set.weight}kg x ${set.reps} (${set.setType})")
                        }
                    }
                }
                if (charsUsed + abbreviated.length <= budgetForHistory) {
                    append(abbreviated)
                    charsUsed += abbreviated.length
                }
                // If even abbreviated does not fit, skip this exercise history
            } else {
                append(historyBlock)
                charsUsed += historyBlock.length
            }
        }
        appendLine()
    }

    @Suppress("LongMethod", "ComplexMethod")
    private fun StringBuilder.appendSafetyConstraints(request: PlanRequest) {
        appendLine("## SAFETY CONSTRAINTS (NON-NEGOTIABLE)")
        appendLine(
            "1. Max weight increase: 10% above the last working weight for any exercise. " +
                "Never exceed 10kg absolute jump for barbells, 5kg for dumbbells.",
        )
        appendLine(
            "2. MRV ceiling: Do not exceed ${getMrvCeiling(request.userProfile.experienceLevel)} " +
                "total working sets per muscle group per session.",
        )
        appendLine(
            "3. Total session volume: Maximum 30 working sets per session, " +
                "maximum 6 working sets per exercise, maximum 12 exercises per session.",
        )
        appendLine(
            "4. Advanced exercise gating: Only include exercises with difficulty level <= " +
                "user experience level. Never include advanced exercises for beginners.",
        )
        appendLine(
            "5. Warm-up sets: Heavy barbell compounds require 3 warm-up sets " +
                "(empty bar, 50%, 75%). Moderate compounds require 2. Isolations require 1 or 0 (bodyweight).",
        )
        appendLine("6. Max RPE 9-10 exercises: At most 2 exercises per session at near-max effort.")

        request.userProfile.age?.let { age ->
            when {
                age < 18 -> appendLine(
                    "7. AGE MODIFIER (under 18): Cap intensity at 85% 1RM. No singles (1-rep sets). " +
                        "Focus on movement quality.",
                )
                age in 41..50 -> appendLine(
                    "7. AGE MODIFIER (41-50): Reduce max intensity by 2.5%. Add +15s rest between sets.",
                )
                age in 51..60 -> appendLine(
                    "7. AGE MODIFIER (51-60): Reduce max intensity by 5%. " +
                        "Add 1 extra warm-up set per compound. Increase rest by 30s. Reduce weekly volume by 10%.",
                )
                age > 60 -> appendLine(
                    "7. AGE MODIFIER (60+): Reduce max intensity by 10%. " +
                        "Add 2 extra warm-up sets. Prefer machine exercises over free weights. " +
                        "Increase rest by 45s. Reduce weekly volume by 20%.",
                )
                else -> {} // 18-40: No age modifier needed
            }
        }

        appendLine(
            "8. Weight rounding: Round all weights DOWN to nearest increment " +
                "(barbell: 2.5kg, dumbbell: 2.5kg, cable/machine: 5kg).",
        )
        appendLine(
            "9. When no training history exists, use baseline tables from exercise-science Section 4 " +
                "(body weight ratios by experience level).",
        )
        appendLine()
    }

    @Suppress("LongMethod", "ComplexMethod")
    private fun StringBuilder.appendContraindications(request: PlanRequest) {
        val contraindications = buildList {
            if (request.exercises.any { it.stableId == "lower_back_barbell_good_morning" }) {
                add("Good Morning: Max weight = 60% of squat working weight. Never appears in beginner plans.")
            }
            if (request.exercises.any { it.stableId == "shoulders_barbell_upright_row" }) {
                add(
                    "Barbell Upright Row: Pull to chest height only. Stop if shoulder pain occurs. " +
                        "Do not program above moderate weight for beginners.",
                )
            }
            if (request.exercises.any { it.stableId == "arms_ez_bar_skull_crusher" }) {
                add("Skull Crusher: Maximum 3 working sets. Recommend EZ bar over straight bar.")
            }
            if (request.exercises.any { it.stableId == "lower_back_barbell_deficit_deadlift" }) {
                val hasDeadliftRegression = request.trainingHistory
                    .filter { it.exerciseName == "Conventional Deadlift" }
                    .any { it.trend == "regressing" }
                if (hasDeadliftRegression) {
                    add(
                        "Deficit Deadlift: EXCLUDED -- conventional deadlift history shows regression. " +
                            "Substitute with conventional deadlift.",
                    )
                } else {
                    add("Deficit Deadlift: Only program if conventional deadlift history shows no regression pattern.")
                }
            }
            val hasDragonFlagOrAbWheel = request.exercises.any {
                it.stableId == "core_bodyweight_dragon_flag" ||
                    it.stableId == "core_bodyweight_ab_wheel_rollout"
            }
            if (hasDragonFlagOrAbWheel) {
                if (request.userProfile.experienceLevel == 1) {
                    add(
                        "Dragon Flag / Ab Wheel Rollout: EXCLUDED for beginners" +
                            " -- never auto-programmed at experience level 1.",
                    )
                }
            }
            if (request.exercises.any { it.stableId == "core_bodyweight_ab_wheel_rollout" }) {
                add(
                    "Ab Wheel Rollout: Maintain posterior pelvic tilt throughout." +
                        " If lower back arches, reduce range of motion.",
                )
            }
        }

        if (contraindications.isNotEmpty()) {
            appendLine("## EXERCISE-SPECIFIC SAFETY CONSTRAINTS (NON-NEGOTIABLE)")
            contraindications.forEach { appendLine("- $it") }
            appendLine()
        }
    }

    private fun StringBuilder.appendCrossGroupFatigue(request: PlanRequest) {
        val overlaps = overlapDetector.detect(request.exercises)
        if (overlaps.isNotEmpty()) {
            appendLine("## CROSS-GROUP FATIGUE WARNING")
            overlaps.forEach { overlap ->
                appendLine("- ${overlap.description}")
            }
            appendLine("Reduce isolation volume for overlapping muscles accordingly.")
            appendLine()
        }
    }

    private fun StringBuilder.appendOutputFormat() {
        appendLine("## Output Format")
        appendLine("Respond ONLY with valid JSON matching this schema:")
        appendLine(
            """
            {
              "exercise_plans": [
                {
                  "exercise_id": "<stable_id string>",
                  "warmup_sets": [{"weight": <number>, "reps": <number>, "set_number": <number>}],
                  "working_sets": [{"weight": <number>, "reps": <number>, "set_number": <number>}],
                  "rest_seconds": <number>,
                  "notes": "<brief rationale for weight/rep selection>"
                }
              ],
              "session_summary": {
                "total_working_sets": <number>,
                "estimated_duration_minutes": <number>,
                "volume_check": "<ok or warning message>"
              }
            }
            """.trimIndent(),
        )
    }

    companion object {
        const val PROMPT_VERSION: String = "v2.0"

        private const val MAX_CHARS = 8000 // ~2000 tokens at ~4 chars/token
        private const val MAX_SESSIONS_PER_EXERCISE = 5

        private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        fun formatDate(epochMillis: Long): String = dateFormatter.format(Date(epochMillis))

        fun getMrvCeiling(experienceLevel: Int): Int = when (experienceLevel) {
            1 -> 12
            2 -> 16
            3 -> 20
            else -> 12
        }

        fun experienceLevelLabel(level: Int): String = when (level) {
            1 -> "Total Beginner (0-6 months)"
            2 -> "Intermediate (6-18 months)"
            3 -> "Advanced (18+ months)"
            else -> "Unknown"
        }
    }
}
