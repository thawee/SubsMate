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
import com.mate.subsmate.ui.utils.VendorUtils
import com.mate.subsmate.ui.utils.IconUtils
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
    val currencySymbol = if (currency == "THB") "฿" else "$"

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
                        onDelete = { viewModel.deleteSubscription(sub) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
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
    val cardBg = if (isDark) Color(0x331E3A8A) else Color(0x66FFFFFF)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        tonalElevation = 0.dp
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
                Text(
                    text = "${subscription.billingCycle.name.lowercase().replaceFirstChar { it.uppercase() }} • Next: ${dateFormatter.format(Date(subscription.nextBillingDate))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$currencySymbol${String.format("%,.2f", subscription.price)}",
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
