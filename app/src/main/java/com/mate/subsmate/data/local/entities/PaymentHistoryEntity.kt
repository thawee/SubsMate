package com.mate.subsmate.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_history")
data class PaymentHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subscriptionId: Long,
    val subscriptionName: String,
    val amount: Double,
    val currency: String,
    val paymentDate: Long, // Timestamp when marked as paid
    val billingPeriodStart: Long,
    val billingPeriodEnd: Long
)
