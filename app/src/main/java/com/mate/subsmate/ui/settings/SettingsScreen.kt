package com.mate.subsmate.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

import com.mate.subsmate.ui.theme.SubsMateTheme
import com.mate.subsmate.ui.theme.GlassyCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Pro Section
            item {
                ProBanner(isPro = uiState.isPro) {
                    viewModel.upgradeToPro()
                }
            }

            item { SectionHeader("Profile & Budget") }

            item {
                GlassyCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = uiState.userName,
                            onValueChange = { viewModel.onNameChange(it) },
                            label = { Text("Your Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                            )
                        )

                        OutlinedTextField(
                            value = if (uiState.monthlyBudget == 0.0) "" else uiState.monthlyBudget.toString(),
                            onValueChange = { 
                                val budget = it.toDoubleOrNull() ?: 0.0
                                viewModel.onBudgetChange(budget) 
                            },
                            label = { Text("Monthly Budget (${uiState.selectedCurrency})") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }

            item { SectionHeader("General") }
            
            item {
                GlassyCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        SettingToggleItem(
                            title = "Notifications",
                            subtitle = "Get alerts before renewals",
                            icon = Icons.Default.Notifications,
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = { viewModel.toggleNotifications(it) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.1f))
                        SettingClickableItem(
                            title = "App Theme",
                            subtitle = uiState.theme.name.lowercase().replaceFirstChar { it.uppercase() },
                            icon = Icons.Default.Settings,
                            onClick = { showThemeDialog = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.1f))
                        SettingClickableItem(
                            title = "Main Currency",
                            subtitle = uiState.selectedCurrency,
                            icon = Icons.Default.Payments,
                            onClick = { 
                                val next = if (uiState.selectedCurrency == "USD") "THB" else "USD"
                                viewModel.setCurrency(next)
                            }
                        )
                    }
                }
            }

            item {
                GlassyCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        SettingClickableItem(
                            title = "Dashboard View",
                            subtitle = "Show charges within ${uiState.dashboardDayCriteria} days",
                            icon = Icons.Default.DateRange,
                            onClick = { 
                                val next = when (uiState.dashboardDayCriteria) {
                                    7 -> 14
                                    14 -> 30
                                    else -> 7
                                }
                                viewModel.setDashboardDayCriteria(next)
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.1f))
                        SettingClickableItem(
                            title = "Paid Visibility",
                            subtitle = "Keep 'Paid' items for ${uiState.paidVisibilityDays} days",
                            icon = Icons.Default.History,
                            onClick = { 
                                val next = when (uiState.paidVisibilityDays) {
                                    1 -> 3
                                    3 -> 5
                                    else -> 1
                                }
                                viewModel.setPaidVisibilityDays(next)
                            }
                        )
                    }
                }
            }

            item { SectionHeader("Account & Data") }

            item {
                GlassyCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        SettingToggleItem(
                            title = "Cloud Sync",
                            subtitle = "Backup data to Google Drive",
                            icon = Icons.Default.Cloud,
                            checked = uiState.cloudSyncEnabled,
                            onCheckedChange = { /* Toggle sync */ }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.1f))
                        SettingClickableItem(
                            title = "About SubsMate",
                            subtitle = "Version 1.0.0 (Mate Series)",
                            icon = Icons.Default.Info,
                            onClick = { }
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme") },
            text = {
                Column {
                    AppTheme.values().forEach { theme ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setTheme(theme)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = uiState.theme == theme,
                                onClick = {
                                    viewModel.setTheme(theme)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(theme.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProBanner(isPro: Boolean, onUpgrade: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPro) MaterialTheme.colorScheme.tertiaryContainer 
                            else MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Star, 
                contentDescription = null, 
                tint = if (isPro) MaterialTheme.colorScheme.tertiary else Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isPro) "You are a Pro User!" else "Upgrade to SubsMate Pro",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isPro) MaterialTheme.colorScheme.onTertiaryContainer else Color.White
            )
            Text(
                text = if (isPro) "Enjoy unlimited subscriptions & cloud sync." 
                       else "Unlock unlimited subscriptions and widgets.",
                style = MaterialTheme.typography.bodySmall,
                color = if (isPro) MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.8f)
            )
            if (!isPro) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onUpgrade,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text("Go Pro")
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingClickableItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
    }
}
