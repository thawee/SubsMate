package com.mate.subsmate

import com.mate.subsmate.ui.utils.LoanUtils
import org.junit.Assert.*
import org.junit.Test

class LoanUtilsTest {

    @Test
    fun `calculateRemainingBalance returns principal when no payments made`() {
        val balance = LoanUtils.calculateRemainingBalance(
            initialPrincipal = 100000.0,
            monthlyPayment = 5000.0,
            annualInterestRate = 5.0,
            installmentsPaid = 0
        )
        assertEquals(100000.0, balance, 0.01)
    }

    @Test
    fun `calculateRemainingBalance subtracts extra principal when no payments`() {
        val balance = LoanUtils.calculateRemainingBalance(
            initialPrincipal = 100000.0,
            monthlyPayment = 5000.0,
            annualInterestRate = 5.0,
            installmentsPaid = 0,
            extraPrincipalPaid = 10000.0
        )
        assertEquals(90000.0, balance, 0.01)
    }

    @Test
    fun `calculateRemainingBalance does not go negative`() {
        val balance = LoanUtils.calculateRemainingBalance(
            initialPrincipal = 100000.0,
            monthlyPayment = 5000.0,
            annualInterestRate = null,
            installmentsPaid = 30
        )
        assertEquals(0.0, balance, 0.01)
    }

    @Test
    fun `calculateRemainingBalance with zero interest`() {
        val balance = LoanUtils.calculateRemainingBalance(
            initialPrincipal = 60000.0,
            monthlyPayment = 5000.0,
            annualInterestRate = 0.0,
            installmentsPaid = 6
        )
        // 60000 - (5000 * 6) = 30000
        assertEquals(30000.0, balance, 0.01)
    }

    @Test
    fun `calculateRemainingBalance with interest`() {
        val balance = LoanUtils.calculateRemainingBalance(
            initialPrincipal = 100000.0,
            monthlyPayment = 5000.0,
            annualInterestRate = 12.0,
            installmentsPaid = 1
        )
        // Balance after 1 payment with 12% APR
        // Interest for first month: 100000 * 0.12 / 12 = 1000
        // Principal paid: 5000 - 1000 = 4000
        // Balance: 100000 - 4000 = 96000
        assertEquals(96000.0, balance, 0.01)
    }

    @Test
    fun `calculateNextPaymentSplit with zero interest`() {
        val split = LoanUtils.calculateNextPaymentSplit(
            initialPrincipal = 60000.0,
            monthlyPayment = 5000.0,
            annualInterestRate = 0.0,
            installmentsPaid = 0
        )
        assertEquals(0.0, split.interest, 0.01)
        assertEquals(5000.0, split.principal, 0.01)
    }

    @Test
    fun `calculateNextPaymentSplit with interest`() {
        val split = LoanUtils.calculateNextPaymentSplit(
            initialPrincipal = 100000.0,
            monthlyPayment = 5000.0,
            annualInterestRate = 12.0,
            installmentsPaid = 0
        )
        // Interest: 100000 * 0.12 / 12 = 1000
        // Principal: 5000 - 1000 = 4000
        assertEquals(1000.0, split.interest, 0.01)
        assertEquals(4000.0, split.principal, 0.01)
    }

    @Test
    fun `calculateNextPaymentSplit with null interest rate`() {
        val split = LoanUtils.calculateNextPaymentSplit(
            initialPrincipal = 100000.0,
            monthlyPayment = 5000.0,
            annualInterestRate = null,
            installmentsPaid = 0
        )
        assertEquals(0.0, split.interest, 0.01)
        assertEquals(5000.0, split.principal, 0.01)
    }

    @Test
    fun `calculateRemainingBalance with extra principal reduces balance faster`() {
        val balanceWithoutExtra = LoanUtils.calculateRemainingBalance(
            initialPrincipal = 100000.0,
            monthlyPayment = 5000.0,
            annualInterestRate = 0.0,
            installmentsPaid = 6
        )
        val balanceWithExtra = LoanUtils.calculateRemainingBalance(
            initialPrincipal = 100000.0,
            monthlyPayment = 5000.0,
            annualInterestRate = 0.0,
            installmentsPaid = 6,
            extraPrincipalPaid = 10000.0
        )
        assertEquals(70000.0, balanceWithoutExtra, 0.01)
        assertEquals(60000.0, balanceWithExtra, 0.01)
    }
}
