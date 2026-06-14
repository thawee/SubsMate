package com.mate.subsmate.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mate.subsmate.domain.model.BillingCycle
import com.mate.subsmate.domain.model.PaymentType

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val price: Double,
    val currency: String = "THB",
    val categoryId: Int,
    val billingCycle: BillingCycle,
    val paymentType: PaymentType = PaymentType.AUTO_PAY,
    val customCycleDays: Int? = null,
    val firstBillingDate: Long, // Timestamp in milliseconds
    val nextBillingDate: Long,  // Calculated timestamp
    val isTrial: Boolean = false,
    val trialEndDate: Long? = null,
    val reminderDaysBefore: Int = 1,
    val iconResId: String? = null,
    val colorHex: String? = null,
    val isActive: Boolean = true,
    val notes: String? = null,
    val lastNotifiedDate: Long? = null,
    val isVariablePrice: Boolean = false,
    val totalInstallments: Int? = null,
    val currentInstallment: Int = 0,
    val totalLoanAmount: Double? = null,
    val interestRate: Double? = null,
    val extraPrincipalPaid: Double = 0.0
)
