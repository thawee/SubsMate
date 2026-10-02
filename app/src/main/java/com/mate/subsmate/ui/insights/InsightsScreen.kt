package com.mate.subsmate.ui.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add

import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mate.subsmate.ui.utils.CurrencyUtils
import com.mate.subsmate.ui.utils.PercentageUtils
import com.mate.subsmate.ui.components.CurrencyFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(viewModel: InsightsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val currencySymbol = CurrencyUtils.getSymbol(uiState.selectedCurrency)
    val showingForecast = uiState.selectedPeriod == InsightsPeriod.NEXT_TWELVE_MONTHS
    val displayedBreakdown = if (showingForecast) uiState.forecastBreakdown else uiState.categoryBreakdown
    val displayedTotal = if (showingForecast) uiState.forecast.total else uiState.totalMonthlySpend
    val formattedTotal = "$currencySymbol${String.format("%,.2f", displayedTotal)}"
    val hasOverview = uiState.categoryBreakdown.isNotEmpty() || uiState.forecastBreakdown.isNotEmpty() ||
        uiState.forecast.hasUnknownAmounts || uiState.forecast.overdueCharges > 0

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Insights", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Column {
                        Text("Spending currency", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground)
                        CurrencyFilter(
                            currencies = uiState.availableCurrencies,
                            selected = uiState.selectedCurrency,
                            onSelect = viewModel::setCurrency,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                if (!hasOverview && uiState.monthlyHistory.isEmpty()) {
                    item {
                        com.mate.subsmate.ui.components.EmptyStateView(
                            icon = Icons.Default.Add,
                            title = "No Data Yet",
                            subtitle = "No subscriptions or payments in ${uiState.selectedCurrency} yet.",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    if (hasOverview) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "Spending Overview",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = !showingForecast,
                                        onClick = { viewModel.setPeriod(InsightsPeriod.MONTHLY) },
                                        label = { Text("Monthly") }
                                    )
                                    FilterChip(
                                        selected = showingForecast,
                                        onClick = { viewModel.setPeriod(InsightsPeriod.NEXT_TWELVE_MONTHS) },
                                        label = { Text("Next 12 months") }
                                    )
                                }
                                Text(
                                    if (showingForecast) "Estimated from scheduled charges" else "Monthly equivalent based on active subscriptions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(if (displayedBreakdown.isEmpty()) 140.dp else 240.dp), contentAlignment = Alignment.Center) {
                                if (displayedBreakdown.isNotEmpty()) DonutChart(displayedBreakdown)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        if (showingForecast) "Next 12 months" else "Total / Mo",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        formattedTotal,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = when {
                                            formattedTotal.length > 16 -> 14.sp
                                            formattedTotal.length > 13 -> 18.sp
                                            else -> MaterialTheme.typography.headlineSmall.fontSize
                                        }
                                    )
                                }
                            }
                        }
                        if (showingForecast) {
                            if (uiState.forecast.usesVariableAmounts) {
                                item {
                                    Text(
                                        "Variable-price charges use their saved amount; actual charges may differ.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (uiState.forecast.hasUnknownAmounts) {
                                item {
                                    Text(
                                        "Some variable-price charges have no saved amount and are excluded from this estimate.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (uiState.forecast.overdueCharges > 0) {
                                item {
                                    Text(
                                        "Overdue: $currencySymbol${String.format("%,.2f", uiState.forecast.overdueAmount)} across ${uiState.forecast.overdueCharges} charges, excluded from the next 12 months.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    if (uiState.monthlyHistory.isNotEmpty()) {
                        item {
                            MonthlySpendChart(uiState.monthlyHistory, currencySymbol)
                        }
                    }
                    if (displayedBreakdown.isNotEmpty()) {
                        item {
                            Text(
                                if (showingForecast) "Forecast by Category" else "Category Breakdown",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(displayedBreakdown) { categorySpend ->
                            CategorySpendItem(categorySpend, currencySymbol)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DonutChart(breakdown: List<CategorySpend>) {
    val sweepProgress = androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(0f) }
    
    androidx.compose.runtime.LaunchedEffect(breakdown) {
        sweepProgress.animateTo(
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000, easing = androidx.compose.animation.core.FastOutSlowInEasing)
        )
    }

    Canvas(
        modifier = Modifier
            .size(200.dp)
            .semantics {
                contentDescription = "Donut chart showing spending breakdown across ${breakdown.size} categories. Top category is ${breakdown.firstOrNull()?.categoryName ?: "none"}."
            }
    ) {
        var startAngle = -90f
        breakdown.forEach { spend ->
            val sweepAngle = spend.percentage * 360f * sweepProgress.value
            drawArc(
                color = Color(android.graphics.Color.parseColor(spend.colorHex)),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 40f, cap = StrokeCap.Butt)
            )
            startAngle += (spend.percentage * 360f)
        }
    }
}

@Composable
fun CategorySpendItem(spend: CategorySpend, currencySymbol: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color(android.graphics.Color.parseColor(spend.colorHex)))
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(spend.categoryName, fontWeight = FontWeight.Medium)
            Text(
                "${PercentageUtils.label(spend.percentage)} of total spend",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            "$currencySymbol${String.format("%,.2f", spend.amount)}",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun MonthlySpendChart(history: List<MonthlySpend>, currencySymbol: String) {
    if (history.isEmpty()) return
    
    val maxVal = history.maxOfOrNull { it.amount } ?: 1.0
    val maxAmount = if (maxVal <= 0.0) 1.0 else maxVal
    
    val isDark = isSystemInDarkTheme()
    val barColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    val formatCompact = { amount: Double ->
        when {
            amount >= 1000000.0 -> "${String.format("%.1f", amount / 1000000.0)}M"
            amount >= 1000.0 -> "${String.format("%.1f", amount / 1000.0)}k"
            else -> String.format("%.0f", amount)
        }
    }

    val barProgress = androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(0f) }
    
    androidx.compose.runtime.LaunchedEffect(history) {
        barProgress.animateTo(
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 800, easing = androidx.compose.animation.core.FastOutSlowInEasing)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Past payments (actual, $currencySymbol)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics {
                        val maxMonth = history.maxByOrNull { it.amount }
                        contentDescription = "Bar chart showing monthly spending trend for the last ${history.size} months. Highest spending was in ${maxMonth?.monthName ?: ""} with $currencySymbol${maxMonth?.amount ?: 0}."
                    },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                history.forEach { spend ->
                    val proportion = (spend.amount / maxAmount).toFloat().coerceIn(0f, 1f)
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
                            color = MaterialTheme.colorScheme.primary,
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
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
