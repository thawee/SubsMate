package com.mate.subsmate.ui.utils

object PercentageUtils {
    fun label(fraction: Float): String {
        val percent = fraction * 100f
        return when {
            percent <= 0f -> "0%"
            percent < 1f -> "<1%"
            else -> "${percent.toInt()}%"
        }
    }
}
