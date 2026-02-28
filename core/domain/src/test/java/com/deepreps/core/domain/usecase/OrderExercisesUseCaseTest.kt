package com.deepreps.core.domain.usecase

import com.deepreps.core.domain.model.Exercise
import com.deepreps.core.domain.model.enums.Difficulty
import com.deepreps.core.domain.model.enums.Equipment
import com.deepreps.core.domain.model.enums.MovementType
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OrderExercisesUseCaseTest {

    private lateinit var useCase: OrderExercisesUseCase

    @BeforeEach
    fun setup() {
        useCase = OrderExercisesUseCase()
    }

    @Test
    fun `compounds appear before isolations`() {
        val isolation = makeExercise(
            id = 1,
            name = "Leg Extension",
            movementType = MovementType.ISOLATION,
            primaryGroupId = 1,
            orderPriority = 50,
        )
        val compound = makeExercise(
            id = 2,
            name = "Barbell Squat",
            movementType = MovementType.COMPOUND,
            primaryGroupId = 1,
            orderPriority = 10,
        )

        val result = useCase(listOf(isolation, compound))

        assertThat(result.map { it.name }).containsExactly(
            "Barbell Squat",
            "Leg Extension",
        ).inOrder()
    }

    @Test
    fun `within compounds, lower orderPriority comes first`() {
        val chestPress = makeExercise(
            id = 1,
            name = "Bench Press",
            movementType = MovementType.COMPOUND,
            primaryGroupId = 4,
            orderPriority = 40,
        )
        val squat = makeExercise(
            id = 2,
            name = "Barbell Squat",
            movementType = MovementType.COMPOUND,
            primaryGroupId = 1,
            orderPriority = 10,
        )
        val row = makeExercise(
            id = 3,
            name = "Barbell Row",
            movementType = MovementType.COMPOUND,
            primaryGroupId = 3,
            orderPriority = 30,
        )

        val result = useCase(listOf(chestPress, squat, row))

        assertThat(result.map { it.name }).containsExactly(
            "Barbell Squat",
            "Barbell Row",
            "Bench Press",
        ).inOrder()
    }

    @Test
    fun `core exercises always last regardless of compound or isolation status`() {
        val coreCompound = makeExercise(
            id = 1,
            name = "Hanging Leg Raise",
            movementType = MovementType.COMPOUND,
            primaryGroupId = 7,
            orderPriority = 70,
        )
        val armIsolation = makeExercise(
            id = 2,
            name = "Barbell Curl",
            movementType = MovementType.ISOLATION,
            primaryGroupId = 6,
            orderPriority = 60,
        )
        val chestCompound = makeExercise(
            id = 3,
            name = "Bench Press",
            movementType = MovementType.COMPOUND,
            primaryGroupId = 4,
            orderPriority = 40,
        )

        val result = useCase(listOf(coreCompound, armIsolation, chestCompound))

        assertThat(result.map { it.name }).containsExactly(
            "Bench Press",
            "Barbell Curl",
            "Hanging Leg Raise",
        ).inOrder()
    }

    @Test
    fun `within same priority, advanced exercises come before beginner`() {
        val beginner = makeExercise(
            id = 1,
            name = "Lat Pulldown",
            movementType = MovementType.COMPOUND,
            primaryGroupId = 3,
            orderPriority = 30,
            difficulty = Difficulty.BEGINNER,
        )
        val advanced = makeExercise(
            id = 2,
            name = "Pull-Up",
            movementType = MovementType.COMPOUND,
            primaryGroupId = 3,
            orderPriority = 30,
            difficulty = Difficulty.INTERMEDIATE,
        )

        val result = useCase(listOf(beginner, advanced))

        assertThat(result.map { it.name }).containsExactly(
            "Pull-Up",
            "Lat Pulldown",
        ).inOrder()
    }

    @Test
    fun `empty list returns empty list`() {
        val result = useCase(emptyList())
        assertThat(result).isEmpty()
    }

    @Test
    fun `single exercise returns unchanged`() {
        val exercise = makeExercise(id = 1, name = "Squat", primaryGroupId = 1)
        val result = useCase(listOf(exercise))
        assertThat(result).containsExactly(exercise)
    }

    @Test
    fun `interleaves equal-sized muscle groups via round-robin`() {
        val chest1 = makeExercise(id = 1, name = "Bench Press", primaryGroupId = 4, orderPriority = 40)
        val chest2 = makeExercise(id = 2, name = "Incline Press", primaryGroupId = 4, orderPriority = 41)
        val chest3 = makeExercise(
            id = 3,
            name = "Dumbbell Fly",
            primaryGroupId = 4,
            orderPriority = 42,
            movementType = MovementType.ISOLATION
        )
        val back1 = makeExercise(id = 4, name = "Barbell Row", primaryGroupId = 3, orderPriority = 30)
        val back2 = makeExercise(id = 5, name = "Lat Pulldown", primaryGroupId = 3, orderPriority = 31)
        val back3 = makeExercise(
            id = 6,
            name = "Face Pull",
            primaryGroupId = 3,
            orderPriority = 32,
            movementType = MovementType.ISOLATION
        )

        val result = useCase(listOf(chest1, back1, chest2, back2, chest3, back3))

        // Both groups have 3 exercises. Back has lower first orderPriority (30 vs 40)
        // so back group comes first in tie-breaking. Round-robin: back, chest, back, chest, back, chest.
        assertThat(result.map { it.name }).containsExactly(
            "Barbell Row",
            "Bench Press",
            "Lat Pulldown",
            "Incline Press",
            "Face Pull",
            "Dumbbell Fly",
        ).inOrder()
    }

    @Test
    fun `interleaves unequal groups - larger group leads`() {
        val back1 = makeExercise(id = 1, name = "Barbell Row", primaryGroupId = 3, orderPriority = 30)
        val back2 = makeExercise(id = 2, name = "Lat Pulldown", primaryGroupId = 3, orderPriority = 31)
        val back3 = makeExercise(id = 3, name = "Cable Row", primaryGroupId = 3, orderPriority = 32)
        val back4 = makeExercise(
            id = 4,
            name = "Face Pull",
            primaryGroupId = 3,
            orderPriority = 33,
            movementType = MovementType.ISOLATION
        )
        val chest1 = makeExercise(id = 5, name = "Bench Press", primaryGroupId = 4, orderPriority = 40)

        val result = useCase(listOf(chest1, back1, back2, back3, back4))

        // Back group (4) is larger, chest group (1). Round-robin:
        // Round 1: back1, chest1. Round 2: back2 (chest exhausted). Round 3: back3. Round 4: back4.
        assertThat(result.map { it.name }).containsExactly(
            "Barbell Row",
            "Bench Press",
            "Lat Pulldown",
            "Cable Row",
            "Face Pull",
        ).inOrder()
    }

    @Test
    fun `three muscle groups interleave correctly`() {
        val legs1 = makeExercise(id = 1, name = "Squat", primaryGroupId = 1, orderPriority = 10)
        val legs2 = makeExercise(id = 2, name = "Leg Press", primaryGroupId = 1, orderPriority = 11)
        val back1 = makeExercise(id = 3, name = "Row", primaryGroupId = 3, orderPriority = 30)
        val back2 = makeExercise(id = 4, name = "Pulldown", primaryGroupId = 3, orderPriority = 31)
        val chest1 = makeExercise(id = 5, name = "Bench", primaryGroupId = 4, orderPriority = 40)

        val result = useCase(listOf(chest1, legs1, back1, legs2, back2))

        // Groups by size: legs(2), back(2), chest(1). Tie-break legs vs back: legs first (priority 10 < 30).
        // Round 1: Squat, Row, Bench. Round 2: Leg Press, Pulldown.
        assertThat(result.map { it.name }).containsExactly(
            "Squat",
            "Row",
            "Bench",
            "Leg Press",
            "Pulldown",
        ).inOrder()
    }

    @Test
    fun `within-group sort puts compounds before isolations`() {
        val isolation = makeExercise(
            id = 1,
            name = "Fly",
            primaryGroupId = 4,
            movementType = MovementType.ISOLATION,
            orderPriority = 40,
        )
        val compound = makeExercise(
            id = 2,
            name = "Bench",
            primaryGroupId = 4,
            movementType = MovementType.COMPOUND,
            orderPriority = 41,
        )

        val result = useCase(listOf(isolation, compound))

        // Compound comes first despite higher orderPriority, because within-group
        // sort is compound-first, then orderPriority.
        assertThat(result.map { it.name }).containsExactly("Bench", "Fly").inOrder()
    }

    @Test
    fun `interleaved non-core with core always at end`() {
        val chest = makeExercise(id = 1, name = "Bench", primaryGroupId = 4, orderPriority = 40)
        val back = makeExercise(id = 2, name = "Row", primaryGroupId = 3, orderPriority = 30)
        val core = makeExercise(
            id = 3,
            name = "Plank",
            primaryGroupId = 7,
            orderPriority = 70,
            movementType = MovementType.ISOLATION,
            difficulty = Difficulty.BEGINNER
        )

        val result = useCase(listOf(core, chest, back))

        // Non-core interleaved: Row (priority 30), Bench (priority 40). Core last.
        assertThat(result.map { it.name }).containsExactly("Row", "Bench", "Plank").inOrder()
    }

    @Test
    fun `core isolations sorted by difficulty`() {
        val plank = makeExercise(
            id = 1,
            name = "Plank",
            movementType = MovementType.ISOLATION,
            primaryGroupId = 7,
            orderPriority = 70,
            difficulty = Difficulty.BEGINNER,
        )
        val dragonFlag = makeExercise(
            id = 2,
            name = "Dragon Flag",
            movementType = MovementType.ISOLATION,
            primaryGroupId = 7,
            orderPriority = 70,
            difficulty = Difficulty.ADVANCED,
        )
        val pallofPress = makeExercise(
            id = 3,
            name = "Pallof Press",
            movementType = MovementType.ISOLATION,
            primaryGroupId = 7,
            orderPriority = 70,
            difficulty = Difficulty.INTERMEDIATE,
        )

        val result = useCase(listOf(plank, dragonFlag, pallofPress))

        assertThat(result.map { it.name }).containsExactly(
            "Dragon Flag",
            "Pallof Press",
            "Plank",
        ).inOrder()
    }

    private fun makeExercise(
        id: Long = 1,
        name: String = "Test Exercise",
        movementType: MovementType = MovementType.COMPOUND,
        primaryGroupId: Long = 1,
        orderPriority: Int = 10,
        difficulty: Difficulty = Difficulty.BEGINNER,
    ): Exercise = Exercise(
        id = id,
        stableId = "test_${name.lowercase().replace(" ", "_")}",
        name = name,
        description = "",
        equipment = Equipment.BARBELL,
        movementType = movementType,
        difficulty = difficulty,
        primaryGroupId = primaryGroupId,
        secondaryMuscles = emptyList(),
        tips = emptyList(),
        pros = emptyList(),
        displayOrder = id.toInt(),
        orderPriority = orderPriority,
        supersetTags = emptyList(),
        autoProgramMinLevel = 1,
    )
}
