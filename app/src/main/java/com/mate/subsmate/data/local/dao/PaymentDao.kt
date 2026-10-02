package com.mate.subsmate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mate.subsmate.data.local.entities.PaymentHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPayment(payment: PaymentHistoryEntity)

    @Query("SELECT * FROM payment_history ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<PaymentHistoryEntity>>

    @Query("SELECT COUNT(*) FROM payment_history WHERE subscriptionId = :subId AND billingPeriodStart = :periodStart")
    suspend fun countPaymentsForPeriod(subId: Long, periodStart: Long): Int

    @Query("DELETE FROM payment_history WHERE id = (SELECT id FROM payment_history WHERE subscriptionId = :subId ORDER BY paymentDate DESC LIMIT 1)")
    suspend fun deleteLastPaymentForSubscription(subId: Long)

    @Query("DELETE FROM payment_history WHERE subscriptionId = :subId")
    suspend fun deleteAllPaymentsForSubscription(subId: Long)

    @Query("SELECT * FROM payment_history WHERE subscriptionId = :subId ORDER BY paymentDate DESC LIMIT 1")
    suspend fun getLastPaymentForSubscription(subId: Long): PaymentHistoryEntity?
}
