package com.habit.app.presentation.budget

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habit.app.data.repository.BudgetItem
import com.habit.app.presentation.theme.AccentEmerald
import com.habit.app.presentation.theme.SurfaceCard
import com.habit.app.presentation.theme.SurfaceElevated
import com.habit.app.presentation.theme.TextMuted
import com.habit.app.presentation.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    onBack: () -> Unit,
    viewModel: BudgetOverviewViewModel = hiltViewModel(),
) {
    val month by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val budgets by viewModel.budgetItems.collectAsStateWithLifecycle()
    val summary by viewModel.monthSummary.collectAsStateWithLifecycle()
    val currency by viewModel.currencySymbol.collectAsStateWithLifecycle()

    // Only show budgets that have actual spending
    val spendingBudgets = budgets.filter { it.spentCents > 0 }
    val totalSpent = spendingBudgets.sumOf { it.spentCents }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Insights", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Month selector
            item { MonthSelector(month, onPrev = viewModel::previousMonth, onNext = viewModel::nextMonth) }

            // Summary row
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InsightChip("Total Spent", "$currency${formatAmount(summary.expenseCents)}",
                        com.habit.app.presentation.theme.DangerRed, Modifier.weight(1f))
                    InsightChip("Total Income", "$currency${formatAmount(summary.incomeCents)}",
                        AccentEmerald, Modifier.weight(1f))
                }
            }

            // Donut chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Spending Breakdown",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(24.dp))

                        if (spendingBudgets.isEmpty()) {
                            Box(
                                modifier = Modifier.size(200.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("No spending\nthis month", color = TextMuted,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                        } else {
                            DonutChart(
                                items = spendingBudgets,
                                total = totalSpent,
                                currency = currency,
                            )
                        }
                    }
                }
            }

            // Legend / breakdown list
            if (spendingBudgets.isNotEmpty()) {
                item {
                    Text("Category Breakdown",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                items(spendingBudgets.sortedByDescending { it.spentCents }, key = { it.entity.id }) { item ->
                    InsightBudgetRow(item = item, total = totalSpent, currency = currency)
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ── Donut Chart ───────────────────────────────────────────────────────────────

@Composable
private fun DonutChart(items: List<BudgetItem>, total: Long, currency: String) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(items) { animProgress.animateTo(1f, tween(900)) }
    val progress = animProgress.value

    val colors = items.map { item ->
        try { Color(item.entity.colorHex.toColorInt()) } catch (e: Exception) { AccentEmerald }
    }
    val sweeps = items.map { item ->
        (item.spentCents.toFloat() / total.toFloat()) * 360f * progress
    }

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val strokeWidth = 36.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f
            val topLeft = Offset((size.width - radius * 2) / 2f, (size.height - radius * 2) / 2f)
            val arcSize = Size(radius * 2, radius * 2)

            var startAngle = -90f
            sweeps.forEachIndexed { index, sweep ->
                drawArc(
                    color = colors[index],
                    startAngle = startAngle,
                    sweepAngle = sweep - 2f, // small gap between arcs
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
                startAngle += sweep
            }
        }
        // Centre label
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Spent", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Text(
                text = "$currency${formatAmount(total)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

// ── Insight chips ─────────────────────────────────────────────────────────────

@Composable
private fun InsightChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.8f))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = color)
        }
    }
}

// ── Legend row ────────────────────────────────────────────────────────────────

@Composable
private fun InsightBudgetRow(item: BudgetItem, total: Long, currency: String) {
    val accentColor = try { Color(item.entity.colorHex.toColorInt()) } catch (e: Exception) { AccentEmerald }
    val pct = if (total == 0L) 0 else ((item.spentCents * 100) / total).toInt()

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Color dot
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) { Text(item.entity.emoji, fontSize = 16.sp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.entity.name, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                Text("$currency${formatAmount(item.spentCents)}",
                    style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Surface(
                color = accentColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("$pct%", style = MaterialTheme.typography.labelMedium,
                    color = accentColor, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}
