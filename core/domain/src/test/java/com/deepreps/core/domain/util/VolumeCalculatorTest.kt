package com.deepreps.core.domain.util

import com.deepreps.core.domain.model.WorkoutSet
import com.deepreps.core.domain.model.enums.SetStatus
import com.deepreps.core.domain.model.enums.SetType
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class VolumeCalculatorTest {

    @Nested
    @DisplayName("workingSetsForExercise")
    inner class WorkingSetsForExercise {

        @Test
        fun `counts only completed working sets`() {
            val sets = listOf(
                makeSet(type = SetType.WARMUP, status = SetStatus.COMPLETED),
                makeSet(type = SetType.WORKING, status = SetStatus.COMPLETED),
                makeSet(type = SetType.WORKING, status = SetStatus.COMPLETED),
                makeSet(type = SetType.WORKING, status = SetStatus.PLANNED),
            )
            assertThat(VolumeCalculator.workingSetsForExercise(sets)).isEqualTo(2)
        }

        @Test
        fun `returns zero for empty set list`() {
            assertThat(VolumeCalculator.workingSetsForExercise(emptyList())).isEqualTo(0)
        }

        @Test
        fun `excludes skipped sets`() {
            val sets = listOf(
                makeSet(type = SetType.WORKING, status = SetStatus.COMPLETED),
                makeSet(type = SetType.WORKING, status = SetStatus.SKIPPED),
            )
            assertThat(VolumeCalculator.workingSetsForExercise(sets)).isEqualTo(1)
        }

        @Test
        fun `warmup sets are never counted`() {
            val sets = listOf(
                makeSet(type = SetType.WARMUP, status = SetStatus.COMPLETED),
                makeSet(type = SetType.WARMUP, status = SetStatus.COMPLETED),
            )
            assertThat(VolumeCalculator.workingSetsForExercise(sets)).isEqualTo(0)
        }
    }

    @Nested
    @DisplayName("workingSetsForSession")
    inner class WorkingSetsForSession {

        @Test
        fun `sums working sets across exercises`() {
            val exerciseSets = mapOf(
                1L to listOf(
                    makeSet(type = SetType.WORKING, status = SetStatus.COMPLETED),
                    makeSet(type = SetType.WORKING, status = SetStatus.COMPLETED),
                ),
                2L to listOf(
                    makeSet(type = SetType.WARMUP, status = SetStatus.COMPLETED),
                    makeSet(type = SetType.WORKING, status = SetStatus.COMPLETED),
                ),
            )
            assertThat(VolumeCalculator.workingSetsForSession(exerciseSets)).isEqualTo(3)
        }

        @Test
        fun `returns zero for empty map`() {
            assertThat(VolumeCalculator.workingSetsForSession(emptyMap())).isEqualTo(0)
        }
    }

    @Nested
    @DisplayName("tonnage")
    inner class TonnageTests {

        @Test
        fun `calculates weight x reps for completed working sets`() {
            val sets = listOf(
                makeSet(
                    type = SetType.WORKING, status = SetStatus.COMPLETED,
                    actualWeight = 100.0, actualReps = 8,
                ),
                makeSet(
                    type = SetType.WORKING, status = SetStatus.COMPLETED,
                    actualWeight = 100.0, actualReps = 8,
                ),
                makeSet(
                    type = SetType.WORKING, status = SetStatus.COMPLETED,
                    actualWeight = 100.0, actualReps = 6,
                ),
            )
            // 100*8 + 100*8 + 100*6 = 800 + 800 + 600 = 2200
            assertThat(VolumeCalculator.tonnage(sets)).isWithin(0.01).of(2200.0)
        }

        @Test
        fun `excludes warmup sets from tonnage`() {
            val sets = listOf(
                makeSet(
                    type = SetType.WARMUP, status = SetStatus.COMPLETED,
                    actualWeight = 60.0, actualReps = 12,
                ),
                makeSet(
                    type = SetType.WORKING, status = SetStatus.COMPLETED,
                    actualWeight = 100.0, actualReps = 8,
                ),
            )
            assertThat(VolumeCalculator.tonnage(sets)).isWithin(0.01).of(800.0)
        }

        @Test
        fun `excludes incomplete sets from tonnage`() {
            val sets = listOf(
                makeSet(
                    type = SetType.WORKING, status = SetStatus.PLANNED,
                    actualWeight = null, actualReps = null,
                ),
                makeSet(
                    type = SetType.WORKING, status = SetStatus.COMPLETED,
                    actualWeight = 80.0, actualReps = 10,
                ),
            )
            assertThat(VolumeCalculator.tonnage(sets)).isWithin(0.01).of(800.0)
        }

        @Test
        fun `handles null weights gracefully (treats as zero)`() {
            val sets = listOf(
                makeSet(
                    type = SetType.WORKING, status = SetStatus.COMPLETED,
                    actualWeight = null, actualReps = 10,
                ),
            )
            assertThat(VolumeCalculator.tonnage(sets)).isWithin(0.01).of(0.0)
        }

        @Test
        fun `returns zero for empty list`() {
            assertThat(VolumeCalculator.tonnage(emptyList())).isWithin(0.01).of(0.0)
        }
    }

    @Nested
    @DisplayName("sessionTonnage")
    inner class SessionTonnageTests {

        @Test
        fun `sums tonnage across multiple exercises`() {
            val exerciseSets = mapOf(
                1L to listOf(
                    makeSet(
                        type = SetType.WORKING, status = SetStatus.COMPLETED,
                        actualWeight = 100.0, actualReps = 10,
                    ),
                ),
                2L to listOf(
                    makeSet(
                        type = SetType.WORKING, status = SetStatus.COMPLETED,
                        actualWeight = 50.0, actualReps = 12,
                    ),
                ),
            )
            // 100*10 + 50*12 = 1000 + 600 = 1600
            assertThat(VolumeCalculator.sessionTonnage(exerciseSets)).isWithin(0.01).of(1600.0)
        }
    }

    private fun makeSet(
        type: SetType = SetType.WORKING,
        status: SetStatus = SetStatus.COMPLETED,
        actualWeight: Double? = 100.0,
        actualReps: Int? = 10,
    ): WorkoutSet = WorkoutSet(
        id = 0,
        setNumber = 1,
        type = type,
        status = status,
        plannedWeightKg = actualWeight,
        plannedReps = actualReps,
        actualWeightKg = actualWeight,
        actualReps = actualReps,
    )
}
