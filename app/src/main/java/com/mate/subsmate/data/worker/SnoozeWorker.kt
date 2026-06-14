package com.mate.subsmate.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mate.subsmate.data.local.database.AppDatabase
import com.mate.subsmate.ui.utils.CurrencyUtils

class SnoozeWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val subId = inputData.getLong("subscriptionId", -1)
        if (subId == -1L) return Result.failure()

        val db = AppDatabase.getInstance(applicationContext)
        val sub = db.subscriptionDao().getSubscriptionByIdOnce(subId) ?: return Result.failure()

        if (!sub.isActive) return Result.success()

        val symbol = CurrencyUtils.getSymbol(sub.currency)
        val priceMessage = when {
            sub.isVariablePrice && sub.price == 0.0 -> ""
            sub.isVariablePrice -> " (Est. $symbol${String.format("%.2f", sub.price)})"
            else -> " for $symbol${String.format("%.2f", sub.price)}"
        }

        showNotification(
            sub.id.toInt(),
            "Subscription Renewal",
            "${sub.name} is renewing soon$priceMessage"
        )

        return Result.success()
    }

    private fun showNotification(id: Int, title: String, message: String) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "renewal_notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Renewals", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }

        val markPaidIntent = android.content.Intent(NotificationActionReceiver.ACTION_MARK_PAID).apply {
            putExtra(NotificationActionReceiver.EXTRA_SUB_ID, id.toLong())
            setPackage(applicationContext.packageName)
        }
        val markPaidPendingIntent = android.app.PendingIntent.getBroadcast(
            applicationContext, id, markPaidIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = android.content.Intent(NotificationActionReceiver.ACTION_SNOOZE).apply {
            putExtra(NotificationActionReceiver.EXTRA_SUB_ID, id.toLong())
            setPackage(applicationContext.packageName)
        }
        val snoozePendingIntent = android.app.PendingIntent.getBroadcast(
            applicationContext, id + 100000, snoozeIntent,
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
}
