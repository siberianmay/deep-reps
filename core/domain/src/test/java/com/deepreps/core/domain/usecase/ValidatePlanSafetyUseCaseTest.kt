package com.deepreps.core.domain.usecase

import com.deepreps.core.domain.model.ExerciseForPlan
import com.deepreps.core.domain.model.ExerciseHistory
import com.deepreps.core.domain.model.ExercisePlan
import com.deepreps.core.domain.model.GeneratedPlan
import com.deepreps.core.domain.model.HistoricalSession
import com.deepreps.core.domain.model.HistoricalSet
import com.deepreps.core.domain.model.PlannedSet
import com.deepreps.core.domain.model.PlanRequest
import com.deepreps.core.domain.model.SafetyViolationType
import com.deepreps.core.domain.model.UserPlanProfile
import com.deepreps.core.domain.model.ViolationSeverity
import com.deepreps.core.domain.model.enums.SetType
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ValidatePlanSafetyUseCaseTest {

    private lateinit var useCase: ValidatePlanSafetyUseCase

    @BeforeEach
    fun setup() {
        useCase = ValidatePlanSafetyUseCase()
    }

    @Test
    fun `detects weight jump exceeding 10 percent for barbell compound`() {
        val plan = makePlan(
            exerciseStableId = "chest_barbell_bench_press",
            workingWeight = 77.0, // 10%+ increase from 60
        )
        val request = makeRequest(
            exercises = listOf(makeExerciseForPlan("chest_barbell_bench_press", "barbell", "compound")),
            history = listOf(
                makeHistory(exerciseId = plan.exercises[0].exerciseId, lastWeight = 60.0),
            ),
        )

        val violations = useCase.validate(plan, request)

        assertThat(violations).isNotEmpty()
        assertThat(violations.any { it.type == SafetyViolationType.WEIGHT_JUMP_EXCEEDED }).isTrue()
    }

    @Test
    fun `accepts weight within 10 percent for barbell compound`() {
        val plan = makePlan(
            exerciseStableId = "chest_barbell_bench_press",
            workingWeight = 65.0, // ~8.3% increase from 60
        )
        val request = makeRequest(
            exercises = listOf(makeExerciseForPlan("chest_barbell_bench_press", "barbell", "compound")),
            history = listOf(
                makeHistory(exerciseId = plan.exercises[0].exerciseId, lastWeight = 60.0),
            ),
        )

        val violations = useCase.validate(plan, request)

        assertThat(violations.none { it.type == SafetyViolationType.WEIGHT_JUMP_EXCEEDED }).isTrue()
    }

    @Test
    fun `detects absolute weight jump exceeding 10kg for barbell`() {
        val plan = makePlan(
            exerciseStableId = "chest_barbell_bench_press",
            workingWeight = 111.0, // 11kg jump from 100
        )
        val request = makeRequest(
            exercises = listOf(makeExerciseForPlan("chest_barbell_bench_press", "barbell", "compound")),
            history = listOf(
                makeHistory(exerciseId = plan.exercises[0].exerciseId, lastWeight = 100.0),
            ),
        )

        val violations = useCase.validate(plan, request)

        assertThat(violations.any { it.type == SafetyViolationType.WEIGHT_JUMP_EXCEEDED }).isTrue()
    }

    @Test
    fun `no weight jump violation when no history exists`() {
        val plan = makePlan(
            exerciseStableId = "chest_barbell_bench_press",
            workingWeight = 100.0,
        )
        val request = makeRequest(
            exercises = listOf(makeExerciseForPlan("chest_barbell_bench_press", "barbell", "compound")),
            history = emptyList(),
        )

        val violations = useCase.validate(plan, request)

        assertThat(violations.none { it.type == SafetyViolationType.WEIGHT_JUMP_EXCEEDED }).isTrue()
    }

    @Test
    fun `detects total working sets exceeding 30`() {
        val exercises = (1..6).map { i ->
            ExercisePlan(
                exerciseId = i.toLong(),
                stableId = "exercise_$i",
                exerciseName = "Exercise $i",
                sets = (1..6).map {
                    PlannedSet(SetType.WORKING, 50.0, 10)
                },
                restSeconds = 90,
            )
        }
        val plan = GeneratedPlan(exercises = exercises) // 36 working sets

        val request = makeRequest(
            exercises = exercises.map {
                makeExerciseForPlan(it.stableId, "barbell", "compound")
            },
        )

        val violations = useCase.validate(plan, request)

        assertThat(violations.any {
            it.type == SafetyViolationType.VOLUME_CEILING_EXCEEDED && it.message.contains("30")
        }).isTrue()
    }

    @Test
    fun `detects per-exercise set ceiling exceeded`() {
        val plan = GeneratedPlan(
            exercises = listOf(
                ExercisePlan(
                    exerciseId = 1,
                    stableId = "chest_barbell_bench_press",
                    exerciseName = "Bench Press",
                    sets = (1..7).map {
                        PlannedSet(SetType.WORKING, 60.0, 10)
                    },
                    restSeconds = 120,
                ),
            ),
        )
        val request = makeRequest(
            exercises = listOf(makeExerciseForPlan("chest_barbell_bench_press", "barbell", "compound")),
        )

        val violations = useCase.validate(plan, request)

        assertThat(violations.any {
            it.type == SafetyViolationType.VOLUME_CEILING_EXCEEDED &&
                it.exerciseStableId == "chest_barbell_bench_press"
        }).isTrue()
    }

    @Test
    fun `detects advanced exercise in beginner plan`() {
        val plan = makePlan(
            exerciseStableId = "lower_back_barbell_good_morning",
            workingWeight = 30.0,
        )
        val request = makeRequest(
            experienceLevel = 1,
            exercises = listOf(
                makeExerciseForPlan(
                    "lower_back_barbell_good_morning", "barbell", "compound", difficulty = "advanced",
                ),
            ),
        )

        val violations = useCase.validate(plan, request)

        assertThat(violations.any { it.type == SafetyViolationType.DIFFICULTY_GATING }).isTrue()
        assertThat(violations.first { it.type == SafetyViolationType.DIFFICULTY_GATING }.severity)
            .isEqualTo(ViolationSeverity.HIGH)
    }

    @Test
    fun `no difficulty gating for intermediate with advanced exercise`() {
        val plan = makePlan(
            exerciseStableId = "lower_back_barbell_good_morning",
            workingWeight = 30.0,
        )
        val request = makeRequest(
            experienceLevel = 2,
            exercises = listOf(
                makeExerciseForPlan(
                    "lower_back_barbell_good_morning", "barbell", "compound", difficulty = "advanced",
                ),
            ),
        )

        val violations = useCase.validate(plan, request)

        assertThat(violations.none { it.type == SafetyViolationType.DIFFICULTY_GATING }).isTrue()
    }

    @Test
    fun `detects age intensity violation for user over 60`() {
        val plan = makePlan(
            exerciseStableId = "chest_barbell_bench_press",
            workingWeight = 65.0,
        )
        val request = makeRequest(
            age = 65,
            exercises = listOf(makeExerciseForPlan("chest_barbell_bench_press", "barbell", "compound")),
            history = listOf(
                makeHistory(exerciseId = plan.exercises[0].exerciseId, lastWeight = 60.0),
            ),
        )

        val violations = useCase.validate(plan, request)

        // 60 * 1.10 * 0.90 = 59.4 is the max allowed. 65 > 59.4
        assertThat(violations.any { it.type == SafetyViolationType.AGE_INTENSITY_EXCEEDED }).isTrue()
    }

    @Test
    fun `no age violation for young user`() {
        val plan = makePlan(
            exerciseStableId = "chest_barbell_bench_press",
            workingWeight = 65.0,
        )
        val request = makeRequest(
            age = 25,
            exercises = listOf(makeExerciseForPlan("chest_barbell_bench_press", "barbell", "compound")),
            history = listOf(
                makeHistory(exerciseId = plan.exercises[0].exerciseId, lastWeight = 60.0),
            ),
        )

        val violations = useCase.validate(plan, request)

        assertThat(violations.none { it.type == SafetyViolationType.AGE_INTENSITY_EXCEEDED }).isTrue()
    }

    @Test
    fun `detects rest period too short`() {
        val plan = GeneratedPlan(
            exercises = listOf(
                ExercisePlan(
                    exerciseId = 1,
                    stableId = "chest_barbell_bench_press",
                    exerciseName = "Bench Press",
                    sets = listOf(PlannedSet(SetType.WORKING, 60.0, 10)),
                    restSeconds = 30, // Too short for compound
                ),
            ),
        )
        val request = makeRequest(
            exercises = listOf(makeExerciseForPlan("chest_barbell_bench_press", "barbell", "compound")),
        )

        val violations = useCase.validate(plan, request)

        assertThat(violations.any { it.type == SafetyViolationType.REST_TOO_SHORT }).isTrue()
    }

    @Test
    fun `valid plan produces no violations`() {
        val plan = GeneratedPlan(
            exercises = listOf(
                ExercisePlan(
                    exerciseId = 1,
                    stableId = "chest_barbell_bench_press",
                    exerciseName = "Bench Press",
                    sets = listOf(
                        PlannedSet(SetType.WARMUP, 20.0, 12),
                        PlannedSet(SetType.WORKING, 40.0, 10),
                        PlannedSet(SetType.WORKING, 40.0, 10),
                        PlannedSet(SetType.WORKING, 40.0, 10),
                    ),
                    restSeconds = 90,
                ),
            ),
        )
        val request = makeRequest(
            exercises = listOf(makeExerciseForPlan("chest_barbell_bench_press", "barbell", "compound")),
        )

        val violations = useCase.validate(plan, request)

        assertThat(violations).isEmpty()
    }

    // --- Helpers ---

    private fun makePlan(
        exerciseStableId: String,
        workingWeight: Double,
    ): GeneratedPlan = GeneratedPlan(
        exercises = listOf(
            ExercisePlan(
                exerciseId = exerciseStableId.hashCode().toLong(),
                stableId = exerciseStableId,
                exerciseName = exerciseStableId,
                sets = listOf(
                    PlannedSet(SetType.WORKING, workingWeight, 10),
                ),
                restSeconds = 120,
            ),
        ),
    )

    private fun makeRequest(
        experienceLevel: Int = 1,
        age: Int? = null,
        exercises: List<ExerciseForPlan> = emptyList(),
        history: List<ExerciseHistory> = emptyList(),
    ): PlanRequest = PlanRequest(
        userProfile = UserPlanProfile(
            experienceLevel = experienceLevel,
            bodyWeightKg = 80.0,
            age = age,
            gender = "male",
        ),
        exercises = exercises,
        trainingHistory = history,
        periodizationModel = "linear",
        performanceTrend = null,
        weeksSinceDeload = null,
        deloadRecommended = false,
        currentBlockPhase = null,
        currentBlockWeek = null,
    )

    private fun makeExerciseForPlan(
        stableId: String,
        equipment: String = "barbell",
        movementType: String = "compound",
        difficulty: String = "beginner",
        primaryGroup: String = "chest",
    ): ExerciseForPlan = ExerciseForPlan(
        exerciseId = stableId.hashCode().toLong(),
        stableId = stableId,
        name = stableId,
        equipment = equipment,
        movementType = movementType,
        difficulty = difficulty,
        primaryGroup = primaryGroup,
    )

    private fun makeHistory(
        exerciseId: Long,
        lastWeight: Double,
    ): ExerciseHistory = ExerciseHistory(
        exerciseId = exerciseId,
        exerciseName = "Exercise",
        sessions = listOf(
            HistoricalSession(
                date = System.currentTimeMillis() - 86_400_000,
                sets = listOf(
                    HistoricalSet(weight = lastWeight, reps = 10, setType = "working"),
                ),
            ),
        ),
    )
}
