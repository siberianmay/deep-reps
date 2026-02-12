package com.deepreps.core.domain.util

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class Estimated1rmCalculatorTest {

    @Nested
    @DisplayName("Epley formula")
    inner class EpleyTests {

        @Test
        fun `100kg x 10 reps returns 133_33`() {
            val result = Estimated1rmCalculator.epley(100.0, 10)
            assertThat(result).isWithin(0.01).of(133.33)
        }

        @Test
        fun `100kg x 1 rep returns 100kg (actual max)`() {
            val result = Estimated1rmCalculator.epley(100.0, 1)
            assertThat(result).isEqualTo(100.0)
        }

        @Test
        fun `100kg x 5 reps returns 116_67`() {
            val result = Estimated1rmCalculator.epley(100.0, 5)
            assertThat(result).isWithin(0.01).of(116.67)
        }

        @ParameterizedTest
        @CsvSource(
            "100.0, 3, 110.0",
            "100.0, 8, 126.67",
            "100.0, 12, 140.0",
            "100.0, 15, 150.0",
            "60.0, 10, 80.0",
        )
        fun `epley parametric test`(weight: Double, reps: Int, expected: Double) {
            val result = Estimated1rmCalculator.epley(weight, reps)
            assertThat(result).isNotNull()
            assertThat(result!!).isWithin(0.01).of(expected)
        }

        @Test
        fun `negative weight returns null`() {
            assertThat(Estimated1rmCalculator.epley(-10.0, 5)).isNull()
        }

        @Test
        fun `zero weight returns null`() {
            assertThat(Estimated1rmCalculator.epley(0.0, 5)).isNull()
        }

        @Test
        fun `zero reps returns null`() {
            assertThat(Estimated1rmCalculator.epley(100.0, 0)).isNull()
        }

        @Test
        fun `31 reps returns null (exceeds 30 limit)`() {
            assertThat(Estimated1rmCalculator.epley(100.0, 31)).isNull()
        }

        @Test
        fun `30 reps is valid`() {
            assertThat(Estimated1rmCalculator.epley(100.0, 30)).isNotNull()
        }
    }

    @Nested
    @DisplayName("Brzycki formula")
    inner class BrzyckiTests {

        @Test
        fun `100kg x 10 reps returns 133_33`() {
            val result = Estimated1rmCalculator.brzycki(100.0, 10)
            assertThat(result).isNotNull()
            // 100 * (36 / (37 - 10)) = 100 * (36/27) = 133.33
            assertThat(result!!).isWithin(0.01).of(133.33)
        }

        @Test
        fun `100kg x 1 rep returns 100kg`() {
            val result = Estimated1rmCalculator.brzycki(100.0, 1)
            assertThat(result).isEqualTo(100.0)
        }

        @Test
        fun `100kg x 5 reps returns 112_5`() {
            // 100 * (36 / (37 - 5)) = 100 * (36/32) = 112.5
            val result = Estimated1rmCalculator.brzycki(100.0, 5)
            assertThat(result).isNotNull()
            assertThat(result!!).isWithin(0.01).of(112.5)
        }

        @Test
        fun `37 reps returns null (division by zero)`() {
            assertThat(Estimated1rmCalculator.brzycki(100.0, 37)).isNull()
        }

        @Test
        fun `36 reps is valid`() {
            assertThat(Estimated1rmCalculator.brzycki(100.0, 36)).isNotNull()
        }

        @Test
        fun `negative weight returns null`() {
            assertThat(Estimated1rmCalculator.brzycki(-10.0, 5)).isNull()
        }
    }

    @Nested
    @DisplayName("calculateWithConfidence")
    inner class ConfidenceTests {

        @Test
        fun `1-5 reps gives HIGH confidence`() {
            val result = Estimated1rmCalculator.calculateWithConfidence(100.0, 3)
            assertThat(result).isNotNull()
            assertThat(result!!.confidence).isEqualTo(Estimated1rmCalculator.Confidence.HIGH)
            assertThat(result.usableForPr).isTrue()
        }

        @Test
        fun `6-10 reps gives MODERATE confidence`() {
            val result = Estimated1rmCalculator.calculateWithConfidence(100.0, 8)
            assertThat(result).isNotNull()
            assertThat(result!!.confidence).isEqualTo(Estimated1rmCalculator.Confidence.MODERATE)
            assertThat(result.usableForPr).isTrue()
        }

        @Test
        fun `11-20 reps gives LOW confidence and not usable for PR`() {
            val result = Estimated1rmCalculator.calculateWithConfidence(100.0, 15)
            assertThat(result).isNotNull()
            assertThat(result!!.confidence).isEqualTo(Estimated1rmCalculator.Confidence.LOW)
            assertThat(result.usableForPr).isFalse()
        }

        @Test
        fun `21+ reps returns null (endurance set)`() {
            val result = Estimated1rmCalculator.calculateWithConfidence(100.0, 21)
            assertThat(result).isNull()
        }

        @Test
        fun `zero reps returns null`() {
            assertThat(Estimated1rmCalculator.calculateWithConfidence(100.0, 0)).isNull()
        }

        @Test
        fun `zero weight returns null`() {
            assertThat(Estimated1rmCalculator.calculateWithConfidence(0.0, 5)).isNull()
        }
    }
}
