package com.mate.subsmate.ui.add_subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mate.subsmate.data.local.entities.SubscriptionEntity
import com.mate.subsmate.domain.model.BillingCycle
import com.mate.subsmate.domain.model.PaymentType
import com.mate.subsmate.domain.model.ServiceTemplate
import com.mate.subsmate.domain.repository.SubscriptionRepository
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
    val price: String = "",
    val categoryId: Int = 1,
    val billingCycle: BillingCycle = BillingCycle.MONTHLY,
    val paymentType: PaymentType = PaymentType.AUTO_PAY,
    val firstBillingDate: Long = System.currentTimeMillis(),
    val isTrial: Boolean = false,
    val trialEndDate: Long? = null,
    val reminderDaysBefore: Int = 1,
    val colorHex: String = "#6200EE",
    val iconName: String = "category",
    val isSaved: Boolean = false,
    val selectedTemplate: ServiceTemplate? = null,
    val isVariablePrice: Boolean = false,
    val isLoan: Boolean = false,
    val totalInstallments: String = "",
    val currentInstallment: String = "",
    val totalLoanAmount: String = "",
    val interestRate: String = "",
    val extraPrincipalPaid: String = ""
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
                _uiState.update {
                    it.copy(
                        id = sub.id,
                        name = sub.name,
                        price = if (sub.isVariablePrice && sub.price == 0.0) "" else sub.price.toString(),
                        categoryId = sub.categoryId,
                        billingCycle = sub.billingCycle,
                        paymentType = sub.paymentType,
                        firstBillingDate = sub.firstBillingDate,
                        isTrial = sub.isTrial,
                        trialEndDate = sub.trialEndDate,
                        reminderDaysBefore = sub.reminderDaysBefore,
                        colorHex = sub.colorHex ?: "#6200EE",
                        iconName = sub.iconResId ?: "category",
                        isVariablePrice = sub.isVariablePrice,
                        isLoan = sub.totalInstallments != null,
                        totalInstallments = sub.totalInstallments?.toString() ?: "",
                        currentInstallment = sub.currentInstallment.toString(),
                        totalLoanAmount = sub.totalLoanAmount?.toString() ?: "",
                        interestRate = sub.interestRate?.toString() ?: "",
                        extraPrincipalPaid = if (sub.extraPrincipalPaid == 0.0) "" else sub.extraPrincipalPaid.toString()
                    )
                }
                isLoaded = true
            }
        }
    }

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName, selectedTemplate = null) }
    }

    fun onPriceChange(newPrice: String) {
        _uiState.update { it.copy(price = newPrice, selectedTemplate = null) }
    }

    fun onCategoryChange(newCategoryId: Int) {
        _uiState.update { it.copy(categoryId = newCategoryId) }
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
                trialEndDate = if (isTrial) it.trialEndDate ?: (System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L) else null
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
        _uiState.update { it.copy(extraPrincipalPaid = value) }
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
        val isPriceValid = state.isVariablePrice || state.price.toDoubleOrNull() != null
        val totalInstallmentsVal = if (state.isLoan) state.totalInstallments.toIntOrNull() else null
        val currentInstallmentVal = if (state.isLoan) state.currentInstallment.toIntOrNull() ?: 0 else 0
        val totalLoanAmountVal = if (state.isLoan) state.totalLoanAmount.toDoubleOrNull() else null
        val interestRateVal = if (state.isLoan) state.interestRate.toDoubleOrNull() else null
        val extraPrincipalPaidVal = if (state.isLoan) state.extraPrincipalPaid.toDoubleOrNull() ?: 0.0 else 0.0

        val isLoanValid = !state.isLoan || (
            totalInstallmentsVal != null && totalInstallmentsVal > 0 &&
            currentInstallmentVal >= 0 && currentInstallmentVal <= totalInstallmentsVal &&
            totalLoanAmountVal != null && totalLoanAmountVal > 0.0 &&
            (interestRateVal == null || interestRateVal >= 0.0) &&
            extraPrincipalPaidVal >= 0.0
        )
        if (state.name.isBlank() || !isPriceValid || !isLoanValid) return

        viewModelScope.launch {
            // If editing, we might want to preserve lastNotifiedDate
            val existingSub = if (state.id != 0L) repository.getSubscriptionById(state.id).firstOrNull() else null
            
            val nextBilling = if (state.isTrial && state.trialEndDate != null) {
                state.trialEndDate
            } else {
                calculateNextBillingDate(state.firstBillingDate, state.billingCycle)
            }

            val finalPrice = if (state.isVariablePrice) {
                state.price.toDoubleOrNull() ?: 0.0
            } else {
                state.price.toDouble()
            }

            val isLoanCompleted = state.isLoan && totalInstallmentsVal != null && currentInstallmentVal >= totalInstallmentsVal
            val activeStatus = if (isLoanCompleted) {
                false
            } else if (existingSub != null) {
                if (state.isLoan && totalInstallmentsVal != null && currentInstallmentVal < totalInstallmentsVal) {
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
                price = finalPrice,
                currency = currency,
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
                totalInstallments = totalInstallmentsVal,
                currentInstallment = currentInstallmentVal,
                totalLoanAmount = totalLoanAmountVal,
                interestRate = interestRateVal,
                extraPrincipalPaid = extraPrincipalPaidVal
            )
            repository.insertSubscription(entity)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    private fun calculateNextBillingDate(firstDate: Long, cycle: BillingCycle): Long {
        val now = System.currentTimeMillis()
        if (firstDate > now) return firstDate

        val calendar = Calendar.getInstance().apply { timeInMillis = firstDate }
        while (calendar.timeInMillis < now) {
            when (cycle) {
                BillingCycle.MONTHLY -> calendar.add(Calendar.MONTH, 1)
                BillingCycle.YEARLY -> calendar.add(Calendar.YEAR, 1)
                BillingCycle.CUSTOM -> calendar.add(Calendar.DAY_OF_YEAR, 30) // Default for custom for now
            }
        }
        return calendar.timeInMillis
    }
}
