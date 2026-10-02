package com.mate.subsmate

import com.mate.subsmate.data.local.entities.SubscriptionEntity
import com.mate.subsmate.domain.model.BillingCycle
import com.mate.subsmate.ui.utils.CategoryUtils
import com.mate.subsmate.ui.utils.ForecastUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ForecastUtilsTest {
    private val today = date(2026, 10, 2)

    @Test fun forecastsEachBillingCycleWithinEndExclusiveWindow() {
        val subs = listOf(
            sub(100.0, BillingCycle.MONTHLY, date(2026, 10, 2), categoryId = 1),
            sub(1200.0, BillingCycle.YEARLY, date(2026, 11, 2), categoryId = 2),
            sub(50.0, BillingCycle.CUSTOM, date(2026, 10, 2), categoryId = 3, customDays = 30)
        )

        val forecast = ForecastUtils.nextTwelveMonths(subs, "THB", today)

        assertEquals(1200.0, forecast.amountsByCategory[1]!!, 0.001)
        assertEquals(1200.0, forecast.amountsByCategory[2]!!, 0.001)
        assertEquals(650.0, forecast.amountsByCategory[3]!!, 0.001)
        assertEquals(3050.0, forecast.total, 0.001)
    }

    @Test fun excludesOtherCurrenciesAndInactiveSubscriptions() {
        val subs = listOf(
            sub(10.0, BillingCycle.YEARLY, today, currency = "USD"),
            sub(20.0, BillingCycle.YEARLY, today, active = false),
            sub(30.0, BillingCycle.YEARLY, today)
        )

        assertEquals(30.0, ForecastUtils.nextTwelveMonths(subs, "THB", today).total, 0.001)
    }

    @Test fun stopsAtRemainingLoanInstallments() {
        val loan = sub(100.0, BillingCycle.MONTHLY, today).copy(totalInstallments = 5, currentInstallment = 3)
        assertEquals(200.0, ForecastUtils.nextTwelveMonths(listOf(loan), "THB", today).total, 0.001)
    }

    @Test fun keepsOverdueChargesOutsideFutureTotal() {
        val sub = sub(100.0, BillingCycle.MONTHLY, date(2026, 9, 2))
            .copy(totalInstallments = 2)

        val forecast = ForecastUtils.nextTwelveMonths(listOf(sub), "THB", today)

        assertEquals(100.0, forecast.overdueAmount, 0.001)
        assertEquals(1, forecast.overdueCharges)
        assertEquals(100.0, forecast.total, 0.001)
    }

    @Test fun usesTrialEndAsFirstScheduledCharge() {
        val trial = sub(80.0, BillingCycle.YEARLY, date(2026, 10, 9))
            .copy(isTrial = true, trialEndDate = date(2026, 10, 9))
        assertEquals(80.0, ForecastUtils.nextTwelveMonths(listOf(trial), "THB", today).total, 0.001)
    }

    @Test fun marksUnknownVariableAmountsWithoutInventingCharges() {
        val variable = sub(0.0, BillingCycle.MONTHLY, today).copy(isVariablePrice = true)
        val known = sub(25.0, BillingCycle.YEARLY, today, categoryId = 2)
        val knownVariable = sub(50.0, BillingCycle.YEARLY, today, categoryId = 3).copy(isVariablePrice = true)

        val forecast = ForecastUtils.nextTwelveMonths(listOf(variable, known, knownVariable), "THB", today)

        assertTrue(forecast.hasUnknownAmounts)
        assertTrue(forecast.usesVariableAmounts)
        assertEquals(75.0, forecast.total, 0.001)
        assertFalse(forecast.amountsByCategory.containsKey(1))
    }

    @Test fun groupsChargesByCategoryAndUsesLocalCalendarBoundary() {
        val first = sub(10.0, BillingCycle.YEARLY, today)
        val second = sub(20.0, BillingCycle.YEARLY, date(2027, 10, 2))
        val third = sub(30.0, BillingCycle.YEARLY, date(2027, 10, 1))
        val forecast = ForecastUtils.nextTwelveMonths(listOf(first, second, third), "THB", today)
        val categories = CategoryUtils.fromCategoryAmounts(forecast.amountsByCategory)

        assertEquals(40.0, forecast.total, 0.001)
        assertEquals(1, categories.size)
        assertEquals(1f, categories.single().percentage, 0.001f)
    }

    private fun sub(
        price: Double,
        cycle: BillingCycle,
        nextDate: Long,
        categoryId: Int = 1,
        currency: String = "THB",
        active: Boolean = true,
        customDays: Int? = null
    ) = SubscriptionEntity(
        name = "Test",
        price = price,
        currency = currency,
        categoryId = categoryId,
        billingCycle = cycle,
        customCycleDays = customDays,
        firstBillingDate = nextDate,
        nextBillingDate = nextDate,
        isActive = active
    )

    private fun date(year: Int, month: Int, day: Int): Long = LocalDate.of(year, month, day)
        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
