package com.deepreps.core.domain.provider

import com.deepreps.core.domain.model.ExerciseForPlan
import com.deepreps.core.domain.model.ExercisePlan
import com.deepreps.core.domain.model.GeneratedPlan
import com.deepreps.core.domain.model.PlannedSet
import com.deepreps.core.domain.model.PlanRequest
import com.deepreps.core.domain.model.enums.SetType
import com.deepreps.core.domain.util.WeightStepProvider
import com.deepreps.core.domain.model.enums.Equipment
import javax.inject.Inject
import kotlin.math.floor

/**
 * Generates a default workout plan when AI is unavailable.
 *
 * Uses BW ratio tables from exercise-science.md Section 4 for weight suggestions.
 * This is the OFFLINE FALLBACK -- must work without any network.
 *
 * Logic:
 * 1. Look up user experience level and body weight
 * 2. Apply BW ratio tables from exercise-science.md Sections 4.1-4.3
 * 3. Calculate working weights with proper rounding (Section 8.7)
 * 4. Generate warm-up sets per protocol (Section 8.5)
 * 5. Apply gender fallback (male ratios - 15% when gender unknown)
 * 6. Apply age modifiers (Section 8.6)
 */
class BaselinePlanGenerator @Inject constructor() {

    fun generate(request: PlanRequest): GeneratedPlan? {
        val bodyWeightKg = request.userProfile.bodyWeightKg ?: return null
        val level = request.userProfile.experienceLevel.coerceIn(1, 3)
        val gender = request.userProfile.gender
        val age = request.userProfile.age

        val exercisePlans = request.exercises.map { exercise ->
            generateExercisePlan(exercise, level, bodyWeightKg, gender, age, request.deloadRecommended)
        }

        return GeneratedPlan(exercises = exercisePlans)
    }

    private fun generateExercisePlan(
        exercise: ExerciseForPlan,
        level: Int,
        bodyWeightKg: Double,
        gender: String?,
        age: Int?,
        deloadRecommended: Boolean,
    ): ExercisePlan {
        val equipment = tryParseEquipment(exercise.equipment)
        val isCompound = exercise.movementType == "compound"
        val isBw = exercise.equipment == "bodyweight"

        val rawWeight = calculateBaselineWeight(exercise.stableId, level, bodyWeightKg, gender)
        var workingWeight = roundDown(rawWeight, equipment)

        // Apply deload reduction
        if (deloadRecommended) {
            workingWeight = roundDown(workingWeight * DELOAD_INTENSITY_FACTOR, equipment)
        }

        // Apply age modifier to intensity
        val ageModifier = getAgeIntensityModifier(age)
        if (ageModifier > 0.0) {
            workingWeight = roundDown(workingWeight * (1.0 - ageModifier), equipment)
        }

        val workingSetsCount = getWorkingSetsCount(level, deloadRecommended)
        val targetReps = getTargetReps(level, isCompound)
        val restSeconds = getRestSeconds(level, exercise)

        val warmupSets = generateWarmupSets(workingWeight, exercise, level, age, equipment)
        val workingSets = (1..workingSetsCount).map { setNum ->
            PlannedSet(
                setType = SetType.WORKING,
                weight = if (isBw) 0.0 else workingWeight,
                reps = targetReps,
                restSeconds = restSeconds,
            )
        }

        return ExercisePlan(
            exerciseId = exercise.exerciseId,
            stableId = exercise.stableId,
            exerciseName = exercise.name,
            sets = warmupSets + workingSets,
            restSeconds = restSeconds,
            notes = if (deloadRecommended) "Deload week: reduced volume and intensity" else null,
        )
    }

    private fun generateWarmupSets(
        workingWeight: Double,
        exercise: ExerciseForPlan,
        level: Int,
        age: Int?,
        equipment: Equipment,
    ): List<PlannedSet> {
        val isCompound = exercise.movementType == "compound"
        val isBw = exercise.equipment == "bodyweight"
        val isHeavyCompound = isCompound && exercise.stableId in HEAVY_COMPOUND_IDS

        if (isBw) {
            if (!isCompound) return emptyList()
            return listOf(
                PlannedSet(SetType.WARMUP, weight = 0.0, reps = 10, restSeconds = WARMUP_REST),
            )
        }

        val isOver50 = age != null && age >= 50
        val emptyBarWeight = if (exercise.equipment == "barbell" || exercise.equipment == "ez_bar") 20.0 else 0.0

        return when {
            isHeavyCompound -> {
                val sets = mutableListOf(
                    PlannedSet(SetType.WARMUP, roundDown(emptyBarWeight, equipment), reps = 12, restSeconds = WARMUP_REST),
                )
                if (isOver50) {
                    sets.add(PlannedSet(SetType.WARMUP, roundDown(workingWeight * 0.40, equipment), reps = 10, restSeconds = WARMUP_REST))
                    sets.add(PlannedSet(SetType.WARMUP, roundDown(workingWeight * 0.60, equipment), reps = 8, restSeconds = WARMUP_REST))
                    sets.add(PlannedSet(SetType.WARMUP, roundDown(workingWeight * 0.80, equipment), reps = 4, restSeconds = WARMUP_REST))
                } else {
                    sets.add(PlannedSet(SetType.WARMUP, roundDown(workingWeight * 0.50, equipment), reps = 8, restSeconds = WARMUP_REST))
                    sets.add(PlannedSet(SetType.WARMUP, roundDown(workingWeight * 0.75, equipment), reps = 4, restSeconds = WARMUP_REST))
                }
                sets
            }
            isCompound -> {
                val sets = mutableListOf(
                    PlannedSet(SetType.WARMUP, roundDown(workingWeight * 0.50, equipment), reps = 10, restSeconds = WARMUP_REST),
                )
                if (isOver50) {
                    sets.add(PlannedSet(SetType.WARMUP, roundDown(workingWeight * 0.60, equipment), reps = 8, restSeconds = WARMUP_REST))
                    sets.add(PlannedSet(SetType.WARMUP, roundDown(workingWeight * 0.80, equipment), reps = 4, restSeconds = WARMUP_REST))
                } else {
                    sets.add(PlannedSet(SetType.WARMUP, roundDown(workingWeight * 0.75, equipment), reps = 6, restSeconds = WARMUP_REST))
                }
                sets
            }
            else -> {
                // Isolation: 1 warmup set (or 0 for bodyweight)
                if (isOver50) {
                    listOf(
                        PlannedSet(SetType.WARMUP, roundDown(workingWeight * 0.40, equipment), reps = 12, restSeconds = WARMUP_REST),
                        PlannedSet(SetType.WARMUP, roundDown(workingWeight * 0.70, equipment), reps = 8, restSeconds = WARMUP_REST),
                    )
                } else {
                    listOf(
                        PlannedSet(SetType.WARMUP, roundDown(workingWeight * 0.50, equipment), reps = 12, restSeconds = WARMUP_REST),
                    )
                }
            }
        }
    }

    private fun getWorkingSetsCount(level: Int, deloadRecommended: Boolean): Int {
        val baseSets = when (level) {
            1 -> 3
            2 -> 4
            3 -> 5
            else -> 3
        }
        return if (deloadRecommended) (baseSets * 0.5).toInt().coerceAtLeast(2) else baseSets
    }

    private fun getTargetReps(level: Int, isCompound: Boolean): Int = when (level) {
        1 -> if (isCompound) 10 else 12
        2 -> if (isCompound) 8 else 10
        3 -> if (isCompound) 5 else 10
        else -> 10
    }

    private fun getRestSeconds(level: Int, exercise: ExerciseForPlan): Int {
        val isHeavyCompound = exercise.movementType == "compound" && exercise.stableId in HEAVY_COMPOUND_IDS
        val isCompound = exercise.movementType == "compound"
        val isCore = exercise.primaryGroup == "core"

        return when {
            isCore -> 60
            isHeavyCompound -> when (level) {
                1 -> 90
                2 -> 120
                3 -> 180
                else -> 90
            }
            isCompound -> when (level) {
                1 -> 75
                2 -> 105
                3 -> 120
                else -> 75
            }
            else -> when (level) {
                1 -> 60
                2 -> 75
                3 -> 75
                else -> 60
            }
        }
    }

    private fun getAgeIntensityModifier(age: Int?): Double {
        if (age == null) return 0.0
        return when {
            age < 18 -> 0.15
            age in 41..50 -> 0.025
            age in 51..60 -> 0.05
            age > 60 -> 0.10
            else -> 0.0
        }
    }

    private fun calculateBaselineWeight(
        stableId: String,
        level: Int,
        bodyWeightKg: Double,
        gender: String?,
    ): Double {
        val ratioTable = when (level) {
            1 -> BEGINNER_RATIOS
            2 -> INTERMEDIATE_RATIOS
            3 -> ADVANCED_RATIOS
            else -> BEGINNER_RATIOS
        }

        val ratios = ratioTable[stableId] ?: return 20.0 // Conservative fallback

        val ratio = when (gender) {
            "male" -> ratios.male
            "female" -> ratios.female
            else -> ratios.male * GENDER_UNKNOWN_FACTOR
        }

        return ratio * bodyWeightKg
    }

    private fun roundDown(weight: Double, equipment: Equipment): Double =
        WeightStepProvider.roundDown(weight, equipment)

    private fun tryParseEquipment(value: String): Equipment =
        try {
            Equipment.fromValue(value)
        } catch (_: IllegalArgumentException) {
            Equipment.BARBELL
        }

    companion object {
        private const val WARMUP_REST = 60
        private const val DELOAD_INTENSITY_FACTOR = 0.575 // Midpoint of 50-65%
        private const val GENDER_UNKNOWN_FACTOR = 0.85

        /**
         * Heavy compound IDs for rest timer and warmup protocol.
         */
        private val HEAVY_COMPOUND_IDS = setOf(
            "legs_barbell_back_squat",
            "legs_barbell_front_squat",
            "lower_back_barbell_conventional_deadlift",
            "lower_back_barbell_sumo_deadlift",
            "lower_back_trap_bar_deadlift",
            "lower_back_barbell_deficit_deadlift",
            "chest_barbell_bench_press",
            "chest_barbell_incline_bench_press",
            "chest_barbell_decline_bench_press",
            "shoulders_barbell_overhead_press",
            "back_barbell_bent_over_row",
        )
    }
}

/**
 * BW ratio for a single exercise at a given level.
 * Values from exercise-science.md Section 4.1-4.3.
 */
internal data class BwRatio(
    val male: Double,
    val female: Double,
)

/**
 * Beginner BW ratios per exercise-science.md Section 4.1.
 */
private val BEGINNER_RATIOS: Map<String, BwRatio> = mapOf(
    // Legs
    "legs_barbell_back_squat" to BwRatio(0.50, 0.35),
    "legs_barbell_front_squat" to BwRatio(0.40, 0.28),
    "legs_machine_leg_press" to BwRatio(0.80, 0.60),
    "legs_barbell_rdl" to BwRatio(0.45, 0.30),
    "legs_dumbbell_walking_lunges" to BwRatio(0.12, 0.08),
    "legs_dumbbell_bulgarian_split_squat" to BwRatio(0.12, 0.08),
    "legs_machine_hack_squat" to BwRatio(0.60, 0.45),
    "legs_machine_leg_extension" to BwRatio(0.35, 0.25),
    "legs_machine_lying_leg_curl" to BwRatio(0.25, 0.18),
    "legs_machine_seated_leg_curl" to BwRatio(0.28, 0.20),
    "legs_machine_standing_calf_raise" to BwRatio(0.40, 0.30),
    "legs_machine_seated_calf_raise" to BwRatio(0.30, 0.22),
    // Lower Back
    "lower_back_barbell_conventional_deadlift" to BwRatio(0.60, 0.40),
    "lower_back_barbell_sumo_deadlift" to BwRatio(0.55, 0.38),
    "lower_back_trap_bar_deadlift" to BwRatio(0.65, 0.45),
    "lower_back_barbell_rack_pull" to BwRatio(0.70, 0.48),
    "lower_back_bodyweight_back_extension" to BwRatio(0.0, 0.0),
    "lower_back_machine_reverse_hyperextension" to BwRatio(0.10, 0.08),
    "lower_back_barbell_hip_thrust" to BwRatio(0.50, 0.35),
    "lower_back_cable_pull_through" to BwRatio(0.25, 0.18),
    // Chest
    "chest_barbell_bench_press" to BwRatio(0.40, 0.20),
    "chest_barbell_incline_bench_press" to BwRatio(0.35, 0.18),
    "chest_dumbbell_bench_press" to BwRatio(0.18, 0.09),
    "chest_dumbbell_incline_press" to BwRatio(0.15, 0.08),
    "chest_barbell_decline_bench_press" to BwRatio(0.42, 0.22),
    "chest_dumbbell_fly" to BwRatio(0.08, 0.04),
    "chest_cable_crossover" to BwRatio(0.12, 0.07),
    "chest_machine_chest_press" to BwRatio(0.35, 0.20),
    "chest_machine_pec_deck" to BwRatio(0.20, 0.12),
    "chest_bodyweight_push_up" to BwRatio(0.0, 0.0),
    "chest_bodyweight_dips" to BwRatio(0.0, 0.0),
    // Back
    "back_barbell_bent_over_row" to BwRatio(0.35, 0.20),
    "back_bodyweight_pull_up" to BwRatio(0.0, 0.0),
    "back_cable_lat_pulldown" to BwRatio(0.35, 0.25),
    "back_cable_seated_row" to BwRatio(0.40, 0.28),
    "back_dumbbell_single_arm_row" to BwRatio(0.15, 0.10),
    "back_barbell_t_bar_row" to BwRatio(0.30, 0.20),
    "back_machine_chest_supported_row" to BwRatio(0.35, 0.25),
    "back_bodyweight_chin_up" to BwRatio(0.0, 0.0),
    "back_cable_straight_arm_pulldown" to BwRatio(0.15, 0.10),
    "back_cable_face_pull" to BwRatio(0.10, 0.07),
    "back_cable_lat_pullover" to BwRatio(0.18, 0.12),
    // Shoulders
    "shoulders_barbell_overhead_press" to BwRatio(0.25, 0.15),
    "shoulders_dumbbell_shoulder_press" to BwRatio(0.12, 0.07),
    "shoulders_dumbbell_arnold_press" to BwRatio(0.10, 0.06),
    "shoulders_dumbbell_lateral_raise" to BwRatio(0.04, 0.02),
    "shoulders_cable_lateral_raise" to BwRatio(0.05, 0.03),
    "shoulders_dumbbell_front_raise" to BwRatio(0.05, 0.03),
    "shoulders_dumbbell_reverse_fly" to BwRatio(0.04, 0.02),
    "shoulders_barbell_upright_row" to BwRatio(0.20, 0.12),
    "shoulders_barbell_shrug" to BwRatio(0.40, 0.25),
    "shoulders_dumbbell_shrug" to BwRatio(0.20, 0.12),
    "shoulders_barbell_landmine_press" to BwRatio(0.15, 0.10),
    // Arms
    "arms_barbell_curl" to BwRatio(0.15, 0.08),
    "arms_dumbbell_curl" to BwRatio(0.07, 0.04),
    "arms_dumbbell_hammer_curl" to BwRatio(0.08, 0.05),
    "arms_barbell_preacher_curl" to BwRatio(0.12, 0.07),
    "arms_dumbbell_incline_curl" to BwRatio(0.06, 0.03),
    "arms_cable_curl" to BwRatio(0.12, 0.07),
    "arms_barbell_close_grip_bench_press" to BwRatio(0.35, 0.18),
    "arms_ez_bar_skull_crusher" to BwRatio(0.12, 0.07),
    "arms_cable_tricep_pushdown" to BwRatio(0.15, 0.10),
    "arms_dumbbell_overhead_tricep_extension" to BwRatio(0.08, 0.05),
    "arms_dumbbell_concentration_curl" to BwRatio(0.07, 0.04),
    "arms_barbell_wrist_curl" to BwRatio(0.10, 0.06),
    // Core
    "core_bodyweight_plank" to BwRatio(0.0, 0.0),
    "core_bodyweight_hanging_leg_raise" to BwRatio(0.0, 0.0),
    "core_cable_crunch" to BwRatio(0.20, 0.15),
    "core_bodyweight_russian_twist" to BwRatio(0.0, 0.0),
    "core_bodyweight_bicycle_crunch" to BwRatio(0.0, 0.0),
    "core_bodyweight_dead_bug" to BwRatio(0.0, 0.0),
    "core_cable_pallof_press" to BwRatio(0.10, 0.07),
    "core_bodyweight_decline_sit_up" to BwRatio(0.0, 0.0),
    "core_bodyweight_side_plank" to BwRatio(0.0, 0.0),
)

/**
 * Intermediate BW ratios per exercise-science.md Section 4.2.
 */
private val INTERMEDIATE_RATIOS: Map<String, BwRatio> = mapOf(
    // Legs
    "legs_barbell_back_squat" to BwRatio(1.00, 0.70),
    "legs_barbell_front_squat" to BwRatio(0.80, 0.56),
    "legs_machine_leg_press" to BwRatio(1.50, 1.10),
    "legs_barbell_rdl" to BwRatio(0.90, 0.63),
    "legs_dumbbell_walking_lunges" to BwRatio(0.25, 0.18),
    "legs_dumbbell_bulgarian_split_squat" to BwRatio(0.25, 0.18),
    "legs_machine_hack_squat" to BwRatio(1.20, 0.85),
    "legs_machine_leg_extension" to BwRatio(0.65, 0.48),
    "legs_machine_lying_leg_curl" to BwRatio(0.50, 0.38),
    "legs_machine_seated_leg_curl" to BwRatio(0.55, 0.42),
    "legs_machine_standing_calf_raise" to BwRatio(0.80, 0.60),
    "legs_machine_seated_calf_raise" to BwRatio(0.60, 0.45),
    // Lower Back
    "lower_back_barbell_conventional_deadlift" to BwRatio(1.25, 0.85),
    "lower_back_barbell_sumo_deadlift" to BwRatio(1.15, 0.80),
    "lower_back_trap_bar_deadlift" to BwRatio(1.35, 0.95),
    "lower_back_barbell_rack_pull" to BwRatio(1.45, 1.00),
    "lower_back_barbell_good_morning" to BwRatio(0.35, 0.25),
    "lower_back_bodyweight_back_extension" to BwRatio(0.0, 0.0),
    "lower_back_machine_reverse_hyperextension" to BwRatio(0.25, 0.18),
    "lower_back_barbell_hip_thrust" to BwRatio(1.00, 0.70),
    "lower_back_cable_pull_through" to BwRatio(0.50, 0.38),
    // Chest
    "chest_barbell_bench_press" to BwRatio(0.75, 0.40),
    "chest_barbell_incline_bench_press" to BwRatio(0.65, 0.35),
    "chest_dumbbell_bench_press" to BwRatio(0.35, 0.19),
    "chest_dumbbell_incline_press" to BwRatio(0.30, 0.17),
    "chest_barbell_decline_bench_press" to BwRatio(0.80, 0.45),
    "chest_dumbbell_fly" to BwRatio(0.15, 0.09),
    "chest_cable_crossover" to BwRatio(0.22, 0.14),
    "chest_machine_chest_press" to BwRatio(0.65, 0.40),
    "chest_machine_pec_deck" to BwRatio(0.38, 0.25),
    "chest_bodyweight_push_up" to BwRatio(0.0, 0.0),
    "chest_bodyweight_dips" to BwRatio(0.0, 0.0),
    // Back
    "back_barbell_bent_over_row" to BwRatio(0.65, 0.40),
    "back_bodyweight_pull_up" to BwRatio(0.0, 0.0),
    "back_cable_lat_pulldown" to BwRatio(0.55, 0.40),
    "back_cable_seated_row" to BwRatio(0.75, 0.53),
    "back_dumbbell_single_arm_row" to BwRatio(0.30, 0.20),
    "back_barbell_t_bar_row" to BwRatio(0.60, 0.42),
    "back_machine_chest_supported_row" to BwRatio(0.65, 0.48),
    "back_bodyweight_chin_up" to BwRatio(0.0, 0.0),
    "back_cable_straight_arm_pulldown" to BwRatio(0.28, 0.20),
    "back_cable_face_pull" to BwRatio(0.20, 0.15),
    "back_cable_lat_pullover" to BwRatio(0.35, 0.25),
    // Shoulders
    "shoulders_barbell_overhead_press" to BwRatio(0.45, 0.25),
    "shoulders_dumbbell_shoulder_press" to BwRatio(0.22, 0.13),
    "shoulders_dumbbell_arnold_press" to BwRatio(0.18, 0.11),
    "shoulders_dumbbell_lateral_raise" to BwRatio(0.08, 0.05),
    "shoulders_cable_lateral_raise" to BwRatio(0.10, 0.07),
    "shoulders_dumbbell_front_raise" to BwRatio(0.10, 0.06),
    "shoulders_dumbbell_reverse_fly" to BwRatio(0.08, 0.05),
    "shoulders_barbell_upright_row" to BwRatio(0.38, 0.25),
    "shoulders_barbell_shrug" to BwRatio(0.80, 0.55),
    "shoulders_dumbbell_shrug" to BwRatio(0.40, 0.28),
    "shoulders_barbell_landmine_press" to BwRatio(0.30, 0.20),
    // Arms
    "arms_barbell_curl" to BwRatio(0.25, 0.13),
    "arms_dumbbell_curl" to BwRatio(0.13, 0.07),
    "arms_dumbbell_hammer_curl" to BwRatio(0.15, 0.09),
    "arms_barbell_preacher_curl" to BwRatio(0.22, 0.12),
    "arms_dumbbell_incline_curl" to BwRatio(0.11, 0.06),
    "arms_cable_curl" to BwRatio(0.23, 0.14),
    "arms_barbell_close_grip_bench_press" to BwRatio(0.65, 0.35),
    "arms_ez_bar_skull_crusher" to BwRatio(0.22, 0.13),
    "arms_cable_tricep_pushdown" to BwRatio(0.28, 0.18),
    "arms_dumbbell_overhead_tricep_extension" to BwRatio(0.15, 0.10),
    "arms_dumbbell_concentration_curl" to BwRatio(0.13, 0.07),
    "arms_barbell_wrist_curl" to BwRatio(0.18, 0.11),
    // Core
    "core_bodyweight_plank" to BwRatio(0.0, 0.0),
    "core_bodyweight_hanging_leg_raise" to BwRatio(0.0, 0.0),
    "core_cable_crunch" to BwRatio(0.40, 0.30),
    "core_bodyweight_russian_twist" to BwRatio(0.0, 0.0),
    "core_bodyweight_bicycle_crunch" to BwRatio(0.0, 0.0),
    "core_bodyweight_dead_bug" to BwRatio(0.0, 0.0),
    "core_cable_pallof_press" to BwRatio(0.20, 0.15),
    "core_bodyweight_decline_sit_up" to BwRatio(0.0, 0.0),
    "core_bodyweight_side_plank" to BwRatio(0.0, 0.0),
)

/**
 * Advanced BW ratios per exercise-science.md Section 4.3.
 */
private val ADVANCED_RATIOS: Map<String, BwRatio> = mapOf(
    // Legs
    "legs_barbell_back_squat" to BwRatio(1.50, 1.10),
    "legs_barbell_front_squat" to BwRatio(1.25, 0.90),
    "legs_machine_leg_press" to BwRatio(2.50, 1.80),
    "legs_barbell_rdl" to BwRatio(1.35, 1.00),
    "legs_dumbbell_walking_lunges" to BwRatio(0.40, 0.30),
    "legs_dumbbell_bulgarian_split_squat" to BwRatio(0.40, 0.30),
    "legs_machine_hack_squat" to BwRatio(2.00, 1.45),
    "legs_machine_leg_extension" to BwRatio(1.00, 0.75),
    "legs_machine_lying_leg_curl" to BwRatio(0.75, 0.58),
    "legs_machine_seated_leg_curl" to BwRatio(0.85, 0.65),
    "legs_machine_standing_calf_raise" to BwRatio(1.25, 0.95),
    "legs_machine_seated_calf_raise" to BwRatio(1.00, 0.75),
    // Lower Back
    "lower_back_barbell_conventional_deadlift" to BwRatio(1.75, 1.25),
    "lower_back_barbell_sumo_deadlift" to BwRatio(1.65, 1.18),
    "lower_back_trap_bar_deadlift" to BwRatio(1.90, 1.38),
    "lower_back_barbell_rack_pull" to BwRatio(2.00, 1.45),
    "lower_back_barbell_good_morning" to BwRatio(0.60, 0.43),
    "lower_back_bodyweight_back_extension" to BwRatio(0.0, 0.0),
    "lower_back_machine_reverse_hyperextension" to BwRatio(0.45, 0.33),
    "lower_back_barbell_hip_thrust" to BwRatio(1.75, 1.25),
    "lower_back_cable_pull_through" to BwRatio(0.85, 0.63),
    "lower_back_barbell_deficit_deadlift" to BwRatio(1.50, 1.08),
    // Chest
    "chest_barbell_bench_press" to BwRatio(1.25, 0.65),
    "chest_barbell_incline_bench_press" to BwRatio(1.10, 0.58),
    "chest_dumbbell_bench_press" to BwRatio(0.55, 0.30),
    "chest_dumbbell_incline_press" to BwRatio(0.48, 0.27),
    "chest_barbell_decline_bench_press" to BwRatio(1.30, 0.70),
    "chest_dumbbell_fly" to BwRatio(0.25, 0.15),
    "chest_cable_crossover" to BwRatio(0.38, 0.23),
    "chest_machine_chest_press" to BwRatio(1.10, 0.65),
    "chest_machine_pec_deck" to BwRatio(0.65, 0.43),
    "chest_bodyweight_push_up" to BwRatio(0.0, 0.0),
    "chest_bodyweight_dips" to BwRatio(0.0, 0.0),
    // Back
    "back_barbell_bent_over_row" to BwRatio(0.90, 0.55),
    "back_bodyweight_pull_up" to BwRatio(0.0, 0.0),
    "back_cable_lat_pulldown" to BwRatio(0.75, 0.55),
    "back_cable_seated_row" to BwRatio(1.10, 0.80),
    "back_dumbbell_single_arm_row" to BwRatio(0.48, 0.33),
    "back_barbell_t_bar_row" to BwRatio(0.95, 0.68),
    "back_machine_chest_supported_row" to BwRatio(1.00, 0.73),
    "back_bodyweight_chin_up" to BwRatio(0.0, 0.0),
    "back_cable_straight_arm_pulldown" to BwRatio(0.45, 0.33),
    "back_cable_face_pull" to BwRatio(0.35, 0.25),
    "back_cable_lat_pullover" to BwRatio(0.55, 0.40),
    // Shoulders
    "shoulders_barbell_overhead_press" to BwRatio(0.70, 0.40),
    "shoulders_dumbbell_shoulder_press" to BwRatio(0.35, 0.20),
    "shoulders_dumbbell_arnold_press" to BwRatio(0.30, 0.18),
    "shoulders_dumbbell_lateral_raise" to BwRatio(0.13, 0.08),
    "shoulders_cable_lateral_raise" to BwRatio(0.17, 0.11),
    "shoulders_dumbbell_front_raise" to BwRatio(0.17, 0.10),
    "shoulders_dumbbell_reverse_fly" to BwRatio(0.13, 0.08),
    "shoulders_barbell_upright_row" to BwRatio(0.60, 0.40),
    "shoulders_barbell_shrug" to BwRatio(1.30, 0.90),
    "shoulders_dumbbell_shrug" to BwRatio(0.65, 0.45),
    "shoulders_barbell_landmine_press" to BwRatio(0.50, 0.35),
    // Arms
    "arms_barbell_curl" to BwRatio(0.35, 0.18),
    "arms_dumbbell_curl" to BwRatio(0.20, 0.11),
    "arms_dumbbell_hammer_curl" to BwRatio(0.23, 0.14),
    "arms_barbell_preacher_curl" to BwRatio(0.32, 0.18),
    "arms_dumbbell_incline_curl" to BwRatio(0.17, 0.10),
    "arms_cable_curl" to BwRatio(0.35, 0.21),
    "arms_barbell_close_grip_bench_press" to BwRatio(1.00, 0.55),
    "arms_ez_bar_skull_crusher" to BwRatio(0.35, 0.20),
    "arms_cable_tricep_pushdown" to BwRatio(0.45, 0.30),
    "arms_dumbbell_overhead_tricep_extension" to BwRatio(0.25, 0.17),
    "arms_dumbbell_concentration_curl" to BwRatio(0.20, 0.11),
    "arms_barbell_wrist_curl" to BwRatio(0.28, 0.18),
    // Core
    "core_bodyweight_plank" to BwRatio(0.0, 0.0),
    "core_bodyweight_ab_wheel_rollout" to BwRatio(0.0, 0.0),
    "core_bodyweight_hanging_leg_raise" to BwRatio(0.0, 0.0),
    "core_cable_crunch" to BwRatio(0.65, 0.50),
    "core_bodyweight_russian_twist" to BwRatio(0.0, 0.0),
    "core_bodyweight_bicycle_crunch" to BwRatio(0.0, 0.0),
    "core_bodyweight_dead_bug" to BwRatio(0.0, 0.0),
    "core_cable_pallof_press" to BwRatio(0.35, 0.25),
    "core_bodyweight_decline_sit_up" to BwRatio(0.0, 0.0),
    "core_bodyweight_side_plank" to BwRatio(0.0, 0.0),
    "core_bodyweight_dragon_flag" to BwRatio(0.0, 0.0),
)
