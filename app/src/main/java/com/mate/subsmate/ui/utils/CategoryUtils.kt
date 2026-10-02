package com.mate.subsmate.ui.utils

import com.mate.subsmate.data.local.entities.SubscriptionEntity
import com.mate.subsmate.domain.model.CategoryDefaults
import com.mate.subsmate.ui.insights.CategorySpend

object CategoryUtils {
    fun colorHex(categoryId: Int): String =
        CategoryDefaults.categories.find { it.id == categoryId }?.colorHex ?: "#808080"

    fun calculateCategoryBreakdown(subs: List<SubscriptionEntity>, total: Double): List<CategorySpend> {
        if (total <= 0) return emptyList()

        val spendByCategoryId = subs.groupBy { it.categoryId }
            .mapValues { (_, categorySubs) ->
                categorySubs.sumOf(SpendingUtils::monthlyAmount)
            }

        return fromCategoryAmounts(spendByCategoryId)
    }

    fun fromCategoryAmounts(amountsByCategory: Map<Int, Double>): List<CategorySpend> {
        val total = amountsByCategory.values.sum()
        if (total <= 0.0) return emptyList()

        return amountsByCategory.map { (catId, amount) ->
            val category = CategoryDefaults.categories.find { it.id == catId }
            CategorySpend(
                categoryName = category?.name ?: "Other",
                amount = amount,
                percentage = (amount / total).toFloat(),
                colorHex = category?.colorHex ?: "#808080",
                categoryId = catId
            )
        }.sortedByDescending { it.amount }
    }
}
