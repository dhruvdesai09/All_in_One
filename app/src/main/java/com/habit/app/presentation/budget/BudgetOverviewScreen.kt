package com.habit.app.presentation.budget

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habit.app.data.local.TransactionEntity
import com.habit.app.data.repository.BudgetItem
import com.habit.app.data.repository.MonthSummary
import com.habit.app.presentation.theme.NexoraGold
import com.habit.app.presentation.theme.NexoraMint
import com.habit.app.presentation.theme.NexoraRose
import com.habit.app.presentation.theme.SurfaceBorder
import com.habit.app.presentation.theme.SurfaceElevated
import com.habit.app.presentation.theme.TextMuted
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun BudgetOverviewScreen(
    onAddBudget: () -> Unit,
    onEditBudget: (Long) -> Unit,
    onAddTransaction: () -> Unit,
    onHistory: () -> Unit,
    onInsights: () -> Unit,
    viewModel: BudgetOverviewViewModel = hiltViewModel(),
) {
    val month by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val summary by viewModel.monthSummary.collectAsStateWithLifecycle()
    val budgets by viewModel.budgetItems.collectAsStateWithLifecycle()
    val recent by viewModel.recentTransactions.collectAsStateWithLifecycle()
    val currency by viewModel.currencySymbol.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                        .clickable(onClick = onAddBudget),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = NexoraMint, fontSize = 22.sp)
                }
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.linearGradient(listOf(NexoraRose, Color(0xFFA65B50))))
                        .clickable(onClick = onAddTransaction),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = Color.White, fontSize = 28.sp)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item { Spacer(Modifier.height(10.dp)) }

            // Month selector & Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MonthSelector(month, onPrev = viewModel::previousMonth, onNext = viewModel::nextMonth)
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                                .clickable(onClick = onInsights),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📊", fontSize = 16.sp)
                        }
                    }
                }
            }

            // Summary card
            item { SummaryCard(summary, currency, budgets) }

            // Budgets header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "TOP CATEGORIES",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    if (budgets.isNotEmpty()) {
                        Text(
                            "${budgets.size} total",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }

            if (budgets.isEmpty()) {
                item { BudgetEmptyCard(onAddBudget) }
            } else {
                items(budgets, key = { it.entity.id }) { item ->
                    BudgetCard(item = item, currency = currency, onClick = { onEditBudget(item.entity.id) })
                }
            }

            // Recent transactions header
            if (recent.isNotEmpty()) {
                item { Spacer(Modifier.height(4.dp)) }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "RECENT TRANSACTIONS",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            "SEE ALL",
                            style = MaterialTheme.typography.labelMedium,
                            color = NexoraRose,
                            modifier = Modifier.clickable { onHistory() }
                        )
                    }
                }
                items(recent, key = { "tx_${it.id}" }) { tx ->
                    RecentTransactionRow(tx = tx, currency = currency, budgets = budgets)
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

// ── Month Selector ────────────────────────────────────────────────────────────

@Composable
internal fun MonthSelector(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    val canGoNext = month.plusMonths(1) <= YearMonth.now()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SurfaceElevated)
                .border(1.dp, SurfaceBorder, CircleShape)
                .clickable(onClick = onPrev),
            contentAlignment = Alignment.Center
        ) {
            Text("←", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
        }
        
        Text(
            text = month.format(DateTimeFormatter.ofPattern("MMMM", Locale.getDefault())),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SurfaceElevated)
                .border(1.dp, SurfaceBorder, CircleShape)
                .clickable(enabled = canGoNext, onClick = onNext),
            contentAlignment = Alignment.Center
        ) {
            Text("→", color = if (canGoNext) MaterialTheme.colorScheme.onSurface else TextMuted, fontSize = 16.sp)
        }
    }
}

// ── Summary Card ──────────────────────────────────────────────────────────────

@Composable
private fun SummaryCard(summary: MonthSummary, currency: String, budgets: List<BudgetItem>) {
    val totalLimit = budgets.sumOf { it.entity.limitCents }
    val progressFrac = if (totalLimit > 0) summary.expenseCents.toFloat() / totalLimit else 0f
    val progAnim by animateFloatAsState(
        targetValue = progressFrac.coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "budget_progress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.radialGradient(listOf(NexoraRose.copy(alpha = 0.15f), Color.Transparent), radius = 600f))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "TOTAL SPENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Text(
                    text = "$currency ${formatAmount(summary.expenseCents)}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = NexoraRose,
                    fontSize = 42.sp
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LinearProgressIndicator(
                    progress = { progAnim },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = NexoraRose,
                    trackColor = SurfaceElevated,
                    strokeCap = StrokeCap.Round,
                )
                val left = (totalLimit - summary.expenseCents).coerceAtLeast(0)
                Text(
                    text = "$currency${formatAmount(left)} left of $currency${formatAmount(totalLimit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ── Budget Card ───────────────────────────────────────────────────────────────

@Composable
fun BudgetCard(item: BudgetItem, currency: String, onClick: () -> Unit) {
    val progress by animateFloatAsState(
        targetValue = item.progressFraction.coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "budget_progress",
    )
    val accentColor = try {
        Color(item.entity.colorHex.toColorInt())
    } catch (e: Exception) { NexoraMint }

    val progressColor = when {
        item.isOverBudget -> NexoraRose
        else -> accentColor
    }

    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Emoji avatar
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) { Text(item.entity.emoji, fontSize = 18.sp) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = item.entity.name, 
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$currency${formatAmount(item.spentCents)} / $currency${formatAmount(item.entity.limitCents)}",
                        style = MaterialTheme.typography.bodySmall, color = TextMuted,
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = "${(item.progressFraction * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = progressColor,
                    fontWeight = FontWeight.Bold,
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.background,
                strokeCap = StrokeCap.Round,
            )
        }
    }
}

// ── Empty budget state ────────────────────────────────────────────────────────

@Composable
private fun BudgetEmptyCard(onAdd: () -> Unit) {
    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onAdd)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("💰", fontSize = 40.sp)
            Text("No budgets yet", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface)
            Text("Tap + to create your first budget category",
                style = MaterialTheme.typography.bodySmall, color = TextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

// ── Recent transaction row ────────────────────────────────────────────────────

@Composable
private fun RecentTransactionRow(
    tx: TransactionEntity,
    currency: String,
    budgets: List<BudgetItem>,
) {
    val isIncome = tx.type == "income"
    val amountColor = if (isIncome) NexoraMint else MaterialTheme.colorScheme.onSurface
    val sign = if (isIncome) "+" else "-"
    val budget = budgets.find { it.entity.id == tx.budgetId }

    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                    .background(amountColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(budget?.entity?.emoji ?: if (isIncome) "💵" else "💸", fontSize = 16.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = tx.title, 
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = budget?.entity?.name ?: if (isIncome) "Income" else "Uncategorised",
                    style = MaterialTheme.typography.bodySmall, color = TextMuted,
                    fontSize = 11.sp
                )
            }
            Text(
                text = "$sign$currency${formatAmount(tx.amountCents)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = amountColor,
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

fun formatAmount(cents: Long): String {
    val whole = cents / 100
    val fraction = cents % 100
    return if (fraction == 0L) whole.toString()
    else "$whole.${fraction.toString().padStart(2, '0')}"
}
