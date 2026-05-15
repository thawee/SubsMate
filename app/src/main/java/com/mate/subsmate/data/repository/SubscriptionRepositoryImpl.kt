package com.mate.subsmate.data.repository

import com.mate.subsmate.data.local.dao.SubscriptionDao
import com.mate.subsmate.data.local.dao.PaymentDao
import com.mate.subsmate.data.local.entities.SubscriptionEntity
import com.mate.subsmate.data.local.entities.PaymentHistoryEntity
import com.mate.subsmate.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow

class SubscriptionRepositoryImpl(
    private val subscriptionDao: SubscriptionDao,
    private val paymentDao: PaymentDao
) : SubscriptionRepository {
    override fun getAllActiveSubscriptions(): Flow<List<SubscriptionEntity>> =
        subscriptionDao.getAllActiveSubscriptions()

    override fun getEstimatedMonthlyTotal(): Flow<Double?> =
        subscriptionDao.getEstimatedMonthlyTotal()

    override fun getSubscriptionById(id: Long): Flow<SubscriptionEntity?> =
        subscriptionDao.getSubscriptionById(id)

    override suspend fun insertSubscription(subscription: SubscriptionEntity) =
        subscriptionDao.insertSubscription(subscription)

    override suspend fun updateSubscription(subscription: SubscriptionEntity) =
        subscriptionDao.updateSubscription(subscription)

    override suspend fun deleteSubscription(subscription: SubscriptionEntity) =
        subscriptionDao.deleteSubscription(subscription)

    override suspend fun recordPayment(payment: PaymentHistoryEntity) =
        paymentDao.insertPayment(payment)

    override suspend fun undoPayment(subscriptionId: Long) =
        paymentDao.deleteLastPaymentForSubscription(subscriptionId)

    override fun getAllPayments(): Flow<List<PaymentHistoryEntity>> =
        paymentDao.getAllPayments()
}
