package com.mate.subsmate.ui.insights

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.flow.first
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Info

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
    val money: (Double) -> String = { "$currencySymbol${String.format("%,.2f", it)}" }
    val formattedTotal = money(displayedTotal)
    val displayedRanking = if (showingForecast) uiState.forecastRanking else uiState.monthlyRanking
    val estimateNotes = buildList {
        if (uiState.forecast.overdueCharges > 0) {
            val count = uiState.forecast.overdueCharges
            add("${money(uiState.forecast.overdueAmount)} from $count overdue ${if (count == 1) "charge" else "charges"} is not included.")
        }
        if (uiState.forecast.hasUnknownAmounts) add("Variable-price charges without a saved amount are not included.")
        if (uiState.forecast.usesVariableAmounts) add("Variable-price charges use their saved amount; actual charges may differ.")
    }
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
                if (!hasOverview && uiState.timeline.isEmpty()) {
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
                        if (showingForecast && estimateNotes.isNotEmpty()) {
                            item { EstimateNotes(estimateNotes) }
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
                        items(displayedBreakdown, key = { it.categoryId }) { categorySpend ->
                            CategorySpendItem(
                                spend = categorySpend,
                                subscriptions = displayedRanking.filter { it.categoryId == categorySpend.categoryId },
                                money = money
                            )
                        }
                    }
                    if (displayedRanking.isNotEmpty()) {
                        item { TopSubscriptions(displayedRanking, showingForecast, displayedTotal, money) }
                    }
                    if (uiState.timeline.isNotEmpty()) {
                        item { SpendingTimelineChart(uiState.timeline, money) }
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
private fun EstimateNotes(notes: List<String>) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClickLabel = if (expanded) "Hide details" else "Show details") { expanded = !expanded }
            .padding(vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Text("About this estimate", style = MaterialTheme.typography.bodySmall, color = color, modifier = Modifier.weight(1f))
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(start = 24.dp, top = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                notes.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall, color = color) }
            }
        }
    }
}

@Composable
fun CategorySpendItem(spend: CategorySpend, subscriptions: List<SubscriptionSpend>, money: (Double) -> String) {
    var expanded by rememberSaveable(spend.categoryId) { mutableStateOf(false) }
    val canExpand = subscriptions.isNotEmpty()
    val count = subscriptions.size
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (canExpand) Modifier.clickable(onClickLabel = if (expanded) "Hide subscriptions" else "Show subscriptions") { expanded = !expanded }
                else Modifier
            )
            .padding(vertical = 4.dp)
    ) {
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
                    "${PercentageUtils.label(spend.percentage)} of total spend" +
                        if (canExpand) " • $count ${if (count == 1) "subscription" else "subscriptions"}" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                money(spend.amount),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (canExpand) {
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(start = 24.dp, end = 32.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                subscriptions.forEach { sub ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            sub.name,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            money(sub.amount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopSubscriptions(
    ranking: List<SubscriptionSpend>,
    showingForecast: Boolean,
    total: Double,
    money: (Double) -> String
) {
    var showAll by rememberSaveable { mutableStateOf(false) }
    val shown = if (showAll) ranking else ranking.take(TOP_SUBSCRIPTION_COUNT)
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column {
            Text("Top Subscriptions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                if (showingForecast) "Ranked by cost over the next 12 months" else "Ranked by monthly cost",
                style = MaterialTheme.typography.bodySmall,
                color = muted
            )
        }
        shown.forEachIndexed { index, sub ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "${index + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = muted,
                    modifier = Modifier.width(20.dp)
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(sub.colorHex)))
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(sub.name, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        when {
                            showingForecast -> "${PercentageUtils.label((sub.amount / total).toFloat())} of the 12-month total"
                            sub.nextTwelveMonths > 0.0 -> "${money(sub.nextTwelveMonths)} over 12 months"
                            else -> "No charges in the next 12 months"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(money(sub.amount), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        if (ranking.size > TOP_SUBSCRIPTION_COUNT) {
            TextButton(onClick = { showAll = !showAll }) {
                Text(if (showAll) "Show less" else "Show all ${ranking.size}")
            }
        }
    }
}

private const val TOP_SUBSCRIPTION_COUNT = 5
private val TimelineColumnWidth = 44.dp
private val TimelineColumnGap = 4.dp

private fun compactAmount(amount: Double): String = when {
    amount >= 1_000_000.0 -> "${String.format("%.1f", amount / 1_000_000.0)}M"
    amount >= 1_000.0 -> "${String.format("%.1f", amount / 1_000.0)}k"
    else -> String.format("%.0f", amount)
}

@Composable
fun SpendingTimelineChart(timeline: List<TimelineMonth>, money: (Double) -> String) {
    val maxAmount = timeline.maxOf { it.total }.takeIf { it > 0.0 } ?: 1.0
    val paidColor = MaterialTheme.colorScheme.primary
    val scheduledColor = paidColor.copy(alpha = 0.45f)
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val currentIndex = timeline.indexOfFirst { it.isCurrent }.coerceAtLeast(0)
    val peak = timeline.maxBy { it.total }
    val description = "Spending timeline. Paid over the last 6 months: ${money(timeline.sumOf { it.paid })}. " +
        "Scheduled from today: ${money(timeline.sumOf { it.forecast })}. " +
        "Highest month: ${peak.label}, ${money(peak.total)}."

    val barProgress = remember { Animatable(0f) }
    LaunchedEffect(timeline) {
        barProgress.animateTo(1f, tween(durationMillis = 800, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        // Start with the current month near the left edge so recent history and the months ahead are both visible.
        val maxScroll = snapshotFlow { scrollState.maxValue }.first { it > 0 }
        val offset = with(density) { ((TimelineColumnWidth + TimelineColumnGap) * (currentIndex - 2).coerceAtLeast(0)).roundToPx() }
        scrollState.scrollTo(offset.coerceAtMost(maxScroll))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text(
                "Spending Timeline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Actual payments and scheduled charges by month",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendItem(paidColor, "Paid")
            LegendItem(scheduledColor, "Scheduled")
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .horizontalScroll(scrollState)
                .semantics { contentDescription = description },
            horizontalArrangement = Arrangement.spacedBy(TimelineColumnGap),
            verticalAlignment = Alignment.Bottom
        ) {
            timeline.forEach { month ->
                val labelColor = if (month.isCurrent) paidColor else textColor
                Column(
                    modifier = Modifier
                        .width(TimelineColumnWidth)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (month.total > 0.0) {
                        Text(
                            compactAmount(month.total),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (month.paid > 0.0) paidColor else textColor,
                            fontSize = 10.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth()
                            .fillMaxHeight((month.total / maxAmount).toFloat() * 0.8f * barProgress.value)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    ) {
                        if (month.forecast > 0.0) {
                            Box(Modifier.fillMaxWidth().weight(month.forecast.toFloat()).background(scheduledColor))
                        }
                        if (month.paid > 0.0) {
                            Box(Modifier.fillMaxWidth().weight(month.paid.toFloat()).background(paidColor))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        month.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (month.isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = labelColor,
                        fontSize = 10.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
