package com.mate.subsmate.ui.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mate.subsmate.data.local.entities.SubscriptionEntity
import com.mate.subsmate.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SubscriptionsUiState(
    val subscriptions: List<SubscriptionEntity> = emptyList(),
    val isLoading: Boolean = true
)

class SubscriptionsViewModel(
    private val repository: SubscriptionRepository
) : ViewModel() {

    val uiState: StateFlow<SubscriptionsUiState> = repository.getAllActiveSubscriptions()
        .map { SubscriptionsUiState(subscriptions = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SubscriptionsUiState()
        )

    fun deleteSubscription(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            repository.deleteSubscription(subscription)
        }
    }
}
