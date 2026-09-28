package com.mate.subsmate.ui.subscriptions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search

import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mate.subsmate.data.local.entities.SubscriptionEntity
import com.mate.subsmate.ui.theme.GlassNavy
import com.mate.subsmate.ui.utils.CurrencyUtils
import com.mate.subsmate.ui.utils.IconUtils
import com.mate.subsmate.ui.utils.LoanUtils
import com.mate.subsmate.ui.utils.VendorUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen(
    viewModel: SubscriptionsViewModel,
    currency: String,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencySymbol = CurrencyUtils.getSymbol(currency)
    var subscriptionToDelete by remember { mutableStateOf<SubscriptionEntity?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Subscriptions", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Subscription")
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search subscriptions...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f),
                    focusedContainerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.8f),
                    unfocusedContainerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.8f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sort Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.filteredSubscriptions.size} subscription${if (uiState.filteredSubscriptions.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                Box {
                    Surface(
                        onClick = { showSortMenu = true },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.sortOption.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Sort",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        SortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option.label,
                                        color = if (option == uiState.sortOption) MaterialTheme.colorScheme.primary
                                               else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    viewModel.onSortOptionChange(option)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subscriptions List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredSubscriptions.isEmpty()) {
                val title = if (uiState.searchQuery.isNotBlank()) "No Matches Found" else "No Subscriptions Yet"
                val subtitle = if (uiState.searchQuery.isNotBlank()) "Try a different search term." else "Tap + to add your first subscription and start tracking."
                val icon = if (uiState.searchQuery.isNotBlank()) Icons.Default.Search else Icons.Default.Add
                com.mate.subsmate.ui.components.EmptyStateView(
                    icon = icon,
                    title = title,
                    subtitle = subtitle,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(uiState.filteredSubscriptions, key = { it.id }) { sub ->
                        SubscriptionManageItem(
                            subscription = sub,
                            currencySymbol = currencySymbol,
                            modifier = Modifier.animateItem(),
                            onEdit = { onNavigateToEdit(sub.id) },
                            onDelete = { subscriptionToDelete = sub }
                        )
                    }
                }
            }
        }
    }

    if (subscriptionToDelete != null) {
        AlertDialog(
            onDismissRequest = { subscriptionToDelete = null },
            title = { Text("Delete Subscription") },
            text = { Text("Are you sure you want to delete ${subscriptionToDelete?.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        subscriptionToDelete?.let { viewModel.deleteSubscription(it) }
                        subscriptionToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { subscriptionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SubscriptionManageItem(
    subscription: SubscriptionEntity,
    currencySymbol: String,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val logo = VendorUtils.getLogo(subscription.name)
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) GlassNavy.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.85f)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.05f)
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 0.dp,
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isBuggyDefault = subscription.iconResId == "category" && subscription.colorHex == "#6200EE" && subscription.categoryId != 99
            val displayIconResId = if (isBuggyDefault) com.mate.subsmate.domain.model.CategoryDefaults.categories.find { it.id == subscription.categoryId }?.iconName ?: subscription.iconResId else subscription.iconResId
            val displayColorHex = if (isBuggyDefault) com.mate.subsmate.domain.model.CategoryDefaults.categories.find { it.id == subscription.categoryId }?.colorHex ?: subscription.colorHex else subscription.colorHex

            // Vendor Logo
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        displayColorHex?.let { Color(android.graphics.Color.parseColor(it)).copy(alpha = 0.15f) } 
                        ?: MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (logo != null) {
                    AsyncImage(
                        model = logo,
                        contentDescription = subscription.name,
                        modifier = Modifier.size(28.dp).clip(CircleShape),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = IconUtils.getIconByName(displayIconResId ?: "category"),
                        contentDescription = subscription.name,
                        tint = displayColorHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subscription.name, 
                    fontWeight = FontWeight.Bold, 
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subscription.totalInstallments != null) {
                    val remainingMonths = subscription.totalInstallments - subscription.currentInstallment
                    val progress = (subscription.currentInstallment.toFloat() / subscription.totalInstallments).coerceIn(0f, 1f)
                    Text(
                        text = "Payment ${subscription.currentInstallment} / ${subscription.totalInstallments} ($remainingMonths left)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = subscription.colorHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )
                    if (subscription.totalLoanAmount != null) {
                        val balance = LoanUtils.calculateRemainingBalance(
                            initialPrincipal = subscription.totalLoanAmount,
                            monthlyPayment = subscription.price,
                            annualInterestRate = subscription.interestRate,
                            installmentsPaid = subscription.currentInstallment,
                            extraPrincipalPaid = subscription.extraPrincipalPaid
                        )
                        val insights = LoanUtils.calculateLoanInsights(
                            remainingBalance = balance,
                            monthlyPayment = subscription.price,
                            annualInterestRate = subscription.interestRate,
                            remainingMonths = remainingMonths
                        )
                        Text(
                            text = "Balance: $currencySymbol${String.format("%,.0f", balance)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        if (insights.totalInterest > 0) {
                            Text(
                                text = "Interest: $currencySymbol${String.format("%,.0f", insights.totalInterest)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else if (subscription.isCreditCard) {
                    val nextDateStr = dateFormatter.format(Date(subscription.nextBillingDate))
                    Text(
                        text = "Credit Card • Due: $nextDateStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    if (subscription.currentStatementBalance != null) {
                        Text(
                            text = "Balance: $currencySymbol${String.format("%,.2f", subscription.currentStatementBalance)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    Text(
                        text = "${subscription.billingCycle.name.lowercase().replaceFirstChar { it.uppercase() }} • Next: ${dateFormatter.format(Date(subscription.nextBillingDate))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                val priceText = when {
                    subscription.isVariablePrice && subscription.price == 0.0 -> "Variable"
                    subscription.isVariablePrice -> "Est. $currencySymbol${String.format("%,.2f", subscription.price)}"
                    else -> "$currencySymbol${String.format("%,.2f", subscription.price)}"
                }
                Text(
                    text = priceText,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )
                Row {
                    IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
