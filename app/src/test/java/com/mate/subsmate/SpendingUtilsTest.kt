package com.mate.subsmate

import com.mate.subsmate.data.local.entities.PaymentHistoryEntity
import com.mate.subsmate.data.local.entities.SubscriptionEntity
import com.mate.subsmate.domain.model.BillingCycle
import com.mate.subsmate.ui.utils.PercentageUtils
import com.mate.subsmate.ui.utils.SpendingUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneId

class SpendingUtilsTest {
    @Test
    fun totalsNeverMixCurrencies() {
        val thb = subscription("THB", 100.0)
        val usd = subscription("USD", 200.0)

        assertEquals(100.0, SpendingUtils.monthlyTotal(listOf(thb, usd), "THB"), 0.001)
        assertEquals(200.0, SpendingUtils.monthlyTotal(listOf(thb, usd), "USD"), 0.001)
    }

    @Test
    fun historyIncludesMonthsWithoutPaymentsAndExcludesOtherCurrencies() {
        val now = timestamp(YearMonth.of(2026, 10))
        val payments = listOf(
            payment("THB", 50.0, YearMonth.of(2026, 7)),
            payment("THB", 75.0, YearMonth.of(2026, 9)),
            payment("USD", 500.0, YearMonth.of(2026, 8))
        )

        val amounts = SpendingUtils.monthlyHistory(payments, "THB", now).map { it.amount }
        assertEquals(listOf(0.0, 0.0, 50.0, 0.0, 75.0, 0.0), amounts)
    }

    @Test
    fun smallNonzeroCategoryIsNotLabeledZero() {
        assertEquals("<1%", PercentageUtils.label(0.0009f))
        assertEquals("0%", PercentageUtils.label(0f))
    }

    private fun subscription(currency: String, price: Double) = SubscriptionEntity(
        name = currency,
        price = price,
        currency = currency,
        categoryId = 1,
        billingCycle = BillingCycle.MONTHLY,
        firstBillingDate = 0,
        nextBillingDate = 0
    )

    private fun payment(currency: String, amount: Double, month: YearMonth) = PaymentHistoryEntity(
        subscriptionId = 1,
        subscriptionName = "Test",
        amount = amount,
        currency = currency,
        paymentDate = timestamp(month),
        billingPeriodStart = 0,
        billingPeriodEnd = 0
    )

    private fun timestamp(month: YearMonth) = month.atDay(10).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
