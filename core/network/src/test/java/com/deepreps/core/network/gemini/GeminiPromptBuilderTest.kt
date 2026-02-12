package com.deepreps.core.network.gemini

import com.deepreps.core.domain.model.ExerciseForPlan
import com.deepreps.core.domain.model.ExerciseHistory
import com.deepreps.core.domain.model.HistoricalSession
import com.deepreps.core.domain.model.HistoricalSet
import com.deepreps.core.domain.model.PlanRequest
import com.deepreps.core.domain.model.UserPlanProfile
import com.deepreps.core.domain.usecase.CrossGroupOverlapDetector
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GeminiPromptBuilderTest {

    private lateinit var builder: GeminiPromptBuilder

    @BeforeEach
    fun setup() {
        builder = GeminiPromptBuilder(CrossGroupOverlapDetector())
    }

    @Test
    fun `prompt contains user profile section`() {
        val request = makeRequest(
            experienceLevel = 2,
            bodyWeightKg = 80.0,
            age = 30,
            gender = "male",
        )

        val prompt = builder.build(request)

        assertThat(prompt).contains("## User Profile")
        assertThat(prompt).contains("Experience level: Intermediate (6-18 months)")
        assertThat(prompt).contains("Body weight: 80.0kg")
        assertThat(prompt).contains("Age: 30")
        assertThat(prompt).contains("Gender: male")
    }

    @Test
    fun `prompt contains exercise list with stable IDs`() {
        val request = makeRequest(
            exercises = listOf(
                makeExercise("chest_barbell_bench_press", "Barbell Bench Press"),
                makeExercise("chest_dumbbell_fly", "Dumbbell Fly"),
            ),
        )

        val prompt = builder.build(request)

        assertThat(prompt).contains("## Exercises (in order)")
        assertThat(prompt).contains("[stable_id: chest_barbell_bench_press]")
        assertThat(prompt).contains("[stable_id: chest_dumbbell_fly]")
        assertThat(prompt).contains("1. Barbell Bench Press")
        assertThat(prompt).contains("2. Dumbbell Fly")
    }

    @Test
    fun `prompt contains safety constraints section`() {
        val request = makeRequest()

        val prompt = builder.build(request)

        assertThat(prompt).contains("## SAFETY CONSTRAINTS (NON-NEGOTIABLE)")
        assertThat(prompt).contains("Max weight increase: 10%")
        assertThat(prompt).contains("MRV ceiling")
        assertThat(prompt).contains("Advanced exercise gating")
        assertThat(prompt).contains("Warm-up sets")
        assertThat(prompt).contains("Weight rounding")
    }

    @Test
    fun `prompt includes age modifier for user over 60`() {
        val request = makeRequest(age = 65)

        val prompt = builder.build(request)

        assertThat(prompt).contains("AGE MODIFIER (60+)")
        assertThat(prompt).contains("Reduce max intensity by 10%")
        assertThat(prompt).contains("Prefer machine exercises over free weights")
    }

    @Test
    fun `prompt includes age modifier for user 41-50`() {
        val request = makeRequest(age = 45)

        val prompt = builder.build(request)

        assertThat(prompt).contains("AGE MODIFIER (41-50)")
        assertThat(prompt).contains("Reduce max intensity by 2.5%")
    }

    @Test
    fun `prompt has no age modifier for 18-40`() {
        val request = makeRequest(age = 25)

        val prompt = builder.build(request)

        assertThat(prompt).doesNotContain("AGE MODIFIER")
    }

    @Test
    fun `prompt includes deload recommendation when flagged`() {
        val request = makeRequest().copy(deloadRecommended = true)

        val prompt = builder.build(request)

        assertThat(prompt).contains("DELOAD RECOMMENDED")
        assertThat(prompt).contains("reduce volume by 40-60%")
    }

    @Test
    fun `prompt includes good morning contraindication`() {
        val request = makeRequest(
            exercises = listOf(
                makeExercise("lower_back_barbell_good_morning", "Good Morning"),
            ),
        )

        val prompt = builder.build(request)

        assertThat(prompt).contains("EXERCISE-SPECIFIC SAFETY CONSTRAINTS")
        assertThat(prompt).contains("Good Morning: Max weight = 60% of squat working weight")
    }

    @Test
    fun `prompt includes skull crusher contraindication`() {
        val request = makeRequest(
            exercises = listOf(
                makeExercise("arms_ez_bar_skull_crusher", "Skull Crusher"),
            ),
        )

        val prompt = builder.build(request)

        assertThat(prompt).contains("Skull Crusher: Maximum 3 working sets")
    }

    @Test
    fun `prompt includes cross-group fatigue for chest plus arms`() {
        val request = makeRequest(
            exercises = listOf(
                makeExercise("chest_barbell_bench_press", "Bench Press", primaryGroup = "chest"),
                makeExercise("arms_cable_tricep_pushdown", "Tricep Pushdown", primaryGroup = "arms"),
            ),
        )

        val prompt = builder.build(request)

        assertThat(prompt).contains("CROSS-GROUP FATIGUE WARNING")
        assertThat(prompt).contains("triceps")
    }

    @Test
    fun `prompt includes output format JSON schema`() {
        val request = makeRequest()

        val prompt = builder.build(request)

        assertThat(prompt).contains("## Output Format")
        assertThat(prompt).contains("\"exercise_plans\"")
        assertThat(prompt).contains("\"warmup_sets\"")
        assertThat(prompt).contains("\"working_sets\"")
        assertThat(prompt).contains("\"session_summary\"")
    }

    @Test
    fun `prompt includes training history when available`() {
        val history = listOf(
            ExerciseHistory(
                exerciseId = 1,
                exerciseName = "Bench Press",
                sessions = listOf(
                    HistoricalSession(
                        date = 1700000000000,
                        sets = listOf(
                            HistoricalSet(weight = 60.0, reps = 10, setType = "working"),
                        ),
                    ),
                ),
            ),
        )

        val request = makeRequest().copy(trainingHistory = history)

        val prompt = builder.build(request)

        assertThat(prompt).contains("## Recent Training History")
        assertThat(prompt).contains("Bench Press")
        assertThat(prompt).contains("60.0kg x 10 (working)")
    }

    @Test
    fun `prompt includes no-history fallback message when empty`() {
        val request = makeRequest()

        val prompt = builder.build(request)

        assertThat(prompt).contains("NO TRAINING HISTORY AVAILABLE")
    }

    @Test
    fun `prompt includes periodization context`() {
        val request = makeRequest().copy(
            periodizationModel = "dup",
            performanceTrend = "improving",
            weeksSinceDeload = 3,
        )

        val prompt = builder.build(request)

        assertThat(prompt).contains("## Progression Context")
        assertThat(prompt).contains("Periodization model: dup")
        assertThat(prompt).contains("Performance trend: improving")
        assertThat(prompt).contains("Weeks since last deload: 3")
    }

    @Test
    fun `prompt version is v2_0`() {
        assertThat(GeminiPromptBuilder.PROMPT_VERSION).isEqualTo("v2.0")
    }

    @Test
    fun `MRV ceiling is correct for each level`() {
        assertThat(GeminiPromptBuilder.getMrvCeiling(1)).isEqualTo(12)
        assertThat(GeminiPromptBuilder.getMrvCeiling(2)).isEqualTo(16)
        assertThat(GeminiPromptBuilder.getMrvCeiling(3)).isEqualTo(20)
    }

    // --- Helpers ---

    private fun makeRequest(
        experienceLevel: Int = 1,
        bodyWeightKg: Double = 75.0,
        age: Int? = null,
        gender: String? = null,
        exercises: List<ExerciseForPlan> = listOf(
            makeExercise("chest_barbell_bench_press", "Barbell Bench Press"),
        ),
    ): PlanRequest = PlanRequest(
        userProfile = UserPlanProfile(
            experienceLevel = experienceLevel,
            bodyWeightKg = bodyWeightKg,
            age = age,
            gender = gender,
        ),
        exercises = exercises,
        trainingHistory = emptyList(),
        periodizationModel = "linear",
        performanceTrend = null,
        weeksSinceDeload = null,
        deloadRecommended = false,
        currentBlockPhase = null,
        currentBlockWeek = null,
    )

    private fun makeExercise(
        stableId: String,
        name: String,
        equipment: String = "barbell",
        movementType: String = "compound",
        difficulty: String = "beginner",
        primaryGroup: String = "chest",
    ): ExerciseForPlan = ExerciseForPlan(
        exerciseId = stableId.hashCode().toLong(),
        stableId = stableId,
        name = name,
        equipment = equipment,
        movementType = movementType,
        difficulty = difficulty,
        primaryGroup = primaryGroup,
    )
}
