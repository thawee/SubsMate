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
    val selectedTemplate: ServiceTemplate? = null
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
                        price = sub.price.toString(),
                        categoryId = sub.categoryId,
                        billingCycle = sub.billingCycle,
                        paymentType = sub.paymentType,
                        firstBillingDate = sub.firstBillingDate,
                        isTrial = sub.isTrial,
                        trialEndDate = sub.trialEndDate,
                        reminderDaysBefore = sub.reminderDaysBefore,
                        colorHex = sub.colorHex ?: "#6200EE",
                        iconName = sub.iconResId ?: "category"
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

    fun onTemplateSelected(template: ServiceTemplate) {
        _uiState.update {
            it.copy(
                name = template.name,
                price = template.monthlyPrice?.toString() ?: template.yearlyPrice?.toString() ?: "",
                billingCycle = if (template.monthlyPrice != null) BillingCycle.MONTHLY else BillingCycle.YEARLY,
                categoryId = template.categoryId,
                colorHex = template.colorHex,
                iconName = template.iconName,
                selectedTemplate = template
            )
        }
    }

    fun saveSubscription() {
        val state = _uiState.value
        if (state.name.isBlank() || state.price.toDoubleOrNull() == null) return

        viewModelScope.launch {
            // If editing, we might want to preserve lastNotifiedDate
            val existingSub = if (state.id != 0L) repository.getSubscriptionById(state.id).firstOrNull() else null
            
            val nextBilling = if (state.isTrial && state.trialEndDate != null) {
                state.trialEndDate
            } else {
                calculateNextBillingDate(state.firstBillingDate, state.billingCycle)
            }

            val entity = SubscriptionEntity(
                id = state.id,
                name = state.name,
                price = state.price.toDouble(),
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
                lastNotifiedDate = existingSub?.lastNotifiedDate
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
