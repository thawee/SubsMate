package com.mate.subsmate.data.worker

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mate.subsmate.data.local.database.AppDatabase
import com.mate.subsmate.ui.utils.BillingUtils
import com.mate.subsmate.ui.utils.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val subId = intent.getLongExtra(EXTRA_SUB_ID, -1)
        if (subId == -1L) return

        val action = intent.action
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val dao = db.subscriptionDao()
                val sub = dao.getSubscriptionByIdOnce(subId) ?: return@launch

                when (action) {
                    ACTION_MARK_PAID -> {
                        val nextDate = BillingUtils.calculateNextDate(sub.nextBillingDate, sub.billingCycle, sub.customCycleDays)

                        db.paymentDao().insertPayment(
                            com.mate.subsmate.data.local.entities.PaymentHistoryEntity(
                                subscriptionId = sub.id,
                                subscriptionName = sub.name,
                                amount = sub.price,
                                currency = sub.currency,
                                paymentDate = System.currentTimeMillis(),
                                billingPeriodStart = sub.nextBillingDate,
                                billingPeriodEnd = nextDate
                            )
                        )

                        val isCompleted = sub.totalInstallments != null && (sub.currentInstallment + 1) >= sub.totalInstallments
                        dao.updateSubscription(sub.copy(
                            nextBillingDate = nextDate,
                            currentInstallment = if (sub.totalInstallments != null) sub.currentInstallment + 1 else sub.currentInstallment,
                            isActive = !isCompleted
                        ))

                        // Dismiss notification
                        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        manager.cancel(subId.toInt())
                    }

                    ACTION_SNOOZE -> {
                        // Snooze for 1 day
                        val snoozeTime = System.currentTimeMillis() + TimeUtils.MILLIS_PER_DAY
                        dao.updateSubscription(sub.copy(
                            lastNotifiedDate = null,
                            reminderDaysBefore = 1
                        ))

                        // Schedule a one-time work for 24 hours later
                        val workRequest = androidx.work.OneTimeWorkRequestBuilder<SnoozeWorker>()
                            .setInitialDelay(24, java.util.concurrent.TimeUnit.HOURS)
                            .setInputData(
                                androidx.work.workDataOf("subscriptionId" to subId)
                            )
                            .build()
                        androidx.work.WorkManager.getInstance(context).enqueueUniqueWork(
                            "snooze_$subId",
                            androidx.work.ExistingWorkPolicy.REPLACE,
                            workRequest
                        )

                        // Dismiss notification
                        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        manager.cancel(subId.toInt())
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_MARK_PAID = "com.mate.subsmate.ACTION_MARK_PAID"
        const val ACTION_SNOOZE = "com.mate.subsmate.ACTION_SNOOZE"
        const val EXTRA_SUB_ID = "subscription_id"
    }
}
