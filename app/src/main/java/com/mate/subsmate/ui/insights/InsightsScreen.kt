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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mate.subsmate.ui.utils.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(viewModel: InsightsViewModel, currency: String) {
    val uiState by viewModel.uiState.collectAsState()
    val currencySymbol = CurrencyUtils.getSymbol(currency)

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
                    Text(
                        "Spending Overview",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (uiState.categoryBreakdown.isEmpty()) {
                    item {
                        Text("Add subscriptions to see insights", color = Color.Gray)
                    }
                } else {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
                            DonutChart(uiState.categoryBreakdown)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total / Mo", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                Text(
                                    "$currencySymbol${String.format("%.2f", uiState.totalMonthlySpend)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (uiState.monthlyHistory.isNotEmpty()) {
                        item {
                            MonthlySpendChart(uiState.monthlyHistory, currencySymbol)
                        }
                    }

                    item {
                        Text(
                            "Category Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(uiState.categoryBreakdown) { categorySpend ->
                        CategorySpendItem(categorySpend, currencySymbol)
                    }
                }
            }
        }
    }
}

@Composable
fun DonutChart(breakdown: List<CategorySpend>) {
    Canvas(modifier = Modifier.size(200.dp)) {
        var startAngle = -90f
        breakdown.forEach { spend ->
            val sweepAngle = spend.percentage * 360f
            drawArc(
                color = Color(android.graphics.Color.parseColor(spend.colorHex)),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 40f, cap = StrokeCap.Butt)
            )
            startAngle += sweepAngle
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
                "${(spend.percentage * 100).toInt()}% of total spend",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Text(
            "$currencySymbol${String.format("%.2f", spend.amount)}",
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
            amount >= 1000000.0 -> "$currencySymbol${String.format("%.1f", amount / 1000000.0)}M"
            amount >= 1000.0 -> "$currencySymbol${String.format("%.1f", amount / 1000.0)}k"
            else -> "$currencySymbol${String.format("%.0f", amount)}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Monthly Spending Trend",
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
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                history.forEach { spend ->
                    val proportion = (spend.amount / maxAmount).toFloat().coerceIn(0.01f, 1f)
                    
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
                            fontSize = 9.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(proportion * 0.8f)
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
