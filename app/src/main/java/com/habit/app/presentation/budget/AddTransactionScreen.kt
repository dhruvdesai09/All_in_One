package com.habit.app.presentation.budget

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habit.app.presentation.theme.AccentEmerald
import com.habit.app.presentation.theme.DangerRed
import com.habit.app.presentation.theme.ForestGreen
import com.habit.app.presentation.theme.SurfaceBorder
import com.habit.app.presentation.theme.SurfaceCard
import com.habit.app.presentation.theme.SurfaceElevated
import com.habit.app.presentation.theme.TextMuted
import com.habit.app.presentation.theme.TextSecondary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    preselectedBudgetId: Long = 0L,
    onBack: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel(),
) {
    val form by viewModel.txForm.collectAsStateWithLifecycle()
    val budgets by viewModel.allBudgets.collectAsStateWithLifecycle()
    val currency by viewModel.currencySymbol.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.initTransactionForm(budgetId = preselectedBudgetId) }
    LaunchedEffect(form.isSaved) { if (form.isSaved) { viewModel.resetTxForm(); onBack() } }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = form.selectedDate
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { ms ->
                        val date = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.onTxDate(date)
                    }
                    showDatePicker = false
                }) { Text("OK", color = AccentEmerald) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = TextSecondary) }
            },
        ) { DatePicker(state = state) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Log Transaction", style = MaterialTheme.typography.titleLarge,
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
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Type toggle
            TypeToggle(selected = form.type, onSelect = viewModel::onTxType)

            // Amount (large, prominent)
            Column {
                Text(
                    text = currency,
                    style = MaterialTheme.typography.displaySmall,
                    color = TextMuted,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                OutlinedTextField(
                    value = form.amountInput,
                    onValueChange = viewModel::onTxAmount,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0.00", color = TextMuted,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = if (form.type == "income") AccentEmerald else DangerRed,
                    ),
                    isError = form.amountError != null,
                    supportingText = form.amountError?.let { { Text(it, color = DangerRed) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors(),
                    singleLine = true,
                )
            }

            // Title
            SectionLabel("Title / Note")
            OutlinedTextField(
                value = form.title,
                onValueChange = viewModel::onTxTitle,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Grocery run", color = TextMuted) },
                isError = form.titleError != null,
                supportingText = form.titleError?.let { { Text(it, color = DangerRed) } },
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors(),
                singleLine = true,
            )

            // Date selector
            SectionLabel("Date")
            Card(
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null,
                        tint = AccentEmerald, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        form.selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", Locale.getDefault())),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Budget category (only for expenses)
            if (form.type == "expense" && budgets.isNotEmpty()) {
                SectionLabel("Category (optional)")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // "None" option
                    CategoryChip(
                        emoji = "❓",
                        name = "Uncategorised",
                        colorHex = "#6B7C7A",
                        selected = form.budgetId == 0L,
                        onClick = { viewModel.onTxBudget(0L) },
                    )
                    budgets.forEach { budget ->
                        CategoryChip(
                            emoji = budget.emoji,
                            name = budget.name,
                            colorHex = budget.colorHex,
                            selected = form.budgetId == budget.id,
                            onClick = { viewModel.onTxBudget(budget.id) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = viewModel::saveTransaction,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreen,
                    contentColor = Color.White,
                    disabledContainerColor = SurfaceElevated,
                    disabledContentColor = TextMuted,
                ),
                enabled = !form.isLoading,
            ) {
                Text("Save Transaction", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun TypeToggle(selected: String, onSelect: (String) -> Unit) {
    val incomeColor by animateColorAsState(
        if (selected == "income") AccentEmerald else TextMuted, label = "income_color")
    val expenseColor by animateColorAsState(
        if (selected == "expense") DangerRed else TextMuted, label = "expense_color")

    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        listOf("income" to "💵 Income", "expense" to "💸 Expense").forEach { (type, label) ->
            val isSelected = selected == type
            val bg by animateColorAsState(
                if (isSelected) if (type == "income") AccentEmerald.copy(alpha = 0.15f)
                else DangerRed.copy(alpha = 0.15f)
                else Color.Transparent, label = "type_bg_$type",
            )
            val textColor = if (type == "income") incomeColor else expenseColor
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                    .background(bg).clickable { onSelect(type) }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = textColor, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun CategoryChip(
    emoji: String,
    name: String,
    colorHex: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val accentColor = try { Color(colorHex.toColorInt()) } catch (e: Exception) { AccentEmerald }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) accentColor.copy(alpha = 0.12f) else SurfaceCard,
        ),
        shape = RoundedCornerShape(10.dp),
        border = if (selected) androidx.compose.foundation.BorderStroke(1.5.dp, accentColor) else null,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(emoji, fontSize = 18.sp)
            Text(name, style = MaterialTheme.typography.bodyMedium,
                color = if (selected) accentColor else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
        }
    }
}
