package com.mate.subsmate.domain.repository

import com.mate.subsmate.data.local.entities.SubscriptionEntity
import com.mate.subsmate.data.local.entities.PaymentHistoryEntity
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun getAllActiveSubscriptions(): Flow<List<SubscriptionEntity>>
    fun getEstimatedMonthlyTotal(): Flow<Double?>
    fun getSubscriptionById(id: Long): Flow<SubscriptionEntity?>
    suspend fun insertSubscription(subscription: SubscriptionEntity)
    suspend fun updateSubscription(subscription: SubscriptionEntity)
    suspend fun deleteSubscription(subscription: SubscriptionEntity)

    // Payment History
    suspend fun recordPayment(payment: PaymentHistoryEntity)
    suspend fun undoPayment(subscriptionId: Long)
    fun getAllPayments(): Flow<List<PaymentHistoryEntity>>
}
