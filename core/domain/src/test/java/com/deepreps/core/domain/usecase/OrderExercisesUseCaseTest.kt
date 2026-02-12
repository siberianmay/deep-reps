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
