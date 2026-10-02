package com.mate.subsmate.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mate.subsmate.domain.repository.SubscriptionRepository
import com.mate.subsmate.ui.utils.CategoryUtils
import com.mate.subsmate.ui.utils.ForecastUtils
import com.mate.subsmate.ui.utils.SpendingForecast
import com.mate.subsmate.ui.utils.SpendingUtils
import kotlinx.coroutines.flow.*

data class CategorySpend(
    val categoryName: String,
    val amount: Double,
    val percentage: Float,
    val colorHex: String,
    val categoryId: Int = -1
)

data class SubscriptionSpend(
    val id: Long,
    val name: String,
    val categoryId: Int,
    val colorHex: String,
    val amount: Double,
    val nextTwelveMonths: Double
)

data class TimelineMonth(
    val label: String,
    val paid: Double,
    val forecast: Double,
    val isCurrent: Boolean
) {
    val total: Double get() = paid + forecast
}

data class MonthlySpend(
    val monthName: String,
    val amount: Double
)

enum class InsightsPeriod { MONTHLY, NEXT_TWELVE_MONTHS }

data class InsightsUiState(
    val selectedCurrency: String = "THB",
    val availableCurrencies: List<String> = emptyList(),
    val categoryBreakdown: List<CategorySpend> = emptyList(),
    val totalMonthlySpend: Double = 0.0,
    val forecast: SpendingForecast = SpendingForecast(),
    val forecastBreakdown: List<CategorySpend> = emptyList(),
    val selectedPeriod: InsightsPeriod = InsightsPeriod.MONTHLY,
    val monthlyRanking: List<SubscriptionSpend> = emptyList(),
    val forecastRanking: List<SubscriptionSpend> = emptyList(),
    val timeline: List<TimelineMonth> = emptyList(),
    val isLoading: Boolean = true
)

class InsightsViewModel(
    private val repository: SubscriptionRepository
) : ViewModel() {
    private val selectedCurrency = MutableStateFlow("THB")
    private val selectedPeriod = MutableStateFlow(InsightsPeriod.MONTHLY)

    val uiState: StateFlow<InsightsUiState> = combine(
        repository.getAllActiveSubscriptions(),
        repository.getAllPayments(),
        selectedCurrency,
        selectedPeriod
    ) { subs, payments, currency, period ->
        val currencySubs = subs.filter { it.currency.equals(currency, ignoreCase = true) }
        val currencyPayments = payments.filter { it.currency.equals(currency, ignoreCase = true) }
        val monthlyTotal = SpendingUtils.monthlyTotal(currencySubs, currency)
        val breakdown = CategoryUtils.calculateCategoryBreakdown(currencySubs, monthlyTotal)
        val forecast = ForecastUtils.nextTwelveMonths(currencySubs, currency)
        val timeline = SpendingUtils.timeline(currencyPayments, forecast.amountsByMonth, currency)
        
        InsightsUiState(
            selectedCurrency = currency,
            availableCurrencies = (subs.map { it.currency } + payments.map { it.currency } + currency).distinct().sorted(),
            categoryBreakdown = breakdown,
            totalMonthlySpend = monthlyTotal,
            forecast = forecast,
            forecastBreakdown = CategoryUtils.fromCategoryAmounts(forecast.amountsByCategory),
            selectedPeriod = period,
            monthlyRanking = SpendingUtils.rankSubscriptions(currencySubs, forecast.amountsBySubscription, SpendingUtils::monthlyAmount),
            forecastRanking = SpendingUtils.rankSubscriptions(currencySubs, forecast.amountsBySubscription) {
                forecast.amountsBySubscription[it.id]
            },
            timeline = if (timeline.any { it.total > 0.0 }) timeline else emptyList(),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InsightsUiState()
    )

    fun setCurrency(currency: String) {
        selectedCurrency.value = currency
    }

    fun setPeriod(period: InsightsPeriod) {
        selectedPeriod.value = period
    }
}
