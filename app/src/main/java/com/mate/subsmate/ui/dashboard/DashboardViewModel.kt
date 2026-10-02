package com.mate.subsmate.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mate.subsmate.data.local.entities.SubscriptionEntity
import com.mate.subsmate.data.local.entities.PaymentHistoryEntity
import com.mate.subsmate.domain.repository.SubscriptionRepository
import com.mate.subsmate.domain.model.BillingCycle
import com.mate.subsmate.domain.model.CategoryDefaults
import com.mate.subsmate.ui.insights.CategorySpend
import com.mate.subsmate.ui.insights.MonthlySpend
import com.mate.subsmate.ui.utils.CategoryUtils
import com.mate.subsmate.ui.utils.TimeUtils
import com.mate.subsmate.ui.utils.BillingUtils
import com.mate.subsmate.ui.utils.SpendingUtils
import com.mate.subsmate.ui.utils.PaymentActions
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

data class SubscriptionChargeUiModel(
    val sub: SubscriptionEntity,
    val isRecentlyPaid: Boolean = false,
    val lastPaymentDate: Long? = null,
    val projectedDate: Long? = null,
    val projectedInstallment: Int? = null,
    val projectedBalance: Double? = null,
    val projectedPrincipal: Double? = null,
    val projectedInterest: Double? = null
)

data class DashboardUiState(
    val selectedCurrency: String = "THB",
    val availableCurrencies: List<String> = emptyList(),
    val userName: String = "User",
    val monthlyBudget: Double = 0.0,
    val subscriptions: List<SubscriptionEntity> = emptyList(),
    val monthlyTotal: Double = 0.0,
    val yearlyTotal: Double = 0.0,
    val upcomingCharges: List<SubscriptionChargeUiModel> = emptyList(),
    val recentlyPaid: List<SubscriptionChargeUiModel> = emptyList(),
    val categoryBreakdown: List<CategorySpend> = emptyList(),
    val isLoading: Boolean = true,
    val dayCriteria: Int = 30,
    val activeCount: Int = 0,
    val trialCount: Int = 0,
    val averageCost: Double = 0.0,
    val mostExpensive: SubscriptionEntity? = null,
    val monthlyHistory: List<MonthlySpend> = emptyList()
)

class DashboardViewModel(
    private val repository: SubscriptionRepository
) : ViewModel() {

    private val _dayCriteria = MutableStateFlow(30)
    private val _userName = MutableStateFlow("User")
    private val _monthlyBudget = MutableStateFlow(0.0)
    private val _paidVisibilityDays = MutableStateFlow(1)
    private val _selectedCurrency = MutableStateFlow("THB")

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getAllActiveSubscriptions(),
        repository.getAllSubscriptions(),
        repository.getAllPayments(),
        _dayCriteria,
        _userName,
        _monthlyBudget,
        _paidVisibilityDays,
        _selectedCurrency
    ) { arrayOfFlows ->
        @Suppress("UNCHECKED_CAST")
        val subs = arrayOfFlows[0] as List<SubscriptionEntity>
        val allSubs = arrayOfFlows[1] as List<SubscriptionEntity>
        val allPayments = arrayOfFlows[2] as List<PaymentHistoryEntity>
        val days = arrayOfFlows[3] as Int
        val name = arrayOfFlows[4] as String
        val budget = arrayOfFlows[5] as Double
        val visibilityDays = arrayOfFlows[6] as Int
        val currency = arrayOfFlows[7] as String

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStart = calendar.timeInMillis
        val limit = if (days == -1) {
            // "This Year" — end of current year
            Calendar.getInstance().apply {
                set(Calendar.MONTH, Calendar.DECEMBER)
                set(Calendar.DAY_OF_MONTH, 31)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.timeInMillis
        } else {
            now + (days * TimeUtils.MILLIS_PER_DAY)
        }
        val currencySubs = subs.filter { it.currency.equals(currency, ignoreCase = true) }
        val currencyPayments = allPayments.filter { it.currency.equals(currency, ignoreCase = true) }
        val total = SpendingUtils.monthlyTotal(currencySubs, currency)

        // Visibility window for recently paid items
        val visibilityThreshold = todayStart - (visibilityDays * TimeUtils.MILLIS_PER_DAY)
        val recentPaid = allSubs.mapNotNull { sub ->
            val payment = allPayments.firstOrNull {
                it.subscriptionId == sub.id && it.paymentDate in visibilityThreshold..now
            }
            payment?.let { SubscriptionChargeUiModel(sub, isRecentlyPaid = true, lastPaymentDate = it.paymentDate) }
        }.sortedByDescending { it.lastPaymentDate }

        val chargeModels = if (days == -1) {
            // "This Year" — generate all projected charges for each subscription
            subs.flatMap { sub ->
                // Generate all charge dates from now until end of year
                val charges = mutableListOf<SubscriptionChargeUiModel>()
                var chargeDate = sub.nextBillingDate
                val yearEnd = limit

                // Include overdue
                if (chargeDate < todayStart && (sub.totalInstallments == null || sub.currentInstallment < sub.totalInstallments)) {
                    charges.add(SubscriptionChargeUiModel(sub = sub, projectedDate = chargeDate))
                }

                // Generate future charges within this year
                var monthOffset = 0
                while (chargeDate <= yearEnd) {
                    if (sub.totalInstallments != null && sub.currentInstallment + monthOffset >= sub.totalInstallments) break
                    if (chargeDate >= todayStart) {
                        if (sub.totalInstallments != null && sub.totalLoanAmount != null) {
                            // Loan — calculate projected state at this month
                            val projectedInstallment = sub.currentInstallment + monthOffset
                            val projectedBalance = com.mate.subsmate.ui.utils.LoanUtils.calculateRemainingBalance(
                                initialPrincipal = sub.totalLoanAmount,
                                monthlyPayment = sub.price,
                                annualInterestRate = sub.interestRate,
                                installmentsPaid = projectedInstallment,
                                extraPrincipalPaid = sub.extraPrincipalPaid
                            )
                            val split = com.mate.subsmate.ui.utils.LoanUtils.calculateNextPaymentSplit(
                                initialPrincipal = sub.totalLoanAmount,
                                monthlyPayment = sub.price,
                                annualInterestRate = sub.interestRate,
                                installmentsPaid = projectedInstallment,
                                extraPrincipalPaid = sub.extraPrincipalPaid
                            )
                            charges.add(SubscriptionChargeUiModel(
                                sub = sub,
                                projectedDate = chargeDate,
                                projectedInstallment = projectedInstallment + 1,
                                projectedBalance = projectedBalance,
                                projectedPrincipal = split.principal,
                                projectedInterest = split.interest
                            ))
                        } else {
                            charges.add(SubscriptionChargeUiModel(sub = sub, projectedDate = chargeDate))
                        }
                    }
                    chargeDate = BillingUtils.advanceByOneCycle(chargeDate, sub.billingCycle, sub.customCycleDays)
                    monthOffset++
                }

                charges
            }.sortedBy { it.projectedDate ?: it.sub.nextBillingDate }
        } else {
            // Normal day-based filter
            subs.map { sub -> SubscriptionChargeUiModel(sub = sub) }.filter { model ->
                val isOverdue = model.sub.nextBillingDate < todayStart
                val isDueSoon = model.sub.nextBillingDate <= limit
                isOverdue || isDueSoon
            }.sortedBy { it.sub.nextBillingDate }
        }

        DashboardUiState(
            selectedCurrency = currency,
            availableCurrencies = (allSubs.map { it.currency } + allPayments.map { it.currency } + currency).distinct().sorted(),
            userName = name,
            monthlyBudget = budget,
            subscriptions = subs,
            monthlyTotal = total,
            yearlyTotal = total * 12,
            upcomingCharges = chargeModels,
            recentlyPaid = recentPaid,
            categoryBreakdown = CategoryUtils.calculateCategoryBreakdown(currencySubs, total),
            isLoading = false,
            dayCriteria = days,
            activeCount = subs.size,
            trialCount = subs.count { it.isTrial },
            averageCost = if (currencySubs.isNotEmpty()) total / currencySubs.size else 0.0,
            mostExpensive = subs.maxByOrNull { it.price },
            monthlyHistory = if (currencyPayments.isEmpty()) emptyList() else SpendingUtils.monthlyHistory(currencyPayments, currency)
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

    fun setCurrency(currency: String) {
        _selectedCurrency.value = currency
    }

    fun undoPayment(sub: SubscriptionEntity) {
        viewModelScope.launch {
            PaymentActions.undoLastPayment(repository, sub.id)
        }
    }

    fun markAsPaid(sub: SubscriptionEntity) {
        viewModelScope.launch {
            val current = repository.getSubscriptionById(sub.id).firstOrNull() ?: sub
            val nextDate = BillingUtils.advanceByOneCycle(current.nextBillingDate, current.billingCycle, current.customCycleDays)

            repository.recordPayment(
                com.mate.subsmate.data.local.entities.PaymentHistoryEntity(
                    subscriptionId = current.id,
                    subscriptionName = current.name,
                    amount = current.price,
                    currency = current.currency,
                    paymentDate = System.currentTimeMillis(),
                    billingPeriodStart = current.nextBillingDate,
                    billingPeriodEnd = nextDate
                )
            )

            val isCompleted = current.totalInstallments != null && (current.currentInstallment + 1) >= current.totalInstallments
            val updatedSub = current.copy(
                nextBillingDate = nextDate,
                currentInstallment = if (current.totalInstallments != null) current.currentInstallment + 1 else current.currentInstallment,
                isActive = !isCompleted
            )
            repository.updateSubscription(updatedSub)
        }
    }

}
