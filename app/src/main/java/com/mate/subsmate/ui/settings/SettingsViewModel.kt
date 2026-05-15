package com.mate.subsmate.ui.settings

import androidx.lifecycle.ViewModel
import com.mate.subsmate.data.local.preferences.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AppTheme {
    SYSTEM, LIGHT, DARK
}

data class SettingsUiState(
    val userName: String = "User",
    val monthlyBudget: Double = 0.0,
    val isPro: Boolean = false,
    val theme: AppTheme = AppTheme.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val cloudSyncEnabled: Boolean = false,
    val userEmail: String? = null,
    val selectedCurrency: String = "USD",
    val dashboardDayCriteria: Int = 14,
    val paidVisibilityDays: Int = 1
)

class SettingsViewModel(private val prefManager: PreferenceManager) : ViewModel() {
    private val _uiState = MutableStateFlow(
        SettingsUiState(
            userName = prefManager.getString(PreferenceManager.KEY_USER_NAME, "User"),
            monthlyBudget = prefManager.getDouble(PreferenceManager.KEY_MONTHLY_BUDGET, 0.0),
            selectedCurrency = prefManager.getString(PreferenceManager.KEY_CURRENCY, "USD"),
            dashboardDayCriteria = prefManager.getInt(PreferenceManager.KEY_DASHBOARD_DAYS, 14),
            notificationsEnabled = prefManager.getBoolean(PreferenceManager.KEY_NOTIFICATIONS, true),
            theme = AppTheme.valueOf(prefManager.getString(PreferenceManager.KEY_THEME, AppTheme.SYSTEM.name)),
            paidVisibilityDays = prefManager.getInt(PreferenceManager.KEY_PAID_VISIBILITY, 1)
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        prefManager.setString(PreferenceManager.KEY_USER_NAME, name)
        _uiState.update { it.copy(userName = name) }
    }

    fun onBudgetChange(budget: Double) {
        prefManager.setDouble(PreferenceManager.KEY_MONTHLY_BUDGET, budget)
        _uiState.update { it.copy(monthlyBudget = budget) }
    }

    fun toggleNotifications(enabled: Boolean) {
        prefManager.setBoolean(PreferenceManager.KEY_NOTIFICATIONS, enabled)
        _uiState.update { it.copy(notificationsEnabled = enabled) }
    }

    fun setTheme(theme: AppTheme) {
        prefManager.setString(PreferenceManager.KEY_THEME, theme.name)
        _uiState.update { it.copy(theme = theme) }
    }

    fun setCurrency(currency: String) {
        prefManager.setString(PreferenceManager.KEY_CURRENCY, currency)
        _uiState.update { it.copy(selectedCurrency = currency) }
    }

    fun setDashboardDayCriteria(days: Int) {
        prefManager.setInt(PreferenceManager.KEY_DASHBOARD_DAYS, days)
        _uiState.update { it.copy(dashboardDayCriteria = days) }
    }

    fun setPaidVisibilityDays(days: Int) {
        prefManager.setInt(PreferenceManager.KEY_PAID_VISIBILITY, days)
        _uiState.update { it.copy(paidVisibilityDays = days) }
    }

    fun upgradeToPro() {
        // Mocking Pro upgrade
        _uiState.update { it.copy(isPro = true) }
    }
}
