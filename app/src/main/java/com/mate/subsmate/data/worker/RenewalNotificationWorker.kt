package com.mate.subsmate.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.mate.subsmate.data.local.database.AppDatabase
import com.mate.subsmate.data.local.entities.SubscriptionEntity
import java.util.*
import java.util.concurrent.TimeUnit

class RenewalNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = androidx.room.Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "subsmate-db"
        ).fallbackToDestructiveMigration().build()

        val dao = db.subscriptionDao()
        val subscriptions = dao.getActiveSubscriptionsOneShot()
        val now = System.currentTimeMillis()
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        subscriptions.forEach { sub ->
            val reminderMillis = sub.reminderDaysBefore * 24 * 60 * 60 * 1000L
            val windowEnd = now + reminderMillis
            
            // Auto-advance AUTO_PAY if overdue
            if (sub.paymentType == com.mate.subsmate.domain.model.PaymentType.AUTO_PAY && sub.nextBillingDate < now) {
                val nextDate = calculateNextDate(sub.nextBillingDate, sub.billingCycle)
                
                // Record payment history
                db.paymentDao().insertPayment(
                    com.mate.subsmate.data.local.entities.PaymentHistoryEntity(
                        subscriptionId = sub.id,
                        subscriptionName = sub.name,
                        amount = sub.price,
                        currency = sub.currency,
                        paymentDate = now,
                        billingPeriodStart = sub.nextBillingDate,
                        billingPeriodEnd = nextDate
                    )
                )

                dao.updateSubscription(sub.copy(nextBillingDate = nextDate, lastNotifiedDate = null))
                return@forEach // Skip notification for this run as it's renewed
            }

            // Check for Renewal
            if (sub.nextBillingDate in now..windowEnd) {
                if (sub.lastNotifiedDate == null || sub.lastNotifiedDate < todayStart) {
                    showNotification(
                        sub.id.toInt(),
                        "Subscription Renewal",
                        "${sub.name} is renewing soon for ${sub.currency}${sub.price}"
                    )
                    dao.updateSubscription(sub.copy(lastNotifiedDate = now))
                }
            }
            
            // Check for Trial End
            if (sub.isTrial && sub.trialEndDate != null && sub.trialEndDate in now..windowEnd) {
                if (sub.lastNotifiedDate == null || sub.lastNotifiedDate < todayStart) {
                    showNotification(
                        sub.id.toInt() + 100000, // Different ID for trial notification
                        "Trial Ending Soon",
                        "Your trial for ${sub.name} ends in ${sub.reminderDaysBefore} days"
                    )
                    dao.updateSubscription(sub.copy(lastNotifiedDate = now))
                }
            }
        }

        return Result.success()
    }

    private fun calculateNextDate(currentDate: Long, cycle: com.mate.subsmate.domain.model.BillingCycle): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentDate }
        val now = System.currentTimeMillis()
        
        while (calendar.timeInMillis <= now) {
            when (cycle) {
                com.mate.subsmate.domain.model.BillingCycle.MONTHLY -> calendar.add(Calendar.MONTH, 1)
                com.mate.subsmate.domain.model.BillingCycle.YEARLY -> calendar.add(Calendar.YEAR, 1)
                com.mate.subsmate.domain.model.BillingCycle.CUSTOM -> calendar.add(Calendar.DAY_OF_YEAR, 30)
            }
        }
        return calendar.timeInMillis
    }

    private fun showNotification(id: Int, title: String, message: String) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "renewal_notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Renewals", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify(id, notification)
    }

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<RenewalNotificationWorker>(12, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "renewal_check",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
