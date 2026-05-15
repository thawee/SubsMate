package com.mate.subsmate.data.local.preferences

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("subsmate_prefs", Context.MODE_PRIVATE)

    fun getString(key: String, defaultValue: String): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    fun setString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getDouble(key: String, defaultValue: Double): Double {
        return prefs.getFloat(key, defaultValue.toFloat()).toDouble()
    }

    fun setDouble(key: String, value: Double) {
        prefs.edit().putFloat(key, value.toFloat()).apply()
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return prefs.getInt(key, defaultValue)
    }

    fun setInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    fun setBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    companion object {
        const val KEY_USER_NAME = "user_name"
        const val KEY_MONTHLY_BUDGET = "monthly_budget"
        const val KEY_CURRENCY = "currency"
        const val KEY_DASHBOARD_DAYS = "dashboard_days"
        const val KEY_NOTIFICATIONS = "notifications"
        const val KEY_THEME = "app_theme"
        const val KEY_PAID_VISIBILITY = "paid_visibility_days"
    }
}
