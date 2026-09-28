package com.mate.subsmate.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.filled.Add

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
import com.mate.subsmate.ui.insights.MonthlySpend
import com.mate.subsmate.ui.theme.*
import com.mate.subsmate.ui.utils.VendorUtils
import com.mate.subsmate.ui.utils.IconUtils
import com.mate.subsmate.ui.utils.LoanUtils
import com.mate.subsmate.ui.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    currency: String
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencySymbol = CurrencyUtils.getSymbol(currency)
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
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(0.dp)) }

            item {
                QuickStatsRow(
                    activeCount = uiState.activeCount,
                    trialCount = uiState.trialCount,
                    averageCost = uiState.averageCost,
                    currencySymbol = currencySymbol
                )
            }

            if (uiState.monthlyHistory.isNotEmpty()) {
                item {
                    SpendingTrendChart(uiState.monthlyHistory, currencySymbol)
                }
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

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filters = listOf(7 to "7d", 15 to "15d", 30 to "30d", -1 to "This Year")
                    items(filters) { (days, label) ->
                        val isSelected = uiState.dayCriteria == days
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setDayCriteria(days) },
                            label = { Text(label, maxLines = 1) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            if (uiState.upcomingCharges.isEmpty()) {
                item {
                    val periodText = if (uiState.dayCriteria == -1) "this year" else "next ${uiState.dayCriteria} days"
                    com.mate.subsmate.ui.components.EmptyStateView(
                        icon = Icons.Default.Add,
                        title = "All Caught Up!",
                        subtitle = "No upcoming charges $periodText.",
                        modifier = Modifier.fillMaxWidth().animateItem()
                    )
                }
            } else {
                items(uiState.upcomingCharges, key = { it.sub.id }) { model ->
                    SubscriptionItem(
                        model = model, 
                        currencySymbol = currencySymbol,
                        onMarkAsPaid = { viewModel.markAsPaid(model.sub) },
                        onUndoPayment = { viewModel.undoPayment(model.sub) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
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
fun QuickStatsRow(
    activeCount: Int,
    trialCount: Int,
    averageCost: Double,
    currencySymbol: String
) {
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Active",
            value = "$activeCount",
            cardBg = cardBg,
            borderColor = borderColor
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Trials",
            value = "$trialCount",
            cardBg = cardBg,
            borderColor = borderColor,
            valueColor = if (trialCount > 0) WarningOrange else MaterialTheme.colorScheme.onSurface
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Avg/Mo",
            value = "$currencySymbol${String.format("%,.0f", averageCost)}",
            cardBg = cardBg,
            borderColor = borderColor
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    cardBg: Color,
    borderColor: Color,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitle: String? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun SpendingTrendChart(history: List<MonthlySpend>, currencySymbol: String) {
    if (history.isEmpty()) return

    val maxVal = history.maxOfOrNull { it.amount } ?: 1.0
    val maxAmount = if (maxVal <= 0.0) 1.0 else maxVal
    val barColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    val formatCompact = { amount: Double ->
        when {
            amount >= 1000000.0 -> "$currencySymbol${String.format("%.1f", amount / 1000000.0)}M"
            amount >= 1000.0 -> "$currencySymbol${String.format("%.1f", amount / 1000.0)}k"
            else -> "$currencySymbol${String.format("%.0f", amount)}"
        }
    }

    val barProgress = androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(0f) }
    
    androidx.compose.runtime.LaunchedEffect(history) {
        barProgress.animateTo(
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 800, easing = androidx.compose.animation.core.FastOutSlowInEasing)
        )
    }

    GlassyCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Spending Trend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics {
                            val maxMonth = history.maxByOrNull { it.amount }
                            contentDescription = "Bar chart showing spending trend. Highest spending was in ${maxMonth?.monthName ?: ""} with $currencySymbol${maxMonth?.amount ?: 0}."
                        },
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    history.forEach { spend ->
                        val proportion = (spend.amount / maxAmount).toFloat().coerceIn(0.05f, 1f)
                        val animatedProportion = proportion * barProgress.value

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = formatCompact(spend.amount),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = barColor,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(animatedProportion * 0.8f)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(barColor)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = spend.monthName,
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor,
                                fontSize = 10.sp
                            )
                        }
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
    modifier: Modifier = Modifier,
    onMarkAsPaid: () -> Unit = {},
    onUndoPayment: () -> Unit = {}
) {
    val sub = model.sub
    val chargeDate = model.projectedDate ?: sub.nextBillingDate
    val daysRemaining = VendorUtils.getDaysRemaining(chargeDate)
    val logo = VendorUtils.getLogo(sub.name)
    val isDue = chargeDate <= System.currentTimeMillis()
    val isDark = isSystemInDarkTheme()
    val dateFormatter = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val isLoan = sub.totalInstallments != null

    val isBuggyDefault = sub.iconResId == "category" && sub.colorHex == "#6200EE" && sub.categoryId != 99
    val displayIconResId = if (isBuggyDefault) com.mate.subsmate.domain.model.CategoryDefaults.categories.find { it.id == sub.categoryId }?.iconName ?: sub.iconResId else sub.iconResId
    val displayColorHex = if (isBuggyDefault) com.mate.subsmate.domain.model.CategoryDefaults.categories.find { it.id == sub.categoryId }?.colorHex ?: sub.colorHex else sub.colorHex

    val cardBg = if (isDark) GlassNavy.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.85f)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.05f)

    Surface(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 0.dp,
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Main row: Logo | Name + Date | Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo
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
                            contentDescription = sub.name,
                            modifier = Modifier.size(28.dp).clip(CircleShape),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Icon(
                            imageVector = IconUtils.getIconByName(displayIconResId ?: "category"),
                            contentDescription = sub.name,
                            tint = displayColorHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Name + Date
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sub.name, 
                        fontWeight = FontWeight.Bold, 
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    val statusColor = when {
                        daysRemaining == 0L -> MaterialTheme.colorScheme.error
                        daysRemaining <= 3 -> WarningOrange
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    }
                    val statusText = if (model.projectedDate != null) {
                        dateFormatter.format(Date(chargeDate))
                    } else if (daysRemaining == 0L) {
                        "Due Today"
                    } else {
                        "In $daysRemaining days"
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }

                // Price + Action
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$currencySymbol${String.format("%,.2f", sub.price)}",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val haptic = LocalHapticFeedback.current
                    
                    // Action indicator
                    if (model.isRecentlyPaid) {
                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onUndoPayment()
                            },
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Undo", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PAID", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if (isDue && sub.paymentType == PaymentType.MANUAL) {
                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onMarkAsPaid()
                            },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Check, contentDescription = "Mark Paid", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                }
            }

            // Loan summary — compact single line
            if (isLoan && sub.totalLoanAmount != null) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = borderColor)
                Spacer(modifier = Modifier.height(10.dp))
                val displayInstallment = model.projectedInstallment ?: sub.currentInstallment
                val remaining = sub.totalInstallments - displayInstallment
                val balance = model.projectedBalance ?: LoanUtils.calculateRemainingBalance(
                    initialPrincipal = sub.totalLoanAmount,
                    monthlyPayment = sub.price,
                    annualInterestRate = sub.interestRate,
                    installmentsPaid = sub.currentInstallment,
                    extraPrincipalPaid = sub.extraPrincipalPaid
                )
                val progress = (displayInstallment.toFloat() / sub.totalInstallments).coerceIn(0f, 1f)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$displayInstallment/${sub.totalInstallments} ($remaining left)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Balance: $currencySymbol${String.format("%,.0f", balance)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp).height(3.dp).clip(RoundedCornerShape(2.dp)),
                    color = sub.colorHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            }

            // Credit Card summary
            if (sub.isCreditCard && sub.currentStatementBalance != null) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = borderColor)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Statement Bal: $currencySymbol${String.format("%,.2f", sub.currentStatementBalance)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    if (sub.minimumPaymentDue != null) {
                        Text(
                            text = "Min Due: $currencySymbol${String.format("%,.2f", sub.minimumPaymentDue)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
