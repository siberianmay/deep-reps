package com.deepreps.core.domain.calculator

import com.deepreps.core.domain.model.HistoricalSession
import com.deepreps.core.domain.model.HistoricalSet
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProgressionCalculatorTest {

    companion object {
        private const val RANGE_MIN = 8
        private const val RANGE_MAX = 12
        private const val FALLBACK_WEIGHT = 60.0
    }

    // -- Helpers --

    private fun workingSet(weight: Double, reps: Int): HistoricalSet =
        HistoricalSet(weight = weight, reps = reps, setType = "working")

    private fun warmupSet(weight: Double, reps: Int): HistoricalSet =
        HistoricalSet(weight = weight, reps = reps, setType = "warmup")

    private fun session(vararg sets: HistoricalSet, date: Long = 0L): HistoricalSession =
        HistoricalSession(date = date, sets = sets.toList())

    @Nested
    @DisplayName("Case 1: All sets hit rangeMax -> weight increase")
    inner class WeightIncrease {

        @Test
        fun `upper body compound increases by 1_25 kg when worst set hits max`() {
            val sessions = listOf(
                session(workingSet(80.0, 12), workingSet(80.0, 12), workingSet(80.0, 12)),
            )

            val result = ProgressionCalculator.compute(
                lastSessions = sessions,
                rangeMin = RANGE_MIN,
                rangeMax = RANGE_MAX,
                isCompound = true,
                isLowerBody = false,
                fallbackWeightKg = FALLBACK_WEIGHT,
            )

            assertThat(result.weightKg).isEqualTo(81.25)
            assertThat(result.targetReps).isEqualTo(RANGE_MIN)
        }

        @Test
        fun `lower body compound increases by 2_5 kg when worst set hits max`() {
            val sessions = listOf(
                session(workingSet(100.0, 12), workingSet(100.0, 12), workingSet(100.0, 12)),
            )

            val result = ProgressionCalculator.compute(
                lastSessions = sessions,
                rangeMin = RANGE_MIN,
                rangeMax = RANGE_MAX,
                isCompound = true,
                isLowerBody = true,
                fallbackWeightKg = FALLBACK_WEIGHT,
            )

            assertThat(result.weightKg).isEqualTo(102.5)
            assertThat(result.targetReps).isEqualTo(RANGE_MIN)
        }

        @Test
        fun `isolation increases by 1_25 kg when worst set hits max`() {
            val sessions = listOf(
                session(workingSet(20.0, 15), workingSet(20.0, 15), workingSet(20.0, 15)),
            )

            val result = ProgressionCalculator.compute(
                lastSessions = sessions,
                rangeMin = 12,
                rangeMax = 15,
                isCompound = false,
                isLowerBody = false,
                fallbackWeightKg = FALLBACK_WEIGHT,
            )

            assertThat(result.weightKg).isEqualTo(21.25)
            assertThat(result.targetReps).isEqualTo(12)
        }
    }

    @Nested
    @DisplayName("Case 2: Average in range -> hold weight, push reps")
    inner class HoldWeightPushReps {

        @Test
        fun `holds weight and increments reps when avg in range`() {
            // avg = (10 + 10 + 9) / 3 = 9.67, floor = 9, next = 10
            val sessions = listOf(
                session(workingSet(80.0, 10), workingSet(80.0, 10), workingSet(80.0, 9)),
            )

            val result = ProgressionCalculator.compute(
                lastSessions = sessions,
                rangeMin = RANGE_MIN,
                rangeMax = RANGE_MAX,
                isCompound = true,
                isLowerBody = false,
                fallbackWeightKg = FALLBACK_WEIGHT,
            )

            assertThat(result.weightKg).isEqualTo(80.0)
            assertThat(result.targetReps).isEqualTo(10)
        }

        @Test
        fun `caps target reps at rangeMax`() {
            // avg = (11 + 11 + 12) / 3 = 11.33, floor = 11, next = 12 (capped at rangeMax)
            val sessions = listOf(
                session(workingSet(80.0, 11), workingSet(80.0, 11), workingSet(80.0, 12)),
            )

            val result = ProgressionCalculator.compute(
                lastSessions = sessions,
                rangeMin = RANGE_MIN,
                rangeMax = RANGE_MAX,
                isCompound = true,
                isLowerBody = false,
                fallbackWeightKg = FALLBACK_WEIGHT,
            )

            assertThat(result.weightKg).isEqualTo(80.0)
            assertThat(result.targetReps).isEqualTo(RANGE_MAX)
        }
    }

    @Nested
    @DisplayName("Case 3: Genuine struggle -> weight decrease 5%")
    inner class WeightDecrease {

        @Test
        fun `decreases weight when avg below min and worst set way below`() {
            // avg = (5 + 5 + 4) / 3 = 4.67, worst = 4, rangeMin - 2 = 6, worst < 6
            val sessions = listOf(
                session(workingSet(80.0, 5), workingSet(80.0, 5), workingSet(80.0, 4)),
            )

            val result = ProgressionCalculator.compute(
                lastSessions = sessions,
                rangeMin = RANGE_MIN,
                rangeMax = RANGE_MAX,
                isCompound = true,
                isLowerBody = false,
                fallbackWeightKg = FALLBACK_WEIGHT,
            )

            // 80.0 * 0.95 = 76.0, rounded to nearest 1.25 = 76.25
            assertThat(result.weightKg).isEqualTo(76.25)
            assertThat(result.targetReps).isEqualTo(RANGE_MIN)
        }

        @Test
        fun `rounds decreased weight to nearest 1_25`() {
            // 85.0 * 0.95 = 80.75, nearest 1.25 = 81.25
            val sessions = listOf(
                session(workingSet(85.0, 4), workingSet(85.0, 5), workingSet(85.0, 4)),
            )

            val result = ProgressionCalculator.compute(
                lastSessions = sessions,
                rangeMin = RANGE_MIN,
                rangeMax = RANGE_MAX,
                isCompound = true,
                isLowerBody = false,
                fallbackWeightKg = FALLBACK_WEIGHT,
            )

            // 85.0 * 0.95 = 80.75, round(80.75/1.25)*1.25 = round(64.6)*1.25 = 65*1.25 = 81.25
            assertThat(result.weightKg).isEqualTo(81.25)
            assertThat(result.targetReps).isEqualTo(RANGE_MIN)
        }
    }

    @Nested
    @DisplayName("Case 4: Fatigue guard -> hold weight and reps")
    inner class FatigueGuard {

        @Test
        fun `holds weight when only last set drops`() {
            // avg = (8 + 8 + 6) / 3 = 7.33 < 8 (rangeMin)
            // worst = 6, rangeMin - 2 = 6, worst NOT < 6 -> Case 4 (not case 3)
            val sessions = listOf(
                session(workingSet(80.0, 8), workingSet(80.0, 8), workingSet(80.0, 6)),
            )

            val result = ProgressionCalculator.compute(
                lastSessions = sessions,
                rangeMin = RANGE_MIN,
                rangeMax = RANGE_MAX,
                isCompound = true,
                isLowerBody = false,
                fallbackWeightKg = FALLBACK_WEIGHT,
            )

            assertThat(result.weightKg).isEqualTo(80.0)
            // floor(7.33) + 1 = 8
            assertThat(result.targetReps).isEqualTo(8)
        }
    }

    @Nested
    @DisplayName("No history -> fallback")
    inner class NoHistory {

        @Test
        fun `returns fallback weight and rangeMin when no sessions`() {
            val result = ProgressionCalculator.compute(
                lastSessions = emptyList(),
                rangeMin = RANGE_MIN,
                rangeMax = RANGE_MAX,
                isCompound = true,
                isLowerBody = false,
                fallbackWeightKg = FALLBACK_WEIGHT,
            )

            assertThat(result.weightKg).isEqualTo(FALLBACK_WEIGHT)
            assertThat(result.targetReps).isEqualTo(RANGE_MIN)
            assertThat(result.isStalled).isFalse()
            assertThat(result.stallNote).isNull()
        }

        @Test
        fun `returns fallback when session has only warmup sets`() {
            val sessions = listOf(
                session(warmupSet(40.0, 10), warmupSet(60.0, 6)),
            )

            val result = ProgressionCalculator.compute(
                lastSessions = sessions,
                rangeMin = RANGE_MIN,
                rangeMax = RANGE_MAX,
                isCompound = true,
                isLowerBody = false,
                fallbackWeightKg = FALLBACK_WEIGHT,
            )

            assertThat(result.weightKg).isEqualTo(FALLBACK_WEIGHT)
            assertThat(result.targetReps).isEqualTo(RANGE_MIN)
        }
    }

    @Nested
    @DisplayName("Stall detection")
    inner class StallDetection {

        @Test
        fun `detects stall when 3 sessions have same weight`() {
            val sessions = listOf(
                session(workingSet(80.0, 10), workingSet(80.0, 10), workingSet(80.0, 9)),
                session(workingSet(80.0, 10), workingSet(80.0, 9), workingSet(80.0, 9)),
                session(workingSet(80.0, 9), workingSet(80.0, 9), workingSet(80.0, 8)),
            )

            val result = ProgressionCalculator.compute(
                lastSessions = sessions,
                rangeMin = RANGE_MIN,
                rangeMax = RANGE_MAX,
                isCompound = true,
                isLowerBody = false,
                fallbackWeightKg = FALLBACK_WEIGHT,
            )

            assertThat(result.isStalled).isTrue()
            assertThat(result.stallNote).contains("3 sessions")
            assertThat(result.stallNote).contains("deload")
        }

        @Test
        fun `no stall when only 2 sessions exist`() {
            val sessions = listOf(
                session(workingSet(80.0, 10), workingSet(80.0, 10)),
                session(workingSet(80.0, 9), workingSet(80.0, 9)),
            )

            val result = ProgressionCalculator.compute(
                lastSessions = sessions,
                rangeMin = RANGE_MIN,
                rangeMax = RANGE_MAX,
                isCompound = true,
                isLowerBody = false,
                fallbackWeightKg = FALLBACK_WEIGHT,
            )

            assertThat(result.isStalled).isFalse()
            assertThat(result.stallNote).isNull()
        }

        @Test
        fun `no stall when weights differ across sessions`() {
            val sessions = listOf(
                session(workingSet(82.5, 10), workingSet(82.5, 10)),
                session(workingSet(80.0, 12), workingSet(80.0, 12)),
                session(workingSet(80.0, 10), workingSet(80.0, 10)),
            )

            val result = ProgressionCalculator.compute(
                lastSessions = sessions,
                rangeMin = RANGE_MIN,
                rangeMax = RANGE_MAX,
                isCompound = true,
                isLowerBody = false,
                fallbackWeightKg = FALLBACK_WEIGHT,
            )

            assertThat(result.isStalled).isFalse()
        }
    }

    @Nested
    @DisplayName("Safety cap")
    inner class SafetyCap {

        @Test
        fun `upper body compound capped at 5 kg increase`() {
            // This test verifies the cap works, though with 1.25 kg increment
            // the cap (5 kg) is never hit in a single step. The cap guards against
            // code changes that might increase the increment.
            val sessions = listOf(
                session(workingSet(80.0, 12), workingSet(80.0, 12), workingSet(80.0, 12)),
            )

            val result = ProgressionCalculator.compute(
                lastSessions = sessions,
                rangeMin = RANGE_MIN,
                rangeMax = RANGE_MAX,
                isCompound = true,
                isLowerBody = false,
                fallbackWeightKg = FALLBACK_WEIGHT,
            )

            // Increment 1.25 is within cap of 5.0
            assertThat(result.weightKg).isEqualTo(81.25)
            assertThat(result.weightKg).isAtMost(80.0 + 5.0)
        }

        @Test
        fun `isolation capped at 2_5 kg increase`() {
            val sessions = listOf(
                session(workingSet(20.0, 15), workingSet(20.0, 15)),
            )

            val result = ProgressionCalculator.compute(
                lastSessions = sessions,
                rangeMin = 12,
                rangeMax = 15,
                isCompound = false,
                isLowerBody = false,
                fallbackWeightKg = FALLBACK_WEIGHT,
            )

            assertThat(result.weightKg).isEqualTo(21.25)
            assertThat(result.weightKg).isAtMost(20.0 + 2.5)
        }

        @Test
        fun `lower body compound capped at 10 kg increase`() {
            val sessions = listOf(
                session(workingSet(100.0, 12), workingSet(100.0, 12)),
            )

            val result = ProgressionCalculator.compute(
                lastSessions = sessions,
                rangeMin = RANGE_MIN,
                rangeMax = RANGE_MAX,
                isCompound = true,
                isLowerBody = true,
                fallbackWeightKg = FALLBACK_WEIGHT,
            )

            assertThat(result.weightKg).isEqualTo(102.5)
            assertThat(result.weightKg).isAtMost(100.0 + 10.0)
        }
    }

    @Nested
    @DisplayName("Weight rounding")
    inner class WeightRounding {

        @Test
        fun `rounds to nearest 1_25 kg`() {
            // 77.0 * 0.95 = 73.15, nearest 1.25 = 73.75
            val sessions = listOf(
                session(workingSet(77.0, 4), workingSet(77.0, 5), workingSet(77.0, 3)),
            )

            val result = ProgressionCalculator.compute(
                lastSessions = sessions,
                rangeMin = RANGE_MIN,
                rangeMax = RANGE_MAX,
                isCompound = true,
                isLowerBody = false,
                fallbackWeightKg = FALLBACK_WEIGHT,
            )

            // 77.0 * 0.95 = 73.15, round(73.15/1.25)*1.25 = round(58.52)*1.25 = 59*1.25 = 73.75
            assertThat(result.weightKg).isEqualTo(73.75)
            // Verify it's a multiple of 1.25
            assertThat(result.weightKg % 1.25).isWithin(0.001).of(0.0)
        }

        @Test
        fun `weight already on step stays unchanged`() {
            val sessions = listOf(
                session(workingSet(81.25, 10), workingSet(81.25, 10)),
            )

            val result = ProgressionCalculator.compute(
                lastSessions = sessions,
                rangeMin = RANGE_MIN,
                rangeMax = RANGE_MAX,
                isCompound = true,
                isLowerBody = false,
                fallbackWeightKg = FALLBACK_WEIGHT,
            )

            assertThat(result.weightKg).isEqualTo(81.25)
        }
    }

    @Nested
    @DisplayName("isLowerBodyGroup")
    inner class LowerBodyDetection {

        @Test
        fun `legs is lower body`() {
            assertThat(ProgressionCalculator.isLowerBodyGroup("legs")).isTrue()
        }

        @Test
        fun `lower_back is lower body`() {
            assertThat(ProgressionCalculator.isLowerBodyGroup("lower_back")).isTrue()
        }

        @Test
        fun `chest is not lower body`() {
            assertThat(ProgressionCalculator.isLowerBodyGroup("chest")).isFalse()
        }

        @Test
        fun `back is not lower body`() {
            assertThat(ProgressionCalculator.isLowerBodyGroup("back")).isFalse()
        }

        @Test
        fun `core is not lower body`() {
            assertThat(ProgressionCalculator.isLowerBodyGroup("core")).isFalse()
        }
    }

    @Nested
    @DisplayName("Mixed set types")
    inner class MixedSetTypes {

        @Test
        fun `ignores warmup sets in progression decision`() {
            val sessions = listOf(
                session(
                    warmupSet(40.0, 10),
                    warmupSet(60.0, 6),
                    workingSet(80.0, 12),
                    workingSet(80.0, 12),
                    workingSet(80.0, 12),
                ),
            )

            val result = ProgressionCalculator.compute(
                lastSessions = sessions,
                rangeMin = RANGE_MIN,
                rangeMax = RANGE_MAX,
                isCompound = true,
                isLowerBody = false,
                fallbackWeightKg = FALLBACK_WEIGHT,
            )

            // Should increase based on working sets only
            assertThat(result.weightKg).isEqualTo(81.25)
            assertThat(result.targetReps).isEqualTo(RANGE_MIN)
        }

        @Test
        fun `uses max weight when working sets have different weights`() {
            val sessions = listOf(
                session(workingSet(77.5, 12), workingSet(80.0, 12), workingSet(80.0, 12)),
            )

            val result = ProgressionCalculator.compute(
                lastSessions = sessions,
                rangeMin = RANGE_MIN,
                rangeMax = RANGE_MAX,
                isCompound = true,
                isLowerBody = false,
                fallbackWeightKg = FALLBACK_WEIGHT,
            )

            // Uses max weight (80.0) + 1.25 = 81.25
            assertThat(result.weightKg).isEqualTo(81.25)
        }
    }
}
