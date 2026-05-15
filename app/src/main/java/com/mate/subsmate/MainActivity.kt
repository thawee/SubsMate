package com.mate.subsmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mate.subsmate.data.local.database.AppDatabase
import com.mate.subsmate.data.local.preferences.PreferenceManager
import com.mate.subsmate.data.repository.SubscriptionRepositoryImpl
import com.mate.subsmate.ui.dashboard.DashboardScreen
import com.mate.subsmate.ui.dashboard.DashboardViewModel
import com.mate.subsmate.ui.add_subscription.AddSubscriptionScreen
import com.mate.subsmate.ui.add_subscription.AddSubscriptionViewModel
import com.mate.subsmate.ui.insights.InsightsScreen
import com.mate.subsmate.ui.insights.InsightsViewModel
import com.mate.subsmate.ui.settings.SettingsScreen
import com.mate.subsmate.ui.settings.SettingsViewModel
import com.mate.subsmate.ui.subscriptions.SubscriptionsScreen
import com.mate.subsmate.ui.subscriptions.SubscriptionsViewModel
import com.mate.subsmate.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.mate.subsmate.data.worker.RenewalNotificationWorker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Schedule notifications
        RenewalNotificationWorker.schedule(applicationContext)

        // Manual DI for the prototype
        val db = androidx.room.Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "subsmate-db"
        ).fallbackToDestructiveMigration().build()
        
        // Data Migration: Merge Entertainment (6) into Streaming (1)
        CoroutineScope(Dispatchers.IO).launch {
            db.subscriptionDao().updateCategoryId(6, 1)
        }

        val repository = SubscriptionRepositoryImpl(db.subscriptionDao(), db.paymentDao())
        val prefManager = PreferenceManager(applicationContext)
        val settingsViewModel = SettingsViewModel(prefManager)
        
        setContent {
            val settingsState by settingsViewModel.uiState.collectAsState()
            val darkTheme = when (settingsState.theme) {
                com.mate.subsmate.ui.settings.AppTheme.LIGHT -> false
                com.mate.subsmate.ui.settings.AppTheme.DARK -> true
                com.mate.subsmate.ui.settings.AppTheme.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            
            SubsMateTheme(darkTheme = darkTheme) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .systemBarsPadding() // Avoid overlap with bars
                ) {
                    // Vibrant Background Blobs to enable Glass effect visibility
                    Canvas(modifier = Modifier.fillMaxSize().blur(80.dp)) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(AccentBlue.copy(alpha = 0.4f), Color.Transparent),
                                center = Offset(size.width * 0.2f, size.height * 0.2f),
                                radius = size.width * 0.8f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(AccentPurple.copy(alpha = 0.3f), Color.Transparent),
                                center = Offset(size.width * 0.8f, size.height * 0.5f),
                                radius = size.width * 0.7f
                            )
                        )
                    }

                    MainApp(repository, settingsViewModel)
                }
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Insights : Screen("insights", "Insights", Icons.Default.Insights)
    object Subscriptions : Screen("subscriptions", "Subscriptions", Icons.Default.Payments)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun MainApp(
    repository: SubscriptionRepositoryImpl,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val settingsState by settingsViewModel.uiState.collectAsState()
    val items = listOf(Screen.Dashboard, Screen.Insights, Screen.Subscriptions, Screen.Settings)

    Scaffold(
        containerColor = Color.Transparent, // Make scaffold transparent to see background
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            
            // Only show bottom bar on main screens
            if (items.any { it.route == currentDestination?.route }) {
                NavigationBar(
                    containerColor = Color.Transparent, // Glassy Nav bar
                    tonalElevation = 0.dp
                ) {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController, 
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                val viewModel = remember { DashboardViewModel(repository) }
                // Update criteria whenever it changes in settings
                LaunchedEffect(settingsState.dashboardDayCriteria) {
                    viewModel.setDayCriteria(settingsState.dashboardDayCriteria)
                }
                LaunchedEffect(settingsState.userName) {
                    viewModel.setUserName(settingsState.userName)
                }
                LaunchedEffect(settingsState.monthlyBudget) {
                    viewModel.setMonthlyBudget(settingsState.monthlyBudget)
                }
                LaunchedEffect(settingsState.paidVisibilityDays) {
                    viewModel.setPaidVisibilityDays(settingsState.paidVisibilityDays)
                }
                DashboardScreen(
                    viewModel = viewModel,
                    currency = settingsState.selectedCurrency
                )
            }
            composable(Screen.Insights.route) {
                val viewModel = remember { InsightsViewModel(repository) }
                InsightsScreen(viewModel = viewModel)
            }
            composable(Screen.Subscriptions.route) {
                val viewModel = remember { SubscriptionsViewModel(repository) }
                SubscriptionsScreen(
                    viewModel = viewModel,
                    currency = settingsState.selectedCurrency,
                    onNavigateToAdd = { navController.navigate("add_subscription") },
                    onNavigateToEdit = { id -> navController.navigate("edit_subscription/$id") }
                )
            }
            composable("add_subscription") {
                val viewModel = remember { AddSubscriptionViewModel(repository) }
                AddSubscriptionScreen(
                    viewModel = viewModel, 
                    currency = settingsState.selectedCurrency,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("edit_subscription/{subId}") { backStackEntry ->
                val subId = backStackEntry.arguments?.getString("subId")?.toLongOrNull()
                val viewModel = remember { AddSubscriptionViewModel(repository) }
                
                // Initialize VM with subscription data if editing
                LaunchedEffect(subId) {
                    subId?.let { viewModel.loadSubscription(it) }
                }

                AddSubscriptionScreen(
                    viewModel = viewModel, 
                    currency = settingsState.selectedCurrency,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}
