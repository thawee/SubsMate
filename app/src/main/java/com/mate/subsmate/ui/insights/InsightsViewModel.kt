package com.mate.subsmate.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mate.subsmate.domain.repository.SubscriptionRepository
import com.mate.subsmate.ui.utils.CategoryUtils
import kotlinx.coroutines.flow.*

data class CategorySpend(
    val categoryName: String,
    val amount: Double,
    val percentage: Float,
    val colorHex: String
)

data class MonthlySpend(
    val monthName: String,
    val amount: Double
)

data class InsightsUiState(
    val categoryBreakdown: List<CategorySpend> = emptyList(),
    val totalMonthlySpend: Double = 0.0,
    val monthlyHistory: List<MonthlySpend> = emptyList(),
    val isLoading: Boolean = true
)

class InsightsViewModel(
    private val repository: SubscriptionRepository
) : ViewModel() {

    val uiState: StateFlow<InsightsUiState> = combine(
        repository.getAllActiveSubscriptions(),
        repository.getEstimatedMonthlyTotal(),
        repository.getAllPayments()
    ) { subs, total, payments ->
        val monthlyTotal = total ?: 0.0
        val breakdown = CategoryUtils.calculateCategoryBreakdown(subs, monthlyTotal)
        val history = calculateMonthlyHistory(payments)
        
        InsightsUiState(
            categoryBreakdown = breakdown,
            totalMonthlySpend = monthlyTotal,
            monthlyHistory = history,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InsightsUiState()
    )

    private fun calculateMonthlyHistory(payments: List<com.mate.subsmate.data.local.entities.PaymentHistoryEntity>): List<MonthlySpend> {
        if (payments.isEmpty()) return emptyList()

        val calendar = java.util.Calendar.getInstance()
        val format = java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault())

        val grouped = payments.groupBy { payment ->
            calendar.timeInMillis = payment.paymentDate
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH)
            year * 12 + month
        }

        val sortedKeys = grouped.keys.sorted().takeLast(6)

        return sortedKeys.map { key ->
            val monthVal = key % 12
            val yearVal = key / 12
            calendar.set(yearVal, monthVal, 1)
            val monthName = format.format(calendar.time)
            
            val totalAmount = grouped[key]?.sumOf { it.amount } ?: 0.0
            MonthlySpend(monthName = monthName, amount = totalAmount)
        }
    }
}
