package com.mate.subsmate.ui.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(viewModel: InsightsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

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
                        "Category Breakdown",
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
                                    "$${String.format("%.2f", uiState.totalMonthlySpend)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    items(uiState.categoryBreakdown) { categorySpend ->
                        CategorySpendItem(categorySpend)
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
fun CategorySpendItem(spend: CategorySpend) {
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
            "$${String.format("%.2f", spend.amount)}",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
