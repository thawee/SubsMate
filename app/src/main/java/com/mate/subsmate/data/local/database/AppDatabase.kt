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
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "subsmate-db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
