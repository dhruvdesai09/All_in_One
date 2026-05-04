package com.habit.app.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habit.app.data.local.TransactionEntity
import com.habit.app.data.preferences.UserPreferences
import com.habit.app.data.repository.BudgetItem
import com.habit.app.data.repository.BudgetRepository
import com.habit.app.data.repository.MonthSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetOverviewViewModel @Inject constructor(
    private val repo: BudgetRepository,
    private val prefs: UserPreferences,
) : ViewModel() {

    // ── Selected month ────────────────────────────────────────────────
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    fun previousMonth() { _selectedMonth.value = _selectedMonth.value.minusMonths(1) }
    fun nextMonth() {
        val next = _selectedMonth.value.plusMonths(1)
        if (!next.isAfter(YearMonth.now())) _selectedMonth.value = next
    }

    // ── Currency ──────────────────────────────────────────────────────
    val currencySymbol: StateFlow<String> = prefs.flow
        .map { it.currencySymbol }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "₹")

    // ── Data streams ──────────────────────────────────────────────────
    val budgetItems: StateFlow<List<BudgetItem>> = _selectedMonth
        .flatMapLatest { repo.observeBudgetsWithSpend(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val monthSummary: StateFlow<MonthSummary> = _selectedMonth
        .flatMapLatest { repo.observeMonthSummary(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MonthSummary(0L, 0L))

    val recentTransactions: StateFlow<List<TransactionEntity>> = _selectedMonth
        .flatMapLatest { repo.observeTransactionsByMonth(it) }
        .map { it.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val fullMonthTransactions: StateFlow<List<TransactionEntity>> = _selectedMonth
        .flatMapLatest { repo.observeTransactionsByMonth(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
