package com.mate.subsmate.ui.utils

import com.mate.subsmate.domain.model.BillingCycle
import java.util.Calendar

object BillingUtils {
    fun calculateNextDate(currentDate: Long, cycle: BillingCycle, customCycleDays: Int? = null): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentDate }
        val now = System.currentTimeMillis()
        val cycleDays = customCycleDays?.coerceAtLeast(1) ?: TimeUtils.DEFAULT_CUSTOM_CYCLE_DAYS

        while (calendar.timeInMillis <= now) {
            when (cycle) {
                BillingCycle.MONTHLY -> calendar.add(Calendar.MONTH, 1)
                BillingCycle.YEARLY -> calendar.add(Calendar.YEAR, 1)
                BillingCycle.CUSTOM -> calendar.add(Calendar.DAY_OF_YEAR, cycleDays)
            }
        }
        return calendar.timeInMillis
    }

    fun advanceByOneCycle(currentDate: Long, cycle: BillingCycle, customCycleDays: Int? = null): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentDate }
        val cycleDays = customCycleDays?.coerceAtLeast(1) ?: TimeUtils.DEFAULT_CUSTOM_CYCLE_DAYS
        when (cycle) {
            BillingCycle.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            BillingCycle.YEARLY -> calendar.add(Calendar.YEAR, 1)
            BillingCycle.CUSTOM -> calendar.add(Calendar.DAY_OF_YEAR, cycleDays)
        }
        return calendar.timeInMillis
    }

    fun revertNextDate(currentDate: Long, cycle: BillingCycle, customCycleDays: Int? = null): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentDate }
        when (cycle) {
            BillingCycle.MONTHLY -> calendar.add(Calendar.MONTH, -1)
            BillingCycle.YEARLY -> calendar.add(Calendar.YEAR, -1)
            BillingCycle.CUSTOM -> calendar.add(Calendar.DAY_OF_YEAR, -(customCycleDays?.coerceAtLeast(1) ?: TimeUtils.DEFAULT_CUSTOM_CYCLE_DAYS))
        }
        return calendar.timeInMillis
    }
}
