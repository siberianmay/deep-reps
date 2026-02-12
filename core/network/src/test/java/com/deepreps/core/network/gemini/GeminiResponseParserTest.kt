package com.deepreps.core.network.gemini

import com.deepreps.core.domain.model.ExerciseForPlan
import com.deepreps.core.domain.model.enums.SetType
import com.deepreps.core.domain.provider.AiPlanException
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GeminiResponseParserTest {

    private lateinit var parser: GeminiResponseParser

    private val testExercises = listOf(
        ExerciseForPlan(
            exerciseId = 1,
            stableId = "chest_barbell_bench_press",
            name = "Barbell Bench Press",
            equipment = "barbell",
            movementType = "compound",
            difficulty = "beginner",
            primaryGroup = "chest",
        ),
        ExerciseForPlan(
            exerciseId = 2,
            stableId = "chest_dumbbell_fly",
            name = "Dumbbell Fly",
            equipment = "dumbbell",
            movementType = "isolation",
            difficulty = "beginner",
            primaryGroup = "chest",
        ),
    )

    @BeforeEach
    fun setup() {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
        parser = GeminiResponseParser(json)
    }

    @Test
    fun `parses valid JSON with warmup and working sets`() {
        val json = """
        {
          "exercise_plans": [
            {
              "exercise_id": "chest_barbell_bench_press",
              "warmup_sets": [
                {"weight": 20, "reps": 12, "set_number": 1},
                {"weight": 40, "reps": 8, "set_number": 2}
              ],
              "working_sets": [
                {"weight": 60, "reps": 10, "set_number": 1},
                {"weight": 60, "reps": 10, "set_number": 2},
                {"weight": 60, "reps": 10, "set_number": 3}
              ],
              "rest_seconds": 120,
              "notes": "Progressive overload from last session."
            }
          ],
          "session_summary": {
            "total_working_sets": 3,
            "estimated_duration_minutes": 30,
            "volume_check": "ok"
          }
        }
        """.trimIndent()

        val plan = parser.parse(json, testExercises)

        assertThat(plan.exercises).hasSize(1)

        val benchPlan = plan.exercises[0]
        assertThat(benchPlan.exerciseId).isEqualTo(1)
        assertThat(benchPlan.stableId).isEqualTo("chest_barbell_bench_press")
        assertThat(benchPlan.exerciseName).isEqualTo("Barbell Bench Press")
        assertThat(benchPlan.restSeconds).isEqualTo(120)
        assertThat(benchPlan.notes).isEqualTo("Progressive overload from last session.")

        val warmupSets = benchPlan.sets.filter { it.setType == SetType.WARMUP }
        val workingSets = benchPlan.sets.filter { it.setType == SetType.WORKING }

        assertThat(warmupSets).hasSize(2)
        assertThat(workingSets).hasSize(3)
        assertThat(warmupSets[0].weight).isEqualTo(20.0)
        assertThat(warmupSets[0].reps).isEqualTo(12)
        assertThat(workingSets[0].weight).isEqualTo(60.0)
        assertThat(workingSets[0].reps).isEqualTo(10)
    }

    @Test
    fun `parses multiple exercises`() {
        val json = """
        {
          "exercise_plans": [
            {
              "exercise_id": "chest_barbell_bench_press",
              "warmup_sets": [{"weight": 20, "reps": 12, "set_number": 1}],
              "working_sets": [{"weight": 60, "reps": 10, "set_number": 1}],
              "rest_seconds": 120
            },
            {
              "exercise_id": "chest_dumbbell_fly",
              "warmup_sets": [],
              "working_sets": [{"weight": 10, "reps": 12, "set_number": 1}],
              "rest_seconds": 60
            }
          ]
        }
        """.trimIndent()

        val plan = parser.parse(json, testExercises)

        assertThat(plan.exercises).hasSize(2)
        assertThat(plan.exercises[0].stableId).isEqualTo("chest_barbell_bench_press")
        assertThat(plan.exercises[1].stableId).isEqualTo("chest_dumbbell_fly")
    }

    @Test
    fun `throws AiPlanException on completely invalid JSON`() {
        assertThrows<AiPlanException> {
            parser.parse("not json at all", testExercises)
        }
    }

    @Test
    fun `throws AiPlanException on empty exercise plans`() {
        val json = """{"exercise_plans": []}"""

        assertThrows<AiPlanException> {
            parser.parse(json, testExercises)
        }
    }

    @Test
    fun `throws AiPlanException when no exercises match request`() {
        val json = """
        {
          "exercise_plans": [
            {
              "exercise_id": "unknown_exercise",
              "warmup_sets": [],
              "working_sets": [{"weight": 50, "reps": 10, "set_number": 1}],
              "rest_seconds": 90
            }
          ]
        }
        """.trimIndent()

        assertThrows<AiPlanException> {
            parser.parse(json, testExercises)
        }
    }

    @Test
    fun `skips unrecognized exercise IDs but keeps valid ones`() {
        val json = """
        {
          "exercise_plans": [
            {
              "exercise_id": "chest_barbell_bench_press",
              "warmup_sets": [],
              "working_sets": [{"weight": 60, "reps": 10, "set_number": 1}],
              "rest_seconds": 120
            },
            {
              "exercise_id": "totally_unknown",
              "warmup_sets": [],
              "working_sets": [{"weight": 50, "reps": 10, "set_number": 1}],
              "rest_seconds": 90
            }
          ]
        }
        """.trimIndent()

        val plan = parser.parse(json, testExercises)

        assertThat(plan.exercises).hasSize(1)
        assertThat(plan.exercises[0].stableId).isEqualTo("chest_barbell_bench_press")
    }

    @Test
    fun `clamps rest seconds to valid range`() {
        val json = """
        {
          "exercise_plans": [
            {
              "exercise_id": "chest_barbell_bench_press",
              "warmup_sets": [],
              "working_sets": [{"weight": 60, "reps": 10, "set_number": 1}],
              "rest_seconds": 500
            }
          ]
        }
        """.trimIndent()

        val plan = parser.parse(json, testExercises)

        assertThat(plan.exercises[0].restSeconds).isEqualTo(300)
    }

    @Test
    fun `handles missing optional fields with defaults`() {
        val json = """
        {
          "exercise_plans": [
            {
              "exercise_id": "chest_barbell_bench_press",
              "working_sets": [{"weight": 60, "reps": 10}]
            }
          ]
        }
        """.trimIndent()

        val plan = parser.parse(json, testExercises)

        assertThat(plan.exercises).hasSize(1)
        val exercise = plan.exercises[0]
        assertThat(exercise.restSeconds).isEqualTo(90) // default
        assertThat(exercise.notes).isNull()
        // warmup_sets defaults to empty
        assertThat(exercise.sets.filter { it.setType == SetType.WARMUP }).isEmpty()
    }

    @Test
    fun `coerces negative weight to zero`() {
        val json = """
        {
          "exercise_plans": [
            {
              "exercise_id": "chest_barbell_bench_press",
              "warmup_sets": [],
              "working_sets": [{"weight": -10, "reps": 10, "set_number": 1}],
              "rest_seconds": 90
            }
          ]
        }
        """.trimIndent()

        val plan = parser.parse(json, testExercises)

        assertThat(plan.exercises[0].sets[0].weight).isEqualTo(0.0)
    }

    @Test
    fun `coerces reps to valid range`() {
        val json = """
        {
          "exercise_plans": [
            {
              "exercise_id": "chest_barbell_bench_press",
              "warmup_sets": [],
              "working_sets": [
                {"weight": 60, "reps": 0, "set_number": 1},
                {"weight": 60, "reps": 100, "set_number": 2}
              ],
              "rest_seconds": 90
            }
          ]
        }
        """.trimIndent()

        val plan = parser.parse(json, testExercises)

        assertThat(plan.exercises[0].sets[0].reps).isEqualTo(1) // clamped to min
        assertThat(plan.exercises[0].sets[1].reps).isEqualTo(50) // clamped to max
    }
}
