package com.mate.subsmate.ui.utils

import kotlin.math.pow

data class LoanPaymentSplit(
    val interest: Double,
    val principal: Double
)

object LoanUtils {
    /**
     * Calculates the remaining principal balance after [installmentsPaid] payments, taking
     * any [extraPrincipalPaid] into account.
     */
    fun calculateRemainingBalance(
        initialPrincipal: Double,
        monthlyPayment: Double,
        annualInterestRate: Double?,
        installmentsPaid: Int,
        extraPrincipalPaid: Double = 0.0
    ): Double {
        if (installmentsPaid <= 0) return (initialPrincipal - extraPrincipalPaid).coerceAtLeast(0.0)
        val rate = annualInterestRate ?: 0.0
        val baseBalance = if (rate <= 0.0) {
            initialPrincipal - (monthlyPayment * installmentsPaid)
        } else {
            val r = rate / 12.0 / 100.0
            val comp = (1.0 + r).pow(installmentsPaid)
            initialPrincipal * comp - monthlyPayment * (comp - 1.0) / r
        }
        return (baseBalance - extraPrincipalPaid).coerceAtLeast(0.0)
    }

    /**
     * Calculates the interest and principal split for the *next* payment, taking
     * any [extraPrincipalPaid] into account.
     */
    fun calculateNextPaymentSplit(
        initialPrincipal: Double,
        monthlyPayment: Double,
        annualInterestRate: Double?,
        installmentsPaid: Int,
        extraPrincipalPaid: Double = 0.0
    ): LoanPaymentSplit {
        val currentBalance = calculateRemainingBalance(initialPrincipal, monthlyPayment, annualInterestRate, installmentsPaid, extraPrincipalPaid)
        val rate = annualInterestRate ?: 0.0
        if (rate <= 0.0) {
            return LoanPaymentSplit(interest = 0.0, principal = monthlyPayment)
        }
        
        val r = rate / 12.0 / 100.0
        val interestPortion = currentBalance * r
        val principalPortion = (monthlyPayment - interestPortion).coerceIn(0.0, monthlyPayment)
        return LoanPaymentSplit(interest = interestPortion, principal = principalPortion)
    }
}
