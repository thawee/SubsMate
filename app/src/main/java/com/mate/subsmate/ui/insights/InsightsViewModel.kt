package com.mate.subsmate.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mate.subsmate.data.local.entities.SubscriptionEntity
import com.mate.subsmate.domain.model.BillingCycle
import com.mate.subsmate.domain.model.CategoryDefaults
import com.mate.subsmate.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.*

data class CategorySpend(
    val categoryName: String,
    val amount: Double,
    val percentage: Float,
    val colorHex: String
)

data class InsightsUiState(
    val categoryBreakdown: List<CategorySpend> = emptyList(),
    val totalMonthlySpend: Double = 0.0,
    val isLoading: Boolean = true
)

class InsightsViewModel(
    private val repository: SubscriptionRepository
) : ViewModel() {

    val uiState: StateFlow<InsightsUiState> = combine(
        repository.getAllActiveSubscriptions(),
        repository.getEstimatedMonthlyTotal()
    ) { subs, total ->
        val monthlyTotal = total ?: 0.0
        val breakdown = calculateCategoryBreakdown(subs, monthlyTotal)
        
        InsightsUiState(
            categoryBreakdown = breakdown,
            totalMonthlySpend = monthlyTotal,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InsightsUiState()
    )

    private fun calculateCategoryBreakdown(subs: List<SubscriptionEntity>, total: Double): List<CategorySpend> {
        if (total <= 0) return emptyList()

        val spendByCategoryId = subs.groupBy { it.categoryId }
            .mapValues { (_, categorySubs) ->
                categorySubs.sumOf { sub ->
                    when (sub.billingCycle) {
                        BillingCycle.MONTHLY -> sub.price
                        BillingCycle.YEARLY -> sub.price / 12
                        BillingCycle.CUSTOM -> 0.0 // Simplified
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
