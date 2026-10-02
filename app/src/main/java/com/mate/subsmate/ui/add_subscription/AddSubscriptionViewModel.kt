package com.mate.subsmate.ui.add_subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mate.subsmate.data.local.entities.SubscriptionEntity
import com.mate.subsmate.domain.model.BillingCycle
import com.mate.subsmate.domain.model.PaymentType
import com.mate.subsmate.domain.model.ServiceTemplate
import com.mate.subsmate.domain.repository.SubscriptionRepository
import com.mate.subsmate.ui.utils.TimeUtils
import com.mate.subsmate.ui.utils.BillingUtils
import com.mate.subsmate.ui.utils.LoanUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

data class AddSubscriptionUiState(
    val id: Long = 0,
    val name: String = "",
    val notes: String = "",
    val price: String = "",
    val currency: String = "",
    val categoryId: Int = 1,
    val billingCycle: BillingCycle = BillingCycle.MONTHLY,
    val paymentType: PaymentType = PaymentType.AUTO_PAY,
    val firstBillingDate: Long = System.currentTimeMillis(),
    val isTrial: Boolean = false,
    val trialEndDate: Long? = null,
    val reminderDaysBefore: Int = 1,
    val colorHex: String = "#E50914",
    val iconName: String = "play_circle",
    val isSaved: Boolean = false,
    val selectedTemplate: ServiceTemplate? = null,
    val isVariablePrice: Boolean = false,
    val isLoan: Boolean = false,
    val totalInstallments: String = "",
    val currentInstallment: String = "",
    val totalLoanAmount: String = "",
    val interestRate: String = "",
    val extraPrincipalPaid: String = "",
    val extraPerMonth: String = "",
    val customCycleDays: String = "",
    val errorMessage: String? = null,
    val isCreditCard: Boolean = false,
    val statementDayOfMonth: String = "",
    val dueDayOfMonth: String = "",
    val creditLimit: String = "",
    val currentStatementBalance: String = "",
    val minimumPaymentDue: String = "",
    val cardApr: String = ""
)

class AddSubscriptionViewModel(
    private val repository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddSubscriptionUiState())
    val uiState: StateFlow<AddSubscriptionUiState> = _uiState.asStateFlow()
    
    private var isLoaded = false

    fun loadSubscription(id: Long) {
        if (isLoaded) return
        viewModelScope.launch {
            val entity = repository.getSubscriptionById(id).firstOrNull()
            entity?.let { sub ->
                val isBuggyDefault = sub.iconResId == "category" && sub.colorHex == "#6200EE" && sub.categoryId != 99
                val fixedIconName = if (isBuggyDefault) com.mate.subsmate.domain.model.CategoryDefaults.categories.find { it.id == sub.categoryId }?.iconName ?: sub.iconResId else sub.iconResId
                val fixedColorHex = if (isBuggyDefault) com.mate.subsmate.domain.model.CategoryDefaults.categories.find { it.id == sub.categoryId }?.colorHex ?: sub.colorHex else sub.colorHex

                _uiState.update {
                    it.copy(
                        id = sub.id,
                        name = sub.name,
                        notes = sub.notes.orEmpty(),
                        price = if (sub.isVariablePrice && sub.price == 0.0) "" else sub.price.toString(),
                        currency = sub.currency,
                        categoryId = sub.categoryId,
                        billingCycle = sub.billingCycle,
                        paymentType = sub.paymentType,
                        firstBillingDate = sub.firstBillingDate,
                        isTrial = sub.isTrial,
                        trialEndDate = sub.trialEndDate,
                        reminderDaysBefore = sub.reminderDaysBefore,
                        colorHex = fixedColorHex ?: "#E50914",
                        iconName = fixedIconName ?: "play_circle",
                        isVariablePrice = sub.isVariablePrice,
                        isLoan = sub.totalInstallments != null,
                        totalInstallments = sub.totalInstallments?.toString() ?: "",
                        currentInstallment = sub.currentInstallment.toString(),
                        totalLoanAmount = sub.totalLoanAmount?.toString() ?: "",
                        interestRate = sub.interestRate?.toString() ?: "",
                        extraPrincipalPaid = if (sub.extraPrincipalPaid == 0.0) "" else sub.extraPrincipalPaid.toString(),
                        customCycleDays = sub.customCycleDays?.toString() ?: "",
                        isCreditCard = sub.isCreditCard,
                        statementDayOfMonth = sub.statementDayOfMonth?.toString() ?: "",
                        dueDayOfMonth = sub.dueDayOfMonth?.toString() ?: "",
                        creditLimit = sub.creditLimit?.toString() ?: "",
                        currentStatementBalance = sub.currentStatementBalance?.toString() ?: "",
                        minimumPaymentDue = sub.minimumPaymentDue?.toString() ?: "",
                        cardApr = sub.cardApr?.toString() ?: ""
                    )
                }
                isLoaded = true
            }
        }
    }

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName, selectedTemplate = null, errorMessage = null) }
    }

    fun onNotesChange(newNotes: String) {
        _uiState.update { it.copy(notes = newNotes) }
    }

    fun onPriceChange(newPrice: String) {
        _uiState.update { it.copy(price = newPrice, selectedTemplate = null, errorMessage = null) }
    }

    fun onCategoryChange(newCategoryId: Int) {
        _uiState.update { state ->
            if (state.selectedTemplate == null) {
                val category = com.mate.subsmate.domain.model.CategoryDefaults.categories.find { it.id == newCategoryId }
                state.copy(
                    categoryId = newCategoryId,
                    iconName = category?.iconName ?: state.iconName,
                    colorHex = category?.colorHex ?: state.colorHex
                )
            } else {
                state.copy(categoryId = newCategoryId)
            }
        }
    }

    fun onBillingCycleChange(newCycle: BillingCycle) {
        _uiState.update { state ->
            val updatedPrice = if (state.selectedTemplate != null) {
                when (newCycle) {
                    BillingCycle.MONTHLY -> state.selectedTemplate.monthlyPrice?.toString() ?: state.price
                    BillingCycle.YEARLY -> state.selectedTemplate.yearlyPrice?.toString() ?: state.price
                    else -> state.price
                }
            } else {
                state.price
            }
            state.copy(billingCycle = newCycle, price = updatedPrice)
        }
    }

    fun onPaymentTypeChange(newType: PaymentType) {
        _uiState.update { it.copy(paymentType = newType) }
    }

    fun onFirstBillingDateChange(newDate: Long) {
        _uiState.update { it.copy(firstBillingDate = newDate) }
    }

    fun onTrialToggle(isTrial: Boolean) {
        _uiState.update { 
            it.copy(
                isTrial = isTrial,
                trialEndDate = if (isTrial) it.trialEndDate ?: (System.currentTimeMillis() + 7 * TimeUtils.MILLIS_PER_DAY) else null
            ) 
        }
    }

    fun onTrialEndDateChange(newDate: Long) {
        _uiState.update { it.copy(trialEndDate = newDate) }
    }

    fun onReminderChange(daysBefore: Int) {
        _uiState.update { it.copy(reminderDaysBefore = daysBefore) }
    }

    fun onVariablePriceToggle(isVariable: Boolean) {
        _uiState.update { it.copy(isVariablePrice = isVariable) }
    }

    fun onLoanToggle(isLoan: Boolean) {
        _uiState.update { it.copy(isLoan = isLoan) }
    }

    fun onTotalInstallmentsChange(value: String) {
        _uiState.update { it.copy(totalInstallments = value) }
    }

    fun onCurrentInstallmentChange(value: String) {
        _uiState.update { it.copy(currentInstallment = value) }
    }

    fun onTotalLoanAmountChange(value: String) {
        _uiState.update { it.copy(totalLoanAmount = value) }
    }

    fun onInterestRateChange(value: String) {
        _uiState.update { it.copy(interestRate = value) }
    }

    fun onExtraPrincipalPaidChange(value: String) {
        _uiState.update { it.copy(extraPrincipalPaid = value, errorMessage = null) }
    }

    fun onExtraPerMonthChange(value: String) {
        _uiState.update { it.copy(extraPerMonth = value, errorMessage = null) }
    }

    fun onCustomCycleDaysChange(value: String) {
        _uiState.update { it.copy(customCycleDays = value, errorMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onCreditCardToggle(isCreditCard: Boolean) {
        _uiState.update { it.copy(isCreditCard = isCreditCard) }
    }

    fun onStatementDayOfMonthChange(value: String) {
        _uiState.update { it.copy(statementDayOfMonth = value) }
    }

    fun onDueDayOfMonthChange(value: String) {
        _uiState.update { it.copy(dueDayOfMonth = value) }
    }

    fun onCreditLimitChange(value: String) {
        _uiState.update { it.copy(creditLimit = value) }
    }

    fun onCurrentStatementBalanceChange(value: String) {
        _uiState.update { it.copy(currentStatementBalance = value) }
    }

    fun onMinimumPaymentDueChange(value: String) {
        _uiState.update { it.copy(minimumPaymentDue = value) }
    }

    fun onCardAprChange(value: String) {
        _uiState.update { it.copy(cardApr = value) }
    }

    fun onTemplateSelected(template: ServiceTemplate) {
        _uiState.update {
            val isVariable = template.monthlyPrice == null && template.yearlyPrice == null
            it.copy(
                name = template.name,
                price = template.monthlyPrice?.toString() ?: template.yearlyPrice?.toString() ?: "",
                billingCycle = if (template.monthlyPrice != null) BillingCycle.MONTHLY else BillingCycle.YEARLY,
                categoryId = template.categoryId,
                colorHex = template.colorHex,
                iconName = template.iconName,
                selectedTemplate = template,
                isVariablePrice = isVariable
            )
        }
    }

    fun saveSubscription(currency: String) {
        val state = _uiState.value
        val savedCurrency = if (state.id != 0L && state.currency.isNotBlank()) state.currency else currency
        val isPriceValid = state.isVariablePrice || state.price.toDoubleOrNull() != null
        val totalInstallmentsVal = if (state.isLoan) state.totalInstallments.toIntOrNull() else null
        val currentInstallmentVal = if (state.isLoan) state.currentInstallment.toIntOrNull() ?: 0 else 0
        val totalLoanAmountVal = if (state.isLoan) state.totalLoanAmount.toDoubleOrNull() else null
        val interestRateVal = if (state.isLoan) state.interestRate.toDoubleOrNull() else null
        val extraPrincipalPaidVal = if (state.isLoan) state.extraPrincipalPaid.toDoubleOrNull() ?: 0.0 else 0.0
        val customCycleDaysVal = if (state.billingCycle == BillingCycle.CUSTOM) state.customCycleDays.toIntOrNull()?.coerceAtLeast(1) else null

        // For loans: require principal. Terms are optional (auto-calculated if blank).
        val priceVal = state.price.toDoubleOrNull() ?: 0.0
        val isLoanValid = !state.isLoan || (
            totalLoanAmountVal != null && totalLoanAmountVal > 0.0 &&
            priceVal > 0.0 &&
            (interestRateVal == null || interestRateVal >= 0.0) &&
            extraPrincipalPaidVal >= 0.0
        )

        val statementDayVal = state.statementDayOfMonth.toIntOrNull()
        val dueDayVal = state.dueDayOfMonth.toIntOrNull()
        val creditLimitVal = state.creditLimit.toDoubleOrNull()
        val currentStatementBalanceVal = state.currentStatementBalance.toDoubleOrNull()
        val minimumPaymentDueVal = state.minimumPaymentDue.toDoubleOrNull()
        val cardAprVal = state.cardApr.toDoubleOrNull()

        val isCreditCardValid = !state.isCreditCard || (
            statementDayVal != null && statementDayVal in 1..31 &&
            dueDayVal != null && dueDayVal in 1..31
        )

        if (state.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Service name is required") }
            return
        }
        if (!isPriceValid) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid price") }
            return
        }
        if (!isLoanValid) {
            _uiState.update { it.copy(errorMessage = "Please check loan details") }
            return
        }
        if (!isCreditCardValid) {
            _uiState.update { it.copy(errorMessage = "Please enter valid statement and due days (1-31)") }
            return
        }
        if (state.billingCycle == BillingCycle.CUSTOM && customCycleDaysVal == null) {
            _uiState.update { it.copy(errorMessage = "Please enter valid cycle days") }
            return
        }

        viewModelScope.launch {
            val existingSub = if (state.id != 0L) repository.getSubscriptionById(state.id).firstOrNull() else null
            
                val nextBilling = if (existingSub != null &&
                    existingSub.firstBillingDate == state.firstBillingDate &&
                    existingSub.billingCycle == state.billingCycle &&
                    existingSub.customCycleDays == customCycleDaysVal &&
                    existingSub.isTrial == state.isTrial &&
                    existingSub.trialEndDate == state.trialEndDate
                ) {
                    existingSub.nextBillingDate
                } else if (state.isTrial && state.trialEndDate != null) {
                    state.trialEndDate
                } else {
                    val cycleDays = if (state.billingCycle == BillingCycle.CUSTOM) state.customCycleDays.toIntOrNull()?.coerceAtLeast(1) else null
                    BillingUtils.calculateNextDate(state.firstBillingDate, state.billingCycle, cycleDays)
                }

            val finalPrice = if (state.isVariablePrice) {
                state.price.toDoubleOrNull() ?: 0.0
            } else {
                state.price.toDouble()
            }

            // Auto-calculate total installments if not provided
            val effectiveTotalInstallments = totalInstallmentsVal
                ?: if (state.isLoan && totalLoanAmountVal != null && priceVal > 0.0) {
                    LoanUtils.estimateTotalInstallments(totalLoanAmountVal, priceVal, interestRateVal)
                } else null

            val isLoanCompleted = state.isLoan && effectiveTotalInstallments != null && currentInstallmentVal >= effectiveTotalInstallments
            val activeStatus = if (isLoanCompleted) {
                false
            } else if (existingSub != null) {
                if (state.isLoan && effectiveTotalInstallments != null && currentInstallmentVal < effectiveTotalInstallments) {
                    true
                } else {
                    existingSub.isActive
                }
            } else {
                true
            }

            val entity = SubscriptionEntity(
                id = state.id,
                name = state.name,
                notes = state.notes.trim().ifBlank { null },
                price = finalPrice,
                currency = savedCurrency,
                categoryId = state.categoryId,
                billingCycle = state.billingCycle,
                paymentType = state.paymentType,
                firstBillingDate = state.firstBillingDate,
                nextBillingDate = nextBilling,
                isTrial = state.isTrial,
                trialEndDate = state.trialEndDate,
                reminderDaysBefore = state.reminderDaysBefore,
                colorHex = state.colorHex,
                iconResId = state.iconName,
                isActive = activeStatus,
                lastNotifiedDate = existingSub?.lastNotifiedDate,
                isVariablePrice = state.isVariablePrice,
                totalInstallments = effectiveTotalInstallments,
                currentInstallment = currentInstallmentVal,
                totalLoanAmount = totalLoanAmountVal,
                interestRate = interestRateVal,
                extraPrincipalPaid = extraPrincipalPaidVal,
                customCycleDays = customCycleDaysVal,
                isCreditCard = state.isCreditCard,
                statementDayOfMonth = statementDayVal,
                dueDayOfMonth = dueDayVal,
                creditLimit = creditLimitVal,
                currentStatementBalance = currentStatementBalanceVal,
                minimumPaymentDue = minimumPaymentDueVal,
                cardApr = cardAprVal
            )
            val savedId = if (existingSub == null) {
                repository.insertSubscription(entity)
            } else {
                repository.updateSubscription(entity)
                entity.id
            }

            // Sync payment history for loans
            if (state.isLoan && finalPrice > 0.0) {
                val oldInstallments = existingSub?.currentInstallment ?: 0
                val cycleDays = if (state.billingCycle == BillingCycle.CUSTOM) state.customCycleDays.toIntOrNull()?.coerceAtLeast(1) else null

                if (state.id == 0L) {
                    // New subscription — generate all past payment records
                    if (currentInstallmentVal > 0) {
                        var periodStart = state.firstBillingDate
                        for (i in 1..currentInstallmentVal) {
                            val periodEnd = BillingUtils.advanceByOneCycle(periodStart, state.billingCycle, cycleDays)
                            repository.recordPayment(
                                com.mate.subsmate.data.local.entities.PaymentHistoryEntity(
                                    subscriptionId = savedId,
                                    subscriptionName = state.name,
                                    amount = finalPrice,
                                    currency = savedCurrency,
                                    paymentDate = periodEnd,
                                    billingPeriodStart = periodStart,
                                    billingPeriodEnd = periodEnd
                                )
                            )
                            periodStart = periodEnd
                        }
                    }
                } else {
                    // Existing subscription — sync payment history delta
                    val diff = currentInstallmentVal - oldInstallments
                    if (diff > 0) {
                        // Added more installments — append payment records
                        val lastPayment = repository.getLastPaymentForSubscription(entity.id)
                        var periodStart = lastPayment?.billingPeriodEnd ?: state.firstBillingDate
                        for (i in 1..diff) {
                            val periodEnd = BillingUtils.advanceByOneCycle(periodStart, state.billingCycle, cycleDays)
                            repository.recordPayment(
                                com.mate.subsmate.data.local.entities.PaymentHistoryEntity(
                                    subscriptionId = entity.id,
                                    subscriptionName = state.name,
                                    amount = finalPrice,
                                    currency = savedCurrency,
                                    paymentDate = periodEnd,
                                    billingPeriodStart = periodStart,
                                    billingPeriodEnd = periodEnd
                                )
                            )
                            periodStart = periodEnd
                        }
                    } else if (diff < 0) {
                        // Removed installments — delete excess payment records
                        for (i in 1..(-diff)) {
                            repository.undoPayment(entity.id)
                        }
                    }
                }
            }

            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
