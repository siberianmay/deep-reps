package com.deepreps.core.domain.util

import com.deepreps.core.domain.model.enums.Equipment
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class WeightStepProviderTest {

    @Test
    fun `barbell increment is 2_5 kg`() {
        assertThat(WeightStepProvider.getIncrementKg(Equipment.BARBELL)).isEqualTo(2.5)
    }

    @Test
    fun `dumbbell increment is 2_5 kg`() {
        assertThat(WeightStepProvider.getIncrementKg(Equipment.DUMBBELL)).isEqualTo(2.5)
    }

    @Test
    fun `cable increment is 5_0 kg`() {
        assertThat(WeightStepProvider.getIncrementKg(Equipment.CABLE)).isEqualTo(5.0)
    }

    @Test
    fun `machine increment is 5_0 kg`() {
        assertThat(WeightStepProvider.getIncrementKg(Equipment.MACHINE)).isEqualTo(5.0)
    }

    @Test
    fun `bodyweight increment is 0`() {
        assertThat(WeightStepProvider.getIncrementKg(Equipment.BODYWEIGHT)).isEqualTo(0.0)
    }

    @Test
    fun `kettlebell increment is 4_0 kg`() {
        assertThat(WeightStepProvider.getIncrementKg(Equipment.KETTLEBELL)).isEqualTo(4.0)
    }

    @Test
    fun `band increment is 0`() {
        assertThat(WeightStepProvider.getIncrementKg(Equipment.BAND)).isEqualTo(0.0)
    }

    @Test
    fun `ez bar increment is 2_5 kg`() {
        assertThat(WeightStepProvider.getIncrementKg(Equipment.EZ_BAR)).isEqualTo(2.5)
    }

    @Test
    fun `trap bar increment is 2_5 kg`() {
        assertThat(WeightStepProvider.getIncrementKg(Equipment.TRAP_BAR)).isEqualTo(2.5)
    }

    @ParameterizedTest
    @EnumSource(Equipment::class)
    @DisplayName("Every equipment type has a non-negative increment")
    fun `all equipment types return non-negative increment`(equipment: Equipment) {
        assertThat(WeightStepProvider.getIncrementKg(equipment)).isAtLeast(0.0)
    }

    @Test
    fun `roundDown with barbell rounds to 2_5 kg increments`() {
        assertThat(WeightStepProvider.roundDown(83.7, Equipment.BARBELL)).isEqualTo(82.5)
    }

    @Test
    fun `roundDown with machine rounds to 5_0 kg increments`() {
        assertThat(WeightStepProvider.roundDown(37.0, Equipment.MACHINE)).isEqualTo(35.0)
    }

    @Test
    fun `roundDown with bodyweight returns original weight`() {
        assertThat(WeightStepProvider.roundDown(75.3, Equipment.BODYWEIGHT)).isEqualTo(75.3)
    }

    @Test
    fun `roundDown with exact increment returns same value`() {
        assertThat(WeightStepProvider.roundDown(80.0, Equipment.BARBELL)).isEqualTo(80.0)
    }

    @Test
    fun `roundDown with kettlebell rounds to 4_0 kg increments`() {
        assertThat(WeightStepProvider.roundDown(15.0, Equipment.KETTLEBELL)).isEqualTo(12.0)
    }
}
