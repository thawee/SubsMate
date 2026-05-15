package com.mate.subsmate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mate.subsmate.data.local.entities.PaymentHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentHistoryEntity)

    @Query("SELECT * FROM payment_history ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<PaymentHistoryEntity>>

    @Query("SELECT * FROM payment_history WHERE subscriptionId = :subId ORDER BY paymentDate DESC")
    fun getPaymentsForSubscription(subId: Long): Flow<List<PaymentHistoryEntity>>

    @Query("DELETE FROM payment_history WHERE id = (SELECT id FROM payment_history WHERE subscriptionId = :subId ORDER BY paymentDate DESC LIMIT 1)")
    suspend fun deleteLastPaymentForSubscription(subId: Long)
}
