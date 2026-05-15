package com.mate.subsmate.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mate.subsmate.data.local.dao.SubscriptionDao
import com.mate.subsmate.data.local.dao.PaymentDao
import com.mate.subsmate.data.local.entities.CategoryEntity
import com.mate.subsmate.data.local.entities.SubscriptionEntity
import com.mate.subsmate.data.local.entities.PaymentHistoryEntity

@Database(
    entities = [SubscriptionEntity::class, CategoryEntity::class, PaymentHistoryEntity::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun paymentDao(): PaymentDao
}
