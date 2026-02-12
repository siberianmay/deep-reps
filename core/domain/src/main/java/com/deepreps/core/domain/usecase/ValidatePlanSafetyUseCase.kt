package com.deepreps.core.domain.usecase

import com.deepreps.core.domain.model.ExerciseForPlan
import com.deepreps.core.domain.model.ExerciseHistory
import com.deepreps.core.domain.model.ExercisePlan
import com.deepreps.core.domain.model.GeneratedPlan
import com.deepreps.core.domain.model.PlanRequest
import com.deepreps.core.domain.model.SafetyViolation
import com.deepreps.core.domain.model.SafetyViolationType
import com.deepreps.core.domain.model.ViolationSeverity
import com.deepreps.core.domain.model.enums.SetType
import java.util.Locale
import javax.inject.Inject

/**
 * Validates a generated plan against safety guardrails.
 *
 * Per exercise-science.md Section 8:
 * 1. Weight jump check: no more than 10% increase from last session (Section 8.1)
 * 2. Volume ceiling: do not exceed MRV per muscle group (Section 8.2)
 * 3. Age modifier: reduce intensity for 41-50 (2.5%), 51-60 (5%), 60+ (10%) (Section 8.6)
 * 4. Difficulty gating: no advanced exercises for beginners (Section 8.4)
 * 5. Minimum rest between sets per exercise type (Section 8.8)
 *
 * Returns a list of safety violations (warnings, not hard blocks).
 * The user sees these on the plan review screen.
 */
class ValidatePlanSafetyUseCase @Inject constructor() {

    fun validate(plan: GeneratedPlan, request: PlanRequest): List<SafetyViolation> {
        val violations = mutableListOf<SafetyViolation>()

        val exerciseMap = request.exercises.associateBy { it.stableId }
        val historyMap = request.trainingHistory.associateBy { it.exerciseId }

        plan.exercises.forEach { exercisePlan ->
            val exerciseInfo = exerciseMap[exercisePlan.stableId]
            val history = historyMap[exercisePlan.exerciseId]

            if (exerciseInfo != null) {
                violations.addAll(
                    checkWeightJump(exercisePlan, exerciseInfo, history),
                )
                violations.addAll(
                    checkDifficultyGating(exercisePlan, exerciseInfo, request.userProfile.experienceLevel),
                )
                violations.addAll(
                    checkRestDuration(exercisePlan, exerciseInfo, request.userProfile.experienceLevel),
                )
            }

            violations.addAll(
                checkAgeIntensity(exercisePlan, request.userProfile.age, historyMap[exercisePlan.exerciseId]),
            )
        }

        violations.addAll(checkVolumeCeilings(plan, request))

        return violations
    }

    /**
     * Per Section 8.1: max 10% relative increase from last working weight.
     * Absolute caps: barbell compound 10kg, dumbbell compound 5kg, machine 10kg, isolation 5kg.
     */
    @Suppress("ReturnCount")
    private fun checkWeightJump(
        exercisePlan: ExercisePlan,
        exerciseInfo: ExerciseForPlan,
        history: ExerciseHistory?,
    ): List<SafetyViolation> {
        if (history == null || history.sessions.isEmpty()) return emptyList()

        val lastSession = history.sessions.lastOrNull() ?: return emptyList()
        val lastWorkingSets = lastSession.sets.filter { it.setType == "working" }
        if (lastWorkingSets.isEmpty()) return emptyList()

        val lastMaxWeight = lastWorkingSets.maxOf { it.weight }
        if (lastMaxWeight <= 0.0) return emptyList()

        val violations = mutableListOf<SafetyViolation>()
        val plannedWorkingSets = exercisePlan.sets.filter { it.setType == SetType.WORKING }
        val plannedMaxWeight = plannedWorkingSets.maxOfOrNull { it.weight } ?: return emptyList()

        val relativeJump = (plannedMaxWeight - lastMaxWeight) / lastMaxWeight
        val absoluteJump = plannedMaxWeight - lastMaxWeight

        val maxAbsoluteJump = getMaxAbsoluteJump(exerciseInfo)
        val maxRelativeJump = getMaxRelativeJump(exerciseInfo)

        if (relativeJump > maxRelativeJump || absoluteJump > maxAbsoluteJump) {
            violations.add(
                SafetyViolation(
                    type = SafetyViolationType.WEIGHT_JUMP_EXCEEDED,
                    exerciseStableId = exercisePlan.stableId,
                    message = "${exercisePlan.exerciseName}: weight jump from ${lastMaxWeight}kg to " +
                        "${plannedMaxWeight}kg exceeds safety limits " +
                        "(${String.format(Locale.US, "%.1f", relativeJump * 100)}% increase, " +
                        "${String.format(Locale.US, "%.1f", absoluteJump)}kg absolute)",
                    severity = ViolationSeverity.HIGH,
                ),
            )
        }

        return violations
    }

    private fun getMaxAbsoluteJump(exercise: ExerciseForPlan): Double = when {
        exercise.equipment in setOf("barbell", "ez_bar", "trap_bar") && exercise.movementType == "compound" -> 10.0
        exercise.equipment == "dumbbell" && exercise.movementType == "compound" -> 5.0
        exercise.equipment == "machine" && exercise.movementType == "compound" -> 10.0
        else -> 5.0 // isolation
    }

    private fun getMaxRelativeJump(exercise: ExerciseForPlan): Double = when {
        exercise.movementType == "compound" && exercise.equipment in setOf("machine") -> 0.15
        exercise.movementType == "isolation" -> 0.15
        else -> 0.10 // barbell, dumbbell, etc. compounds
    }

    /**
     * Per Section 8.2: volume ceilings per session.
     * Hard maximums: 30 total working sets, 16 per muscle group, 6 per exercise, 12 exercises.
     */
    @Suppress("LongMethod")
    private fun checkVolumeCeilings(plan: GeneratedPlan, request: PlanRequest): List<SafetyViolation> {
        val violations = mutableListOf<SafetyViolation>()

        val totalWorkingSets = plan.exercises.sumOf { ep ->
            ep.sets.count { it.setType == SetType.WORKING }
        }

        if (totalWorkingSets > MAX_TOTAL_WORKING_SETS) {
            violations.add(
                SafetyViolation(
                    type = SafetyViolationType.VOLUME_CEILING_EXCEEDED,
                    exerciseStableId = null,
                    message = "Total working sets ($totalWorkingSets) exceeds " +
                        "the hard maximum of $MAX_TOTAL_WORKING_SETS",
                    severity = ViolationSeverity.HIGH,
                ),
            )
        }

        if (plan.exercises.size > MAX_EXERCISES_PER_SESSION) {
            violations.add(
                SafetyViolation(
                    type = SafetyViolationType.VOLUME_CEILING_EXCEEDED,
                    exerciseStableId = null,
                    message = "Exercise count (${plan.exercises.size}) exceeds " +
                        "the hard maximum of $MAX_EXERCISES_PER_SESSION",
                    severity = ViolationSeverity.WARNING,
                ),
            )
        }

        // Per-exercise set limits
        plan.exercises.forEach { ep ->
            val workingSets = ep.sets.count { it.setType == SetType.WORKING }
            if (workingSets > MAX_WORKING_SETS_PER_EXERCISE) {
                violations.add(
                    SafetyViolation(
                        type = SafetyViolationType.VOLUME_CEILING_EXCEEDED,
                        exerciseStableId = ep.stableId,
                        message = "${ep.exerciseName}: $workingSets working sets exceeds " +
                            "the hard maximum of $MAX_WORKING_SETS_PER_EXERCISE per exercise",
                        severity = ViolationSeverity.WARNING,
                    ),
                )
            }
        }

        // Per-muscle-group set limits
        val exerciseMap = request.exercises.associateBy { it.stableId }
        val setsPerGroup = mutableMapOf<String, Int>()
        plan.exercises.forEach { ep ->
            val group = exerciseMap[ep.stableId]?.primaryGroup ?: return@forEach
            val workingSets = ep.sets.count { it.setType == SetType.WORKING }
            setsPerGroup[group] = (setsPerGroup[group] ?: 0) + workingSets
        }

        val mrvCeiling = getMrvCeiling(request.userProfile.experienceLevel)
        setsPerGroup.forEach { (group, sets) ->
            if (sets > MAX_WORKING_SETS_PER_GROUP) {
                violations.add(
                    SafetyViolation(
                        type = SafetyViolationType.VOLUME_CEILING_EXCEEDED,
                        exerciseStableId = null,
                        message = "$group: $sets working sets exceeds the hard maximum of " +
                            "$MAX_WORKING_SETS_PER_GROUP per muscle group per session",
                        severity = ViolationSeverity.HIGH,
                    ),
                )
            } else if (sets > mrvCeiling) {
                violations.add(
                    SafetyViolation(
                        type = SafetyViolationType.VOLUME_CEILING_EXCEEDED,
                        exerciseStableId = null,
                        message = "$group: $sets working sets exceeds the recommended MRV ceiling of " +
                            "$mrvCeiling for experience level ${request.userProfile.experienceLevel}",
                        severity = ViolationSeverity.WARNING,
                    ),
                )
            }
        }

        return violations
    }

    /**
     * Per Section 8.6: age-adjusted intensity caps.
     */
    @Suppress("ReturnCount", "CyclomaticComplexMethod")
    private fun checkAgeIntensity(
        exercisePlan: ExercisePlan,
        age: Int?,
        history: ExerciseHistory?,
    ): List<SafetyViolation> {
        if (age == null) return emptyList()

        val intensityReduction = when {
            age < 18 -> 0.15
            age in 41..50 -> 0.025
            age in 51..60 -> 0.05
            age > 60 -> 0.10
            else -> return emptyList()
        }

        // Only check if we have historical context for the "max expected"
        if (history == null || history.sessions.isEmpty()) return emptyList()

        val lastMaxWeight = history.sessions.lastOrNull()?.sets
            ?.filter { it.setType == "working" }
            ?.maxOfOrNull { it.weight } ?: return emptyList()

        val maxAllowed = lastMaxWeight * (1.0 + 0.10) * (1.0 - intensityReduction)
        val plannedMax = exercisePlan.sets
            .filter { it.setType == SetType.WORKING }
            .maxOfOrNull { it.weight } ?: return emptyList()

        if (plannedMax > maxAllowed) {
            val label = when {
                age < 18 -> "under 18"
                age in 41..50 -> "41-50"
                age in 51..60 -> "51-60"
                else -> "60+"
            }
            return listOf(
                SafetyViolation(
                    type = SafetyViolationType.AGE_INTENSITY_EXCEEDED,
                    exerciseStableId = exercisePlan.stableId,
                    message = "${exercisePlan.exerciseName}: planned weight ${plannedMax}kg exceeds " +
                        "age-adjusted maximum (${String.format(Locale.US, "%.1f", maxAllowed)}kg) " +
                        "for age group $label",
                    severity = ViolationSeverity.WARNING,
                ),
            )
        }

        return emptyList()
    }

    /**
     * Per Section 8.4: advanced exercises must not appear for beginners.
     */
    private fun checkDifficultyGating(
        exercisePlan: ExercisePlan,
        exerciseInfo: ExerciseForPlan,
        experienceLevel: Int,
    ): List<SafetyViolation> {
        if (exerciseInfo.difficulty == "advanced" && experienceLevel < 2) {
            return listOf(
                SafetyViolation(
                    type = SafetyViolationType.DIFFICULTY_GATING,
                    exerciseStableId = exercisePlan.stableId,
                    message = "${exercisePlan.exerciseName} is an advanced exercise and should not " +
                        "appear in a beginner plan",
                    severity = ViolationSeverity.HIGH,
                ),
            )
        }
        return emptyList()
    }

    /**
     * Check rest durations meet minimums per exercise type and experience level.
     * Per Section 8.8.
     */
    private fun checkRestDuration(
        exercisePlan: ExercisePlan,
        exerciseInfo: ExerciseForPlan,
        experienceLevel: Int,
    ): List<SafetyViolation> {
        val minRest = getMinimumRest(exerciseInfo, experienceLevel)
        if (exercisePlan.restSeconds < minRest) {
            return listOf(
                SafetyViolation(
                    type = SafetyViolationType.REST_TOO_SHORT,
                    exerciseStableId = exercisePlan.stableId,
                    message = "${exercisePlan.exerciseName}: rest period ${exercisePlan.restSeconds}s is below " +
                        "the minimum recommended ${minRest}s for this exercise type",
                    severity = ViolationSeverity.WARNING,
                ),
            )
        }
        return emptyList()
    }

    private fun getMinimumRest(exercise: ExerciseForPlan, level: Int): Int {
        val isCore = exercise.primaryGroup == "core"
        if (isCore) return 45

        val isCompound = exercise.movementType == "compound"
        return when {
            isCompound -> when (level) {
                1 -> 60
                2 -> 75
                3 -> 90
                else -> 60
            }
            else -> when (level) {
                1 -> 45
                2 -> 45
                3 -> 45
                else -> 45
            }
        }
    }

    private fun getMrvCeiling(experienceLevel: Int): Int = when (experienceLevel) {
        1 -> 12
        2 -> 16
        3 -> 20
        else -> 12
    }

    companion object {
        private const val MAX_TOTAL_WORKING_SETS = 30
        private const val MAX_EXERCISES_PER_SESSION = 12
        private const val MAX_WORKING_SETS_PER_EXERCISE = 6
        private const val MAX_WORKING_SETS_PER_GROUP = 16
    }
}
