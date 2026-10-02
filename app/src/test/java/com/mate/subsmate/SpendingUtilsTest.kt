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

    @Test
    fun timelineSplitsPaidHistoryAndScheduledMonths() {
        val now = timestamp(YearMonth.of(2026, 10))
        val payments = listOf(
            payment("THB", 50.0, YearMonth.of(2026, 5)),
            payment("THB", 30.0, YearMonth.of(2026, 10)),
            payment("THB", 99.0, YearMonth.of(2026, 4)),
            payment("USD", 500.0, YearMonth.of(2026, 9))
        )
        val scheduled = mapOf(
            YearMonth.of(2026, 9) to 999.0,
            YearMonth.of(2026, 10) to 20.0,
            YearMonth.of(2027, 1) to 1200.0,
            YearMonth.of(2027, 10) to 999.0
        )

        val timeline = SpendingUtils.timeline(payments, scheduled, "THB", now)

        assertEquals(17, timeline.size)
        assertEquals(50.0, timeline.first().paid, 0.001)
        assertEquals(0.0, timeline[4].forecast, 0.001)
        assertEquals(30.0, timeline[5].paid, 0.001)
        assertEquals(20.0, timeline[5].forecast, 0.001)
        assertEquals(listOf(5), timeline.indices.filter { timeline[it].isCurrent })
        assertEquals(1200.0, timeline[8].forecast, 0.001)
        assertEquals(true, timeline[8].label.endsWith("'27"))
        assertEquals(1220.0, timeline.sumOf { it.forecast }, 0.001)
    }

    @Test
    fun rankingSortsByAmountAndSkipsZeroOrMissing() {
        val small = subscription("THB", 50.0).copy(id = 1, name = "Small")
        val large = subscription("THB", 300.0).copy(id = 2, name = "Large")
        val unknown = subscription("THB", 0.0).copy(id = 3, name = "Unknown", isVariablePrice = true)
        val nextYear = mapOf(1L to 600.0, 2L to 3600.0)

        val monthly = SpendingUtils.rankSubscriptions(listOf(small, large, unknown), nextYear, SpendingUtils::monthlyAmount)
        assertEquals(listOf("Large", "Small"), monthly.map { it.name })
        assertEquals(3600.0, monthly.first().nextTwelveMonths, 0.001)

        val scheduled = SpendingUtils.rankSubscriptions(listOf(small, large, unknown), mapOf(1L to 600.0)) { mapOf(1L to 600.0)[it.id] }
        assertEquals(listOf("Small"), scheduled.map { it.name })
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
