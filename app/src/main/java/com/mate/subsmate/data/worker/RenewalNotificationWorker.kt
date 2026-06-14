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
        val db = AppDatabase.getInstance(applicationContext)

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
            val reminderMillis = sub.reminderDaysBefore * com.mate.subsmate.ui.utils.TimeUtils.MILLIS_PER_DAY
            val windowEnd = now + reminderMillis
            
            // Auto-advance AUTO_PAY if overdue
            if (sub.paymentType == com.mate.subsmate.domain.model.PaymentType.AUTO_PAY && sub.nextBillingDate < now) {
                // Check if payment already exists for this billing period (user may have marked as paid)
                val existingPaymentCount = db.paymentDao().countPaymentsForPeriod(sub.id, sub.nextBillingDate)
                if (existingPaymentCount > 0) {
                    // Payment already recorded, just advance the date without creating duplicate
                val nextDate = calculateNextDate(sub.nextBillingDate, sub.billingCycle, sub.customCycleDays)
                    val isCompleted = sub.totalInstallments != null && (sub.currentInstallment + 1) >= sub.totalInstallments
                    dao.updateSubscription(sub.copy(
                        nextBillingDate = nextDate,
                        currentInstallment = if (sub.totalInstallments != null) sub.currentInstallment + 1 else sub.currentInstallment,
                        isActive = !isCompleted,
                        lastNotifiedDate = null
                    ))
                    return@forEach
                }

                val nextDate = calculateNextDate(sub.nextBillingDate, sub.billingCycle, sub.customCycleDays)
                
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

                val isCompleted = sub.totalInstallments != null && (sub.currentInstallment + 1) >= sub.totalInstallments
                dao.updateSubscription(sub.copy(
                    nextBillingDate = nextDate,
                    currentInstallment = if (sub.totalInstallments != null) sub.currentInstallment + 1 else sub.currentInstallment,
                    isActive = !isCompleted,
                    lastNotifiedDate = null
                ))
                return@forEach // Skip notification for this run as it's renewed
            }

            // Check for Renewal
            if (sub.nextBillingDate in now..windowEnd) {
                if (sub.lastNotifiedDate == null || sub.lastNotifiedDate < todayStart) {
                    val symbol = com.mate.subsmate.ui.utils.CurrencyUtils.getSymbol(sub.currency)
                    val priceMessage = when {
                        sub.isVariablePrice && sub.price == 0.0 -> ""
                        sub.isVariablePrice -> " (Est. $symbol${String.format("%.2f", sub.price)})"
                        else -> " for $symbol${String.format("%.2f", sub.price)}"
                    }
                    showNotification(
                        sub.id.toInt(),
                        "Subscription Renewal",
                        "${sub.name} is renewing soon$priceMessage",
                        sub.id
                    )
                    dao.updateSubscription(sub.copy(lastNotifiedDate = now))
                }
            }
            
            // Check for Trial End
            if (sub.isTrial && sub.trialEndDate != null && sub.trialEndDate in now..windowEnd) {
                if (sub.lastNotifiedDate == null || sub.lastNotifiedDate < todayStart) {
                    showNotification(
                        sub.id.toInt() + 100000,
                        "Trial Ending Soon",
                        "Your trial for ${sub.name} ends in ${sub.reminderDaysBefore} days",
                        sub.id
                    )
                    dao.updateSubscription(sub.copy(lastNotifiedDate = now))
                }
            }
        }

        return Result.success()
    }

    private fun calculateNextDate(currentDate: Long, cycle: com.mate.subsmate.domain.model.BillingCycle, customCycleDays: Int? = null): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentDate }
        val now = System.currentTimeMillis()
        val cycleDays = customCycleDays?.coerceAtLeast(1) ?: com.mate.subsmate.ui.utils.TimeUtils.DEFAULT_CUSTOM_CYCLE_DAYS
        
        while (calendar.timeInMillis <= now) {
            when (cycle) {
                com.mate.subsmate.domain.model.BillingCycle.MONTHLY -> calendar.add(Calendar.MONTH, 1)
                com.mate.subsmate.domain.model.BillingCycle.YEARLY -> calendar.add(Calendar.YEAR, 1)
                com.mate.subsmate.domain.model.BillingCycle.CUSTOM -> calendar.add(Calendar.DAY_OF_YEAR, cycleDays)
            }
        }
        return calendar.timeInMillis
    }

    private fun showNotification(id: Int, title: String, message: String, subId: Long = id.toLong()) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "renewal_notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Renewals", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }

        val markPaidIntent = android.content.Intent(NotificationActionReceiver.ACTION_MARK_PAID).apply {
            putExtra(NotificationActionReceiver.EXTRA_SUB_ID, subId)
            setPackage(applicationContext.packageName)
        }
        val markPaidPendingIntent = android.app.PendingIntent.getBroadcast(
            applicationContext, subId.toInt(), markPaidIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = android.content.Intent(NotificationActionReceiver.ACTION_SNOOZE).apply {
            putExtra(NotificationActionReceiver.EXTRA_SUB_ID, subId)
            setPackage(applicationContext.packageName)
        }
        val snoozePendingIntent = android.app.PendingIntent.getBroadcast(
            applicationContext, subId.toInt() + 100000, snoozeIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(0, "Mark as Paid", markPaidPendingIntent)
            .addAction(0, "Remind Later", snoozePendingIntent)
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
