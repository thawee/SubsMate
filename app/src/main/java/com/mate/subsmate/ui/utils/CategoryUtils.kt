package com.mate.subsmate.ui.utils

import com.mate.subsmate.data.local.entities.SubscriptionEntity
import com.mate.subsmate.domain.model.BillingCycle
import com.mate.subsmate.domain.model.CategoryDefaults
import com.mate.subsmate.ui.insights.CategorySpend

object CategoryUtils {
    fun calculateCategoryBreakdown(subs: List<SubscriptionEntity>, total: Double): List<CategorySpend> {
        if (total <= 0) return emptyList()

        val spendByCategoryId = subs.groupBy { it.categoryId }
            .mapValues { (_, categorySubs) ->
                categorySubs.sumOf { sub ->
                    when (sub.billingCycle) {
                        BillingCycle.MONTHLY -> sub.price
                        BillingCycle.YEARLY -> sub.price / 12
                        BillingCycle.CUSTOM -> sub.price * 30.0 / (sub.customCycleDays?.coerceAtLeast(1) ?: 30)
                    }
                }
            }

        return spendByCategoryId.map { (catId, amount) ->
            val category = CategoryDefaults.categories.find { it.id == catId }
            CategorySpend(
                categoryName = category?.name ?: "Other",
                amount = amount,
                percentage = (amount / total).toFloat(),
                colorHex = category?.colorHex ?: "#808080"
            )
        }.sortedByDescending { it.amount }
    }
}
