package com.mate.subsmate.ui.utils

import java.util.Currency
import java.util.Locale

object CurrencyUtils {
    /**
     * Resolves the currency symbol dynamically for any 3-letter currency code.
     */
    fun getSymbol(currencyCode: String): String {
        if (currencyCode.isBlank()) return "$"
        val code = currencyCode.trim().uppercase()
        return try {
            val currency = Currency.getInstance(code)
            currency.getSymbol(Locale.getDefault())
        } catch (e: Exception) {
            when (code) {
                "THB" -> "฿"
                "USD" -> "$"
                "EUR" -> "€"
                "GBP" -> "£"
                "JPY" -> "¥"
                else -> code
            }
        }
    }
}
