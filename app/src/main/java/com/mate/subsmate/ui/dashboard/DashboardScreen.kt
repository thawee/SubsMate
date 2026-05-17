package com.mate.subsmate.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mate.subsmate.data.local.entities.SubscriptionEntity
import com.mate.subsmate.domain.model.PaymentType
import com.mate.subsmate.ui.insights.CategorySpend
import com.mate.subsmate.ui.theme.*
import com.mate.subsmate.ui.utils.VendorUtils
import com.mate.subsmate.ui.utils.IconUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    currency: String
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencySymbol = if (currency == "THB") "฿" else "$"
    val today = remember { SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "Hello, ${uiState.userName}", 
                            fontWeight = FontWeight.ExtraBold, 
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = today, 
                            style = MaterialTheme.typography.labelMedium, 
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(0.dp)) }

            item {
                BudgetUsageCard(
                    spent = uiState.monthlyTotal, 
                    budget = uiState.monthlyBudget, 
                    yearly = uiState.yearlyTotal,
                    currencySymbol = currencySymbol
                )
            }

            if (uiState.categoryBreakdown.isNotEmpty()) {
                item {
                    CategoryBreakdownSummary(uiState.categoryBreakdown)
                }
            }

            item {
                Text(
                    text = "Upcoming Charges",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (uiState.upcomingCharges.isEmpty()) {
                item {
                    GlassyCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "No upcoming charges in next ${uiState.dayCriteria} days.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(uiState.upcomingCharges) { model ->
                    SubscriptionItem(
                        model = model, 
                        currencySymbol = currencySymbol,
                        onMarkAsPaid = { viewModel.markAsPaid(model.sub) },
                        onUndoPayment = { viewModel.undoPayment(model.sub) }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun BudgetUsageCard(spent: Double, budget: Double, yearly: Double, currencySymbol: String) {
    val percentage = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1.2f) else 0f
    val isOverBudget = budget > 0 && spent > budget
    val progressColor = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary

    GlassyCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(90.dp)) {
                    CircularProgressIndicator(
                        progress = { percentage.coerceAtMost(1f) },
                        modifier = Modifier.fillMaxSize(),
                        color = progressColor,
                        strokeWidth = 10.dp,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        strokeCap = StrokeCap.Round
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(percentage * 100).toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = progressColor
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Monthly Budget", 
                        style = MaterialTheme.typography.labelLarge, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$currencySymbol${String.format("%,.2f", spent)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (budget > 0) {
                            Text(
                                text = " / $currencySymbol${String.format("%,.0f", budget)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )
                        }
                    }

                    if (budget > 0) {
                        val remaining = (budget - spent).coerceAtLeast(0.0)
                        Surface(
                            color = if (isOverBudget) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) 
                                    else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = if (!isOverBudget) "$currencySymbol${String.format("%,.2f", remaining)} left" 
                                       else "Over by $currencySymbol${String.format("%,.2f", spent - budget)}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Monthly Card
                Surface(
                    modifier = Modifier.weight(1f),
                    color = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Monthly Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$currencySymbol${String.format("%,.2f", spent)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
                // Yearly Card
                Surface(
                    modifier = Modifier.weight(1f),
                    color = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Yearly Estimate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$currencySymbol${String.format("%,.2f", yearly)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBreakdownSummary(breakdown: List<CategorySpend>) {
    GlassyCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                "Spending by Category", 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                breakdown.forEach { spend ->
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(spend.percentage.coerceAtLeast(0.01f))
                            .background(Color(android.graphics.Color.parseColor(spend.colorHex)))
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                breakdown.take(3).forEach { spend ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(spend.colorHex)))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            spend.categoryName, 
                            style = MaterialTheme.typography.bodySmall, 
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${(spend.percentage * 100).toInt()}%", 
                            style = MaterialTheme.typography.labelSmall, 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionItem(
    model: SubscriptionChargeUiModel, 
    currencySymbol: String,
    onMarkAsPaid: () -> Unit = {},
    onUndoPayment: () -> Unit = {}
) {
    val sub = model.sub
    val daysRemaining = VendorUtils.getDaysRemaining(sub.nextBillingDate)
    val logo = VendorUtils.getLogo(sub.name)
    val isDue = sub.nextBillingDate <= System.currentTimeMillis()

    val cardBg = if (isSystemInDarkTheme()) GlassNavy.copy(alpha = 0.3f) else GlassWhite.copy(alpha = 0.6f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = cardBg,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        sub.colorHex?.let { Color(android.graphics.Color.parseColor(it)).copy(alpha = 0.15f) } 
                        ?: MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (logo != null) {
                    AsyncImage(
                        model = logo,
                        contentDescription = sub.name,
                        modifier = Modifier.size(32.dp).clip(CircleShape),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = IconUtils.getIconByName(sub.iconResId ?: "category"),
                        contentDescription = sub.name,
                        tint = sub.colorHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sub.name, 
                    fontWeight = FontWeight.Bold, 
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                val statusColor = when {
                    daysRemaining == 0L -> MaterialTheme.colorScheme.error
                    daysRemaining <= 3 -> WarningOrange
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                }
                Text(
                    text = if (daysRemaining == 0L) "Due Today" else "In $daysRemaining days",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                Text(
                    text = sub.billingCycle.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Payment Action / Status Indicator
                if (model.isRecentlyPaid) {
                    // Paid Status - Clickable to Undo
                    Surface(
                        onClick = onUndoPayment,
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Undo", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PAID", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                } else if (sub.paymentType == PaymentType.MANUAL) {
                    // Interactive M indicator (Button)
                    Surface(
                        onClick = onMarkAsPaid,
                        shape = CircleShape,
                        color = if (isDue) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        modifier = Modifier.size(40.dp),
                        shadowElevation = if (isDue) 4.dp else 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isDue) {
                                Icon(
                                    imageVector = Icons.Default.Check, 
                                    contentDescription = "Mark as Paid", 
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(
                                    text = "M",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                } else {
                    // Auto-Pay Indicator (A)
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "A",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }

                Text(
                    text = "$currencySymbol${String.format("%,.2f", sub.price)}",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
