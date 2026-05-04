package com.habit.app.presentation.budget

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habit.app.data.local.TransactionEntity
import com.habit.app.data.repository.BudgetItem
import com.habit.app.presentation.theme.AccentEmerald
import com.habit.app.presentation.theme.DangerRed
import com.habit.app.presentation.theme.SurfaceCard
import com.habit.app.presentation.theme.SurfaceElevated
import com.habit.app.presentation.theme.TextMuted
import com.habit.app.presentation.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    onBack: () -> Unit,
    overviewViewModel: BudgetOverviewViewModel = hiltViewModel(),
    budgetViewModel: BudgetViewModel = hiltViewModel(),
) {
    val month by overviewViewModel.selectedMonth.collectAsStateWithLifecycle()
    val allTx by overviewViewModel.fullMonthTransactions.collectAsStateWithLifecycle()
    val budgetItems by overviewViewModel.budgetItems.collectAsStateWithLifecycle()
    val currency by overviewViewModel.currencySymbol.collectAsStateWithLifecycle()

    var typeFilter by rememberSaveable { mutableStateOf("all") }

    val grouped = allTx
        .let { txs ->
            when (typeFilter) {
                "income" -> txs.filter { it.type == "income" }
                "expense" -> txs.filter { it.type == "expense" }
                else -> txs
            }
        }
        .groupBy { LocalDate.ofEpochDay(it.epochDay) }
        .entries.sortedByDescending { it.key }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Transactions", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                },
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
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Month selector
            item { MonthSelector(month, onPrev = overviewViewModel::previousMonth,
                onNext = overviewViewModel::nextMonth) }

            // Type filter chips
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("all" to "All", "income" to "Income", "expense" to "Expenses")
                        .forEach { (type, label) ->
                            FilterChip(label = label, selected = typeFilter == type,
                                onClick = { typeFilter = type })
                        }
                }
            }

            if (grouped.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(vertical = 64.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🧾", fontSize = 48.sp)
                            Text("No transactions", style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface)
                            Text("Add one from the Budget tab",
                                style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                    }
                }
            } else {
                grouped.forEach { (date, txList) ->
                    item(key = "header_$date") {
                        Text(
                            text = formatDateHeader(date),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                        )
                    }
                    items(txList, key = { it.id }) { tx ->
                        SwipeToDeleteTx(
                            tx = tx,
                            currency = currency,
                            budgets = budgetItems,
                            onDelete = { budgetViewModel.deleteTransaction(tx.id) {} },
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteTx(
    tx: TransactionEntity,
    currency: String,
    budgets: List<BudgetItem>,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) { onDelete(); true } else false
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                    .background(DangerRed.copy(alpha = 0.15f)),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete",
                    tint = DangerRed, modifier = Modifier.padding(end = 20.dp))
            }
        },
        content = { TxHistoryRow(tx = tx, currency = currency, budgets = budgets) },
    )
}

@Composable
private fun TxHistoryRow(
    tx: TransactionEntity,
    currency: String,
    budgets: List<BudgetItem>,
) {
    val isIncome = tx.type == "income"
    val amountColor = if (isIncome) AccentEmerald else DangerRed
    val budget = budgets.find { it.entity.id == tx.budgetId }

    Surface(color = SurfaceCard, shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape)
                    .background(amountColor.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(budget?.entity?.emoji ?: if (isIncome) "💵" else "💸", fontSize = 18.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(tx.title, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = budget?.entity?.name ?: if (isIncome) "Income" else "Uncategorised",
                    style = MaterialTheme.typography.bodySmall, color = TextMuted,
                )
            }
            Text(
                text = "${if (isIncome) "+" else "-"}$currency${formatAmount(tx.amountCents)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = amountColor,
            )
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(
        if (selected) AccentEmerald.copy(alpha = 0.15f) else SurfaceElevated, label = "chip_bg")
    val textColor by animateColorAsState(
        if (selected) AccentEmerald else TextSecondary, label = "chip_text")
    Surface(color = bg, shape = CircleShape, modifier = Modifier.clickable(onClick = onClick)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = textColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
    }
}

private fun formatDateHeader(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault()))
    }
}
