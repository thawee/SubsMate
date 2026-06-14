package com.mate.subsmate

import com.mate.subsmate.domain.model.BillingCycle
import com.mate.subsmate.ui.utils.TimeUtils
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class BillingCycleLogicTest {

    private fun calculateNextDate(currentDate: Long, cycle: BillingCycle, customCycleDays: Int? = null): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentDate }
        val cycleDays = customCycleDays?.coerceAtLeast(1) ?: TimeUtils.DEFAULT_CUSTOM_CYCLE_DAYS
        when (cycle) {
            BillingCycle.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            BillingCycle.YEARLY -> calendar.add(Calendar.YEAR, 1)
            BillingCycle.CUSTOM -> calendar.add(Calendar.DAY_OF_YEAR, cycleDays)
        }
        return calendar.timeInMillis
    }

    private fun revertNextDate(currentDate: Long, cycle: BillingCycle, customCycleDays: Int? = null): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentDate }
        when (cycle) {
            BillingCycle.MONTHLY -> calendar.add(Calendar.MONTH, -1)
            BillingCycle.YEARLY -> calendar.add(Calendar.YEAR, -1)
            BillingCycle.CUSTOM -> calendar.add(Calendar.DAY_OF_YEAR, -(customCycleDays?.coerceAtLeast(1) ?: 30))
        }
        return calendar.timeInMillis
    }

    @Test
    fun `calculateNextDate MONTHLY adds one month`() {
        val calendar = Calendar.getInstance().apply {
            set(2025, Calendar.JANUARY, 15, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val result = calculateNextDate(calendar.timeInMillis, BillingCycle.MONTHLY)
        val resultCal = Calendar.getInstance().apply { timeInMillis = result }
        assertEquals(Calendar.FEBRUARY, resultCal.get(Calendar.MONTH))
        assertEquals(15, resultCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `calculateNextDate YEARLY adds one year`() {
        val calendar = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 10, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val result = calculateNextDate(calendar.timeInMillis, BillingCycle.YEARLY)
        val resultCal = Calendar.getInstance().apply { timeInMillis = result }
        assertEquals(2026, resultCal.get(Calendar.YEAR))
        assertEquals(Calendar.MARCH, resultCal.get(Calendar.MONTH))
        assertEquals(10, resultCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `calculateNextDate CUSTOM adds specified days`() {
        val calendar = Calendar.getInstance().apply {
            set(2025, Calendar.JUNE, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val result = calculateNextDate(calendar.timeInMillis, BillingCycle.CUSTOM, customCycleDays = 15)
        val resultCal = Calendar.getInstance().apply { timeInMillis = result }
        assertEquals(Calendar.JUNE, resultCal.get(Calendar.MONTH))
        assertEquals(16, resultCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `calculateNextDate CUSTOM with null cycle days defaults to 30`() {
        val calendar = Calendar.getInstance().apply {
            set(2025, Calendar.JUNE, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val result = calculateNextDate(calendar.timeInMillis, BillingCycle.CUSTOM, customCycleDays = null)
        val resultCal = Calendar.getInstance().apply { timeInMillis = result }
        assertEquals(Calendar.JULY, resultCal.get(Calendar.MONTH))
        assertEquals(1, resultCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `revertNextDate MONTHLY subtracts one month`() {
        val calendar = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 15, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val result = revertNextDate(calendar.timeInMillis, BillingCycle.MONTHLY)
        val resultCal = Calendar.getInstance().apply { timeInMillis = result }
        assertEquals(Calendar.FEBRUARY, resultCal.get(Calendar.MONTH))
        assertEquals(15, resultCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `revertNextDate YEARLY subtracts one year`() {
        val calendar = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 10, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val result = revertNextDate(calendar.timeInMillis, BillingCycle.YEARLY)
        val resultCal = Calendar.getInstance().apply { timeInMillis = result }
        assertEquals(2024, resultCal.get(Calendar.YEAR))
        assertEquals(Calendar.MARCH, resultCal.get(Calendar.MONTH))
    }

    @Test
    fun `revertNextDate CUSTOM subtracts specified days`() {
        val calendar = Calendar.getInstance().apply {
            set(2025, Calendar.JUNE, 30, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val result = revertNextDate(calendar.timeInMillis, BillingCycle.CUSTOM, customCycleDays = 15)
        val resultCal = Calendar.getInstance().apply { timeInMillis = result }
        assertEquals(Calendar.JUNE, resultCal.get(Calendar.MONTH))
        assertEquals(15, resultCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `revertNextDate CUSTOM with null cycle days defaults to 30`() {
        val calendar = Calendar.getInstance().apply {
            set(2025, Calendar.JULY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val result = revertNextDate(calendar.timeInMillis, BillingCycle.CUSTOM, customCycleDays = null)
        val resultCal = Calendar.getInstance().apply { timeInMillis = result }
        assertEquals(Calendar.JUNE, resultCal.get(Calendar.MONTH))
        assertEquals(1, resultCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `revertNextDate and calculateNextDate are inverse operations for MONTHLY`() {
        val original = Calendar.getInstance().apply {
            set(2025, Calendar.JANUARY, 15, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val next = calculateNextDate(original, BillingCycle.MONTHLY)
        val reverted = revertNextDate(next, BillingCycle.MONTHLY)
        assertEquals(original, reverted)
    }

    @Test
    fun `revertNextDate and calculateNextDate are inverse operations for YEARLY`() {
        val original = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 10, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val next = calculateNextDate(original, BillingCycle.YEARLY)
        val reverted = revertNextDate(next, BillingCycle.YEARLY)
        assertEquals(original, reverted)
    }

    @Test
    fun `revertNextDate and calculateNextDate are inverse operations for CUSTOM`() {
        val original = Calendar.getInstance().apply {
            set(2025, Calendar.JUNE, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val cycleDays = 15
        val next = calculateNextDate(original, BillingCycle.CUSTOM, cycleDays)
        val reverted = revertNextDate(next, BillingCycle.CUSTOM, cycleDays)
        assertEquals(original, reverted)
    }
}
