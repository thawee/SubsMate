package com.mate.subsmate.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mate.subsmate.data.local.entities.SubscriptionEntity
import com.mate.subsmate.data.local.entities.PaymentHistoryEntity
import com.mate.subsmate.domain.repository.SubscriptionRepository
import com.mate.subsmate.domain.model.BillingCycle
import com.mate.subsmate.domain.model.CategoryDefaults
import com.mate.subsmate.ui.insights.CategorySpend
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

data class SubscriptionChargeUiModel(
    val sub: SubscriptionEntity,
    val isRecentlyPaid: Boolean = false,
    val lastPaymentDate: Long? = null
)

data class DashboardUiState(
    val userName: String = "User",
    val monthlyBudget: Double = 0.0,
    val subscriptions: List<SubscriptionEntity> = emptyList(),
    val monthlyTotal: Double = 0.0,
    val yearlyTotal: Double = 0.0,
    val upcomingCharges: List<SubscriptionChargeUiModel> = emptyList(),
    val categoryBreakdown: List<CategorySpend> = emptyList(),
    val isLoading: Boolean = true,
    val dayCriteria: Int = 14
)

class DashboardViewModel(
    private val repository: SubscriptionRepository
) : ViewModel() {

    private val _dayCriteria = MutableStateFlow(14)
    private val _userName = MutableStateFlow("User")
    private val _monthlyBudget = MutableStateFlow(0.0)
    private val _paidVisibilityDays = MutableStateFlow(1)

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getAllActiveSubscriptions(),
        repository.getEstimatedMonthlyTotal(),
        repository.getAllPayments(),
        _dayCriteria,
        _userName,
        _monthlyBudget,
        _paidVisibilityDays
    ) { arrayOfFlows ->
        @Suppress("UNCHECKED_CAST")
        val subs = arrayOfFlows[0] as List<SubscriptionEntity>
        val monthlyTotal = arrayOfFlows[1] as Double?
        val allPayments = arrayOfFlows[2] as List<PaymentHistoryEntity>
        val days = arrayOfFlows[3] as Int
        val name = arrayOfFlows[4] as String
        val budget = arrayOfFlows[5] as Double
        val visibilityDays = arrayOfFlows[6] as Int

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStart = calendar.timeInMillis
        val limit = now + (days * 24 * 60 * 60 * 1000L)
        val total = monthlyTotal ?: 0.0

        // Visibility window for recently paid items
        val visibilityThreshold = todayStart - (visibilityDays * 24 * 60 * 60 * 1000L)

        val chargeModels = subs.map { sub ->
            val recentPayment = allPayments.find { 
                it.subscriptionId == sub.id && it.paymentDate >= visibilityThreshold 
            }
            SubscriptionChargeUiModel(
                sub = sub,
                isRecentlyPaid = recentPayment != null,
                lastPaymentDate = recentPayment?.paymentDate
            )
        }.filter { model ->
            // Include if:
            // 1. It is overdue (nextBillingDate < todayStart)
            // 2. It is due within the window (nextBillingDate <= limit)
            // 3. It was recently paid (so the user sees the confirmation)
            val isOverdue = model.sub.nextBillingDate < todayStart
            val isDueSoon = model.sub.nextBillingDate <= limit
            isOverdue || isDueSoon || model.isRecentlyPaid
        }.sortedBy { it.sub.nextBillingDate }

        DashboardUiState(
            userName = name,
            monthlyBudget = budget,
            subscriptions = subs,
            monthlyTotal = total,
            yearlyTotal = total * 12,
            upcomingCharges = chargeModels,
            categoryBreakdown = calculateCategoryBreakdown(subs, total),
            isLoading = false,
            dayCriteria = days
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )


    fun setDayCriteria(days: Int) {
        _dayCriteria.value = days
    }

    fun setUserName(name: String) {
        _userName.value = name
    }

    fun setMonthlyBudget(budget: Double) {
        _monthlyBudget.value = budget
    }

    fun setPaidVisibilityDays(days: Int) {
        _paidVisibilityDays.value = days
    }

    fun undoPayment(sub: SubscriptionEntity) {
        viewModelScope.launch {
            val previousDate = revertNextDate(sub.nextBillingDate, sub.billingCycle)
            repository.undoPayment(sub.id)
            repository.updateSubscription(sub.copy(nextBillingDate = previousDate))
        }
    }

    private fun revertNextDate(currentDate: Long, cycle: BillingCycle): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentDate }
        when (cycle) {
            BillingCycle.MONTHLY -> calendar.add(Calendar.MONTH, -1)
            BillingCycle.YEARLY -> calendar.add(Calendar.YEAR, -1)
            BillingCycle.CUSTOM -> calendar.add(Calendar.DAY_OF_YEAR, -30)
        }
        return calendar.timeInMillis
    }

    fun markAsPaid(sub: SubscriptionEntity) {
        viewModelScope.launch {
            val nextDate = calculateNextDate(sub.nextBillingDate, sub.billingCycle)

            // Record to history before updating the next billing date
            repository.recordPayment(
                com.mate.subsmate.data.local.entities.PaymentHistoryEntity(
                    subscriptionId = sub.id,
                    subscriptionName = sub.name,
                    amount = sub.price,
                    currency = sub.currency,
                    paymentDate = System.currentTimeMillis(),
                    billingPeriodStart = sub.nextBillingDate,
                    billingPeriodEnd = nextDate
                )
            )

            repository.updateSubscription(sub.copy(nextBillingDate = nextDate))
        }
    }


    private fun calculateNextDate(currentDate: Long, cycle: BillingCycle): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentDate }
        when (cycle) {
            BillingCycle.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            BillingCycle.YEARLY -> calendar.add(Calendar.YEAR, 1)
            BillingCycle.CUSTOM -> calendar.add(Calendar.DAY_OF_YEAR, 30)
        }
        
        // If the calculated next date is STILL in the past (e.g. user missed multiple months), 
        // keep advancing until it's in the future.
        val now = System.currentTimeMillis()
        while (calendar.timeInMillis < now) {
            when (cycle) {
                BillingCycle.MONTHLY -> calendar.add(Calendar.MONTH, 1)
                BillingCycle.YEARLY -> calendar.add(Calendar.YEAR, 1)
                BillingCycle.CUSTOM -> calendar.add(Calendar.DAY_OF_YEAR, 30)
            }
        }
        
        return calendar.timeInMillis
    }

    private fun calculateCategoryBreakdown(subs: List<SubscriptionEntity>, total: Double): List<CategorySpend> {
        if (total <= 0) return emptyList()

        val spendByCategoryId = subs.groupBy { it.categoryId }
            .mapValues { (_, categorySubs) ->
                categorySubs.sumOf { sub ->
                    when (sub.billingCycle) {
                        BillingCycle.MONTHLY -> sub.price
                        BillingCycle.YEARLY -> sub.price / 12
                        BillingCycle.CUSTOM -> 0.0
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
        }.sortedByDescending { it.amount }.take(4) // Only show top 4 on dashboard
    }
}
