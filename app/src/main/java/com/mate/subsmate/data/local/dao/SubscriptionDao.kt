package com.mate.subsmate.data.local.dao

import androidx.room.*
import com.mate.subsmate.data.local.entities.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions WHERE isActive = 1 ORDER BY nextBillingDate ASC")
    fun getAllActiveSubscriptions(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE isActive = 1")
    suspend fun getActiveSubscriptionsOneShot(): List<SubscriptionEntity>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    fun getSubscriptionById(id: Long): Flow<SubscriptionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubscriptionEntity)

    @Update
    suspend fun updateSubscription(subscription: SubscriptionEntity)

    @Delete
    suspend fun deleteSubscription(subscription: SubscriptionEntity)

    @Query("UPDATE subscriptions SET categoryId = :newId WHERE categoryId = :oldId")
    suspend fun updateCategoryId(oldId: Int, newId: Int)

    @Query("SELECT SUM(CASE WHEN billingCycle = 'MONTHLY' THEN price WHEN billingCycle = 'YEARLY' THEN price / 12 ELSE 0 END) FROM subscriptions WHERE isActive = 1")
    fun getEstimatedMonthlyTotal(): Flow<Double?>
}
