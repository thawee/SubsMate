package com.mate.subsmate.ui.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mate.subsmate.data.local.entities.SubscriptionEntity
import com.mate.subsmate.domain.repository.SubscriptionRepository
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
    val sortOption: SortOption = SortOption.NEXT_DATE
)

class SubscriptionsViewModel(
    private val repository: SubscriptionRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _sortOption = MutableStateFlow(SortOption.NEXT_DATE)

    val uiState: StateFlow<SubscriptionsUiState> = combine(
        repository.getAllActiveSubscriptions(),
        _searchQuery,
        _sortOption
    ) { subs, query, sort ->
        val filtered = subs.filter { sub ->
            query.isBlank() || sub.name.contains(query, ignoreCase = true)
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
            sortOption = sort
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

    fun deleteSubscription(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            repository.deleteSubscription(subscription)
        }
    }
}
