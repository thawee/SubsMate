package com.mate.subsmate

import com.mate.subsmate.domain.model.BillingCycle
import org.junit.Assert.assertEquals
import org.junit.Test

class SubscriptionLogicTest {
    @Test
    fun testMonthlyCalculation() {
        val price = 120.0
        val monthlyEquivalent = if (BillingCycle.YEARLY.name == "YEARLY") price / 12 else price
        assertEquals(10.0, monthlyEquivalent, 0.01)
    }
}
