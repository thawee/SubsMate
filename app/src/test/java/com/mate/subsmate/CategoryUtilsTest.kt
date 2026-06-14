package com.mate.subsmate

import com.mate.subsmate.data.local.entities.SubscriptionEntity
import com.mate.subsmate.domain.model.BillingCycle
import com.mate.subsmate.domain.model.PaymentType
import com.mate.subsmate.ui.utils.CategoryUtils
import org.junit.Assert.*
import org.junit.Test

class CategoryUtilsTest {

    private fun createSubscription(
        price: Double,
        billingCycle: BillingCycle,
        categoryId: Int = 1,
        customCycleDays: Int? = null
    ) = SubscriptionEntity(
        name = "Test",
        price = price,
        categoryId = categoryId,
        billingCycle = billingCycle,
        customCycleDays = customCycleDays,
        firstBillingDate = System.currentTimeMillis(),
        nextBillingDate = System.currentTimeMillis()
    )

    @Test
    fun `calculateCategoryBreakdown returns empty for zero total`() {
        val subs = listOf(createSubscription(100.0, BillingCycle.MONTHLY))
        val result = CategoryUtils.calculateCategoryBreakdown(subs, 0.0)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `calculateCategoryBreakdown returns empty for negative total`() {
        val subs = listOf(createSubscription(100.0, BillingCycle.MONTHLY))
        val result = CategoryUtils.calculateCategoryBreakdown(subs, -10.0)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `calculateCategoryBreakdown handles MONTHLY subscription`() {
        val subs = listOf(createSubscription(100.0, BillingCycle.MONTHLY))
        val result = CategoryUtils.calculateCategoryBreakdown(subs, 100.0)
        assertEquals(1, result.size)
        assertEquals(100.0, result[0].amount, 0.01)
        assertEquals(1.0f, result[0].percentage, 0.01f)
    }

    @Test
    fun `calculateCategoryBreakdown handles YEARLY subscription`() {
        val subs = listOf(createSubscription(1200.0, BillingCycle.YEARLY))
        val result = CategoryUtils.calculateCategoryBreakdown(subs, 100.0)
        assertEquals(1, result.size)
        assertEquals(100.0, result[0].amount, 0.01)
    }

    @Test
    fun `calculateCategoryBreakdown handles CUSTOM subscription with 30 days`() {
        val subs = listOf(createSubscription(60.0, BillingCycle.CUSTOM, customCycleDays = 30))
        val result = CategoryUtils.calculateCategoryBreakdown(subs, 60.0)
        assertEquals(1, result.size)
        assertEquals(60.0, result[0].amount, 0.01)
    }

    @Test
    fun `calculateCategoryBreakdown handles CUSTOM subscription with 15 days`() {
        val subs = listOf(createSubscription(30.0, BillingCycle.CUSTOM, customCycleDays = 15))
        val result = CategoryUtils.calculateCategoryBreakdown(subs, 60.0)
        assertEquals(1, result.size)
        // 30 * 30 / 15 = 60.0 monthly equivalent
        assertEquals(60.0, result[0].amount, 0.01)
    }

    @Test
    fun `calculateCategoryBreakdown handles CUSTOM subscription with null cycle days`() {
        val subs = listOf(createSubscription(60.0, BillingCycle.CUSTOM, customCycleDays = null))
        val result = CategoryUtils.calculateCategoryBreakdown(subs, 60.0)
        assertEquals(1, result.size)
        // Falls back to 30 days
        assertEquals(60.0, result[0].amount, 0.01)
    }

    @Test
    fun `calculateCategoryBreakdown handles multiple categories`() {
        val subs = listOf(
            createSubscription(100.0, BillingCycle.MONTHLY, categoryId = 1),
            createSubscription(200.0, BillingCycle.MONTHLY, categoryId = 2)
        )
        val result = CategoryUtils.calculateCategoryBreakdown(subs, 300.0)
        assertEquals(2, result.size)
        // Sorted by amount descending
        assertEquals(200.0, result[0].amount, 0.01)
        assertEquals(100.0, result[1].amount, 0.01)
    }

    @Test
    fun `calculateCategoryBreakdown merges same category`() {
        val subs = listOf(
            createSubscription(100.0, BillingCycle.MONTHLY, categoryId = 1),
            createSubscription(50.0, BillingCycle.MONTHLY, categoryId = 1)
        )
        val result = CategoryUtils.calculateCategoryBreakdown(subs, 150.0)
        assertEquals(1, result.size)
        assertEquals(150.0, result[0].amount, 0.01)
    }

    @Test
    fun `calculateCategoryBreakdown handles mixed billing cycles`() {
        val subs = listOf(
            createSubscription(1200.0, BillingCycle.YEARLY, categoryId = 1), // 100/mo
            createSubscription(50.0, BillingCycle.MONTHLY, categoryId = 1),  // 50/mo
            createSubscription(60.0, BillingCycle.CUSTOM, categoryId = 1, customCycleDays = 30) // 60/mo
        )
        val result = CategoryUtils.calculateCategoryBreakdown(subs, 210.0)
        assertEquals(1, result.size)
        assertEquals(210.0, result[0].amount, 0.01)
    }

    @Test
    fun `calculateCategoryBreakdown resolves category name`() {
        val subs = listOf(createSubscription(100.0, BillingCycle.MONTHLY, categoryId = 1))
        val result = CategoryUtils.calculateCategoryBreakdown(subs, 100.0)
        assertEquals("Streaming", result[0].categoryName)
    }

    @Test
    fun `calculateCategoryBreakdown uses Other for unknown category`() {
        val subs = listOf(createSubscription(100.0, BillingCycle.MONTHLY, categoryId = 999))
        val result = CategoryUtils.calculateCategoryBreakdown(subs, 100.0)
        assertEquals("Other", result[0].categoryName)
    }
}
