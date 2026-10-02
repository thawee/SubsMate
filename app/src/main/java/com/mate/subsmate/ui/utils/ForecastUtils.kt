package com.mate.subsmate.ui.utils

import com.mate.subsmate.data.local.entities.SubscriptionEntity
import java.time.Instant
import java.time.ZoneId

data class SpendingForecast(
    val amountsByCategory: Map<Int, Double> = emptyMap(),
    val overdueAmount: Double = 0.0,
    val overdueCharges: Int = 0,
    val hasUnknownAmounts: Boolean = false,
    val usesVariableAmounts: Boolean = false
) {
    val total: Double get() = amountsByCategory.values.sum()
}

object ForecastUtils {
    fun nextTwelveMonths(
        subscriptions: List<SubscriptionEntity>,
        currency: String,
        now: Long = System.currentTimeMillis()
    ): SpendingForecast {
        val zone = ZoneId.systemDefault()
        val startDate = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val start = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = startDate.plusYears(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val categoryAmounts = mutableMapOf<Int, Double>()
        var overdueAmount = 0.0
        var overdueCharges = 0
        var hasUnknownAmounts = false
        var usesVariableAmounts = false

        subscriptions.asSequence()
            .filter { it.isActive && it.currency.equals(currency, ignoreCase = true) }
            .forEach { sub ->
                var chargeDate = sub.nextBillingDate
                var remaining = sub.totalInstallments?.let { (it - sub.currentInstallment).coerceAtLeast(0) }
                val unknownPrice = sub.isVariablePrice && sub.price <= 0.0

                while (chargeDate < end && (remaining == null || remaining > 0)) {
                    if (unknownPrice) {
                        hasUnknownAmounts = true
                    } else if (sub.price > 0.0) {
                        if (sub.isVariablePrice) usesVariableAmounts = true
                        if (chargeDate < start) {
                            overdueAmount += sub.price
                            overdueCharges++
                        } else {
                            categoryAmounts[sub.categoryId] = (categoryAmounts[sub.categoryId] ?: 0.0) + sub.price
                        }
                    }
                    remaining = remaining?.minus(1)
                    chargeDate = BillingUtils.advanceByOneCycle(chargeDate, sub.billingCycle, sub.customCycleDays)
                }
            }

        return SpendingForecast(categoryAmounts, overdueAmount, overdueCharges, hasUnknownAmounts, usesVariableAmounts)
    }
}
