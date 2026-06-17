package com.mate.subsmate.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payment_history",
    foreignKeys = [
        ForeignKey(
            entity = SubscriptionEntity::class,
            parentColumns = ["id"],
            childColumns = ["subscriptionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("subscriptionId"),
        Index("billingPeriodStart")
    ]
)
data class PaymentHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subscriptionId: Long,
    val subscriptionName: String,
    val amount: Double,
    val currency: String,
    val paymentDate: Long,
    val billingPeriodStart: Long,
    val billingPeriodEnd: Long
)
