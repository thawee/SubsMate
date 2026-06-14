package com.mate.subsmate.ui.subscriptions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mate.subsmate.data.local.entities.SubscriptionEntity
import com.mate.subsmate.ui.theme.GlassNavy
import com.mate.subsmate.ui.utils.VendorUtils
import com.mate.subsmate.ui.utils.IconUtils
import com.mate.subsmate.ui.utils.LoanUtils
import com.mate.subsmate.ui.utils.CurrencyUtils
import com.mate.subsmate.ui.theme.GlassyCard
import java.text.SimpleDateFormat
import java.util.*

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
        if (uiState.subscriptions.isEmpty() && !uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No subscriptions yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.subscriptions) { sub ->
                    SubscriptionManageItem(
                        subscription = sub,
                        currencySymbol = currencySymbol,
                        onEdit = { onNavigateToEdit(sub.id) },
                        onDelete = { subscriptionToDelete = sub }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
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
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val logo = VendorUtils.getLogo(subscription.name)
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) GlassNavy.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.85f)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.05f)
    
    Surface(
        modifier = Modifier
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
            // Vendor Logo
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        subscription.colorHex?.let { Color(android.graphics.Color.parseColor(it)).copy(alpha = 0.15f) } 
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
                        imageVector = IconUtils.getIconByName(subscription.iconResId ?: "category"),
                        contentDescription = subscription.name,
                        tint = subscription.colorHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: MaterialTheme.colorScheme.primary,
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
                    val progress = (subscription.currentInstallment.toFloat() / subscription.totalInstallments).coerceIn(0f, 1f)
                    Text(
                        text = "Payment ${subscription.currentInstallment} / ${subscription.totalInstallments} (${subscription.totalInstallments - subscription.currentInstallment} left)",
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
                        Text(
                            text = "Est. Balance: $currencySymbol${String.format("%,.0f", balance)}",
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
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
