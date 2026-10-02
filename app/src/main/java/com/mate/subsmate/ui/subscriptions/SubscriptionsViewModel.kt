package com.mate.subsmate.ui.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mate.subsmate.data.local.entities.SubscriptionEntity
import com.mate.subsmate.domain.repository.SubscriptionRepository
import com.mate.subsmate.ui.utils.PaymentActions
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortOption(val label: String) {
    NAME("Name"),
    PRICE_LOW("Price: Low to High"),
    PRICE_HIGH("Price: High to Low"),
    NEXT_DATE("Next Charge Date")
}

data class SubscriptionsUiState(
    val subscriptions: List<SubscriptionEntity> = emptyList(),
    val filteredSubscriptions: List<SubscriptionEntity> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.NEXT_DATE,
    val showCompleted: Boolean = false,
    val completedCount: Int = 0
)

class SubscriptionsViewModel(
    private val repository: SubscriptionRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _sortOption = MutableStateFlow(SortOption.NEXT_DATE)
    private val _showCompleted = MutableStateFlow(false)

    val uiState: StateFlow<SubscriptionsUiState> = combine(
        repository.getAllSubscriptions(),
        _searchQuery,
        _sortOption,
        _showCompleted
    ) { subs, query, sort, showCompleted ->
        val filtered = subs.filter { sub ->
            sub.isActive != showCompleted && (query.isBlank() || sub.name.contains(query, ignoreCase = true))
        }

        val sorted = when (sort) {
            SortOption.NAME -> filtered.sortedBy { it.name.lowercase() }
            SortOption.PRICE_LOW -> filtered.sortedBy { it.price }
            SortOption.PRICE_HIGH -> filtered.sortedByDescending { it.price }
            SortOption.NEXT_DATE -> filtered.sortedBy { it.nextBillingDate }
        }

        SubscriptionsUiState(
            subscriptions = subs,
            filteredSubscriptions = sorted,
            isLoading = false,
            searchQuery = query,
            sortOption = sort,
            showCompleted = showCompleted,
            completedCount = subs.count { !it.isActive }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SubscriptionsUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSortOptionChange(option: SortOption) {
        _sortOption.value = option
    }

    fun setShowCompleted(showCompleted: Boolean) {
        _showCompleted.value = showCompleted
    }

    fun undoFinalPayment(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            PaymentActions.undoLastPayment(repository, subscription.id)
        }
    }

    fun deleteSubscription(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            repository.deleteSubscription(subscription)
        }
    }
}
