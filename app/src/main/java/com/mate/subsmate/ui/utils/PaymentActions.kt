package com.mate.subsmate.ui.utils

import com.mate.subsmate.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.firstOrNull

object PaymentActions {
    suspend fun undoLastPayment(repository: SubscriptionRepository, subscriptionId: Long): Boolean {
        val sub = repository.getSubscriptionById(subscriptionId).firstOrNull() ?: return false
        val payment = repository.getLastPaymentForSubscription(subscriptionId)
        if (payment == null && (sub.isActive || sub.totalInstallments == null || sub.currentInstallment == 0)) return false
        if (payment != null) repository.undoPayment(subscriptionId)
        repository.updateSubscription(
            sub.copy(
                nextBillingDate = payment?.billingPeriodStart
                    ?: BillingUtils.revertNextDate(sub.nextBillingDate, sub.billingCycle, sub.customCycleDays),
                currentInstallment = if (sub.totalInstallments != null) (sub.currentInstallment - 1).coerceAtLeast(0) else sub.currentInstallment,
                isActive = true,
                lastNotifiedDate = null
            )
        )
        return true
    }
}
