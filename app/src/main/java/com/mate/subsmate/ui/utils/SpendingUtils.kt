package com.mate.subsmate.ui.utils

import com.mate.subsmate.data.local.entities.PaymentHistoryEntity
import com.mate.subsmate.data.local.entities.SubscriptionEntity
import com.mate.subsmate.domain.model.BillingCycle
import com.mate.subsmate.ui.insights.MonthlySpend
import com.mate.subsmate.ui.insights.SubscriptionSpend
import com.mate.subsmate.ui.insights.TimelineMonth
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object SpendingUtils {
    fun monthlyAmount(sub: SubscriptionEntity): Double = when (sub.billingCycle) {
        BillingCycle.MONTHLY -> sub.price
        BillingCycle.YEARLY -> sub.price / 12.0
        BillingCycle.CUSTOM -> sub.price * 30.0 / (sub.customCycleDays?.coerceAtLeast(1) ?: 30)
    }

    fun monthlyTotal(subs: List<SubscriptionEntity>, currency: String): Double = subs
        .filter { it.currency.equals(currency, ignoreCase = true) }
        .sumOf(::monthlyAmount)

    fun monthlyHistory(
        payments: List<PaymentHistoryEntity>,
        currency: String,
        now: Long = System.currentTimeMillis()
    ): List<MonthlySpend> {
        val zone = ZoneId.systemDefault()
        val currentMonth = YearMonth.from(Instant.ofEpochMilli(now).atZone(zone))
        val firstMonth = currentMonth.minusMonths(5)
        val totals = payments.asSequence()
            .filter { it.currency.equals(currency, ignoreCase = true) }
            .groupBy { YearMonth.from(Instant.ofEpochMilli(it.paymentDate).atZone(zone)) }
            .mapValues { (_, monthPayments) -> monthPayments.sumOf { it.amount } }
        val format = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())

        return (0L..5L).map { offset ->
            val month = firstMonth.plusMonths(offset)
            MonthlySpend(monthName = month.format(format), amount = totals[month] ?: 0.0)
        }
    }

    /** Six months of actual payments through the current month, then forecast charges through 11 months ahead. */
    fun timeline(
        payments: List<PaymentHistoryEntity>,
        forecastByMonth: Map<YearMonth, Double>,
        currency: String,
        now: Long = System.currentTimeMillis()
    ): List<TimelineMonth> {
        val zone = ZoneId.systemDefault()
        val currentMonth = YearMonth.from(Instant.ofEpochMilli(now).atZone(zone))
        val paidByMonth = payments.asSequence()
            .filter { it.currency.equals(currency, ignoreCase = true) }
            .groupBy { YearMonth.from(Instant.ofEpochMilli(it.paymentDate).atZone(zone)) }
            .mapValues { (_, monthPayments) -> monthPayments.sumOf { it.amount } }
        val format = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())

        return (-5L..11L).map { offset ->
            val month = currentMonth.plusMonths(offset)
            val label = month.format(format) + if (month.monthValue == 1) " '${month.year % 100}" else ""
            TimelineMonth(
                label = label,
                paid = if (offset <= 0) paidByMonth[month] ?: 0.0 else 0.0,
                forecast = if (offset >= 0) forecastByMonth[month] ?: 0.0 else 0.0,
                isCurrent = offset == 0L
            )
        }
    }

    /** Subscriptions ranked by [amountOf], highest first; zero or missing amounts are left out. */
    fun rankSubscriptions(
        subs: List<SubscriptionEntity>,
        nextTwelveMonths: Map<Long, Double>,
        amountOf: (SubscriptionEntity) -> Double?
    ): List<SubscriptionSpend> = subs.mapNotNull { sub ->
        val amount = amountOf(sub)?.takeIf { it > 0.0 } ?: return@mapNotNull null
        SubscriptionSpend(
            id = sub.id,
            name = sub.name,
            categoryId = sub.categoryId,
            colorHex = CategoryUtils.colorHex(sub.categoryId),
            amount = amount,
            nextTwelveMonths = nextTwelveMonths[sub.id] ?: 0.0
        )
    }.sortedByDescending { it.amount }
}
