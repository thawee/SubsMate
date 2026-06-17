package com.mate.subsmate.ui.utils

import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.pow

data class LoanPaymentSplit(
    val interest: Double,
    val principal: Double
)

data class LoanInsights(
    val totalInterest: Double,
    val totalPayment: Double,
    val payoffMonths: Int,
    val monthlyInterest: Double,
    val monthlyPrincipal: Double
)

data class ExtraPaymentImpact(
    val extraPerMonth: Double,
    val monthsSaved: Int,
    val interestSaved: Double,
    val newPayoffMonths: Int
)

object LoanUtils {
    fun estimateTotalInstallments(
        initialPrincipal: Double,
        monthlyPayment: Double,
        annualInterestRate: Double?
    ): Int? {
        if (monthlyPayment <= 0.0 || initialPrincipal <= 0.0) return null
        val rate = annualInterestRate ?: 0.0
        if (rate <= 0.0) {
            return ceil(initialPrincipal / monthlyPayment).toInt()
        }
        val r = rate / 12.0 / 100.0
        val n = -ln(1.0 - r * initialPrincipal / monthlyPayment) / ln(1.0 + r)
        return if (n.isNaN() || n.isInfinite()) null else ceil(n).toInt()
    }

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

    /**
     * Calculates total interest and payment summary for the remaining loan.
     */
    fun calculateLoanInsights(
        remainingBalance: Double,
        monthlyPayment: Double,
        annualInterestRate: Double?,
        remainingMonths: Int
    ): LoanInsights {
        val rate = annualInterestRate ?: 0.0
        if (rate <= 0.0 || remainingMonths <= 0) {
            return LoanInsights(
                totalInterest = 0.0,
                totalPayment = remainingBalance,
                payoffMonths = remainingMonths,
                monthlyInterest = 0.0,
                monthlyPrincipal = monthlyPayment
            )
        }

        val r = rate / 12.0 / 100.0
        var balance = remainingBalance
        var totalInterest = 0.0
        var months = 0

        while (balance > 0.0 && months < remainingMonths * 2) {
            val interest = balance * r
            val principal = (monthlyPayment - interest).coerceAtLeast(0.0)
            balance = (balance - principal).coerceAtLeast(0.0)
            totalInterest += interest
            months++
        }

        val nextSplit = calculateNextPaymentSplit(remainingBalance, monthlyPayment, annualInterestRate, 0)

        return LoanInsights(
            totalInterest = totalInterest,
            totalPayment = remainingBalance + totalInterest,
            payoffMonths = months,
            monthlyInterest = nextSplit.interest,
            monthlyPrincipal = nextSplit.principal
        )
    }

    /**
     * Calculates the impact of paying extra each month.
     */
    fun calculateExtraPaymentImpact(
        remainingBalance: Double,
        monthlyPayment: Double,
        annualInterestRate: Double?,
        extraPerMonth: Double,
        remainingMonths: Int
    ): ExtraPaymentImpact {
        if (extraPerMonth <= 0.0) {
            return ExtraPaymentImpact(0.0, 0, 0.0, remainingMonths)
        }

        val baseInsights = calculateLoanInsights(remainingBalance, monthlyPayment, annualInterestRate, remainingMonths)
        val withExtra = calculateLoanInsights(remainingBalance, monthlyPayment + extraPerMonth, annualInterestRate, remainingMonths)

        return ExtraPaymentImpact(
            extraPerMonth = extraPerMonth,
            monthsSaved = (baseInsights.payoffMonths - withExtra.payoffMonths).coerceAtLeast(0),
            interestSaved = (baseInsights.totalInterest - withExtra.totalInterest).coerceAtLeast(0.0),
            newPayoffMonths = withExtra.payoffMonths
        )
    }
}
