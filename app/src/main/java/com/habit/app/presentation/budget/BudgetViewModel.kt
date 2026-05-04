package com.habit.app.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habit.app.data.local.BudgetEntity
import com.habit.app.data.local.TransactionEntity
import com.habit.app.data.preferences.UserPreferences
import com.habit.app.data.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

// ── Preset colour & emoji options ─────────────────────────────────────────────

val PRESET_COLORS = listOf(
    "#1E88E5", "#FFB300", "#00897B", "#8E24AA",
    "#E91E63", "#FF6D00", "#3949AB", "#00ACC1",
    "#43A047", "#EF5350",
)

val PRESET_EMOJIS = listOf(
    "🍔", "🚗", "🏠", "💊", "🛍️", "🎮", "✈️", "📚",
    "💡", "🎵", "☕", "🐾", "💪", "🎁", "🍕", "📱",
    "🏋️", "🎬", "👗", "💰",
)

// ── Budget form state ─────────────────────────────────────────────────────────

data class BudgetFormState(
    val id: Long = 0L,
    val name: String = "",
    val emoji: String = "💰",
    val colorHex: String = "#1E88E5",
    val limitInput: String = "",
    val nameError: String? = null,
    val limitError: String? = null,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false,
)

// ── Transaction form state ────────────────────────────────────────────────────

data class TransactionFormState(
    val id: Long = 0L,
    val title: String = "",
    val amountInput: String = "",
    val type: String = "expense",          // "income" | "expense"
    val budgetId: Long = 0L,
    val selectedDate: LocalDate = LocalDate.now(),
    val titleError: String? = null,
    val amountError: String? = null,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false,
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repo: BudgetRepository,
    private val prefs: UserPreferences,
) : ViewModel() {

    // ── Budget form ───────────────────────────────────────────────────
    private val _budgetForm = MutableStateFlow(BudgetFormState())
    val budgetForm: StateFlow<BudgetFormState> = _budgetForm.asStateFlow()

    fun loadBudgetForEdit(id: Long?) {
        _budgetForm.value = BudgetFormState(isLoading = id != null)
        if (id == null) return
        viewModelScope.launch {
            repo.getBudgetById(id)?.let { b ->
                _budgetForm.value = BudgetFormState(
                    id = b.id,
                    name = b.name,
                    emoji = b.emoji,
                    colorHex = b.colorHex,
                    limitInput = (b.limitCents / 100.0).toLong().toString(),
                )
            }
        }
    }

    fun onBudgetName(v: String) = _budgetForm.update { it.copy(name = v, nameError = null) }
    fun onBudgetEmoji(v: String) = _budgetForm.update { it.copy(emoji = v) }
    fun onBudgetColor(v: String) = _budgetForm.update { it.copy(colorHex = v) }
    fun onBudgetLimit(v: String) = _budgetForm.update { it.copy(limitInput = v, limitError = null) }

    fun saveBudget() {
        val f = _budgetForm.value
        var hasError = false
        if (f.name.isBlank()) {
            _budgetForm.update { it.copy(nameError = "Name is required") }
            hasError = true
        }
        val limitCents = f.limitInput.trim().toDoubleOrNull()?.let { (it * 100).toLong() }
        if (limitCents == null || limitCents <= 0) {
            _budgetForm.update { it.copy(limitError = "Enter a valid amount") }
            hasError = true
        }
        if (hasError) return

        _budgetForm.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            repo.upsertBudget(
                id = f.id,
                name = f.name.trim(),
                emoji = f.emoji,
                colorHex = f.colorHex,
                limitCents = limitCents!!,
            )
            _budgetForm.update { it.copy(isLoading = false, isSaved = true) }
        }
    }

    fun deleteBudget(id: Long, onDone: () -> Unit) {
        viewModelScope.launch { repo.deleteBudget(id); onDone() }
    }

    fun resetBudgetForm() { _budgetForm.value = BudgetFormState() }

    // ── All budgets (for category picker) ────────────────────────────
    val allBudgets: StateFlow<List<BudgetEntity>> = repo.observeAllBudgets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Currency symbol ───────────────────────────────────────────────
    val currencySymbol: StateFlow<String> = prefs.flow
        .map { it.currencySymbol }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "₹")

    // ── Transaction form ──────────────────────────────────────────────
    private val _txForm = MutableStateFlow(TransactionFormState())
    val txForm: StateFlow<TransactionFormState> = _txForm.asStateFlow()

    fun initTransactionForm(budgetId: Long = 0L, type: String = "expense") {
        _txForm.value = TransactionFormState(budgetId = budgetId, type = type)
    }

    fun onTxTitle(v: String) = _txForm.update { it.copy(title = v, titleError = null) }
    fun onTxAmount(v: String) = _txForm.update { it.copy(amountInput = v, amountError = null) }
    fun onTxType(v: String) = _txForm.update { it.copy(type = v, budgetId = if (v == "income") 0L else it.budgetId) }
    fun onTxBudget(id: Long) = _txForm.update { it.copy(budgetId = id) }
    fun onTxDate(date: LocalDate) = _txForm.update { it.copy(selectedDate = date) }

    fun saveTransaction() {
        val f = _txForm.value
        var hasError = false
        if (f.title.isBlank()) {
            _txForm.update { it.copy(titleError = "Title is required") }
            hasError = true
        }
        val amountCents = f.amountInput.trim().toDoubleOrNull()?.let { (it * 100).toLong() }
        if (amountCents == null || amountCents <= 0) {
            _txForm.update { it.copy(amountError = "Enter a valid amount") }
            hasError = true
        }
        if (hasError) return

        _txForm.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            repo.addTransaction(
                title = f.title.trim(),
                amountCents = amountCents!!,
                type = f.type,
                budgetId = f.budgetId,
                epochDay = f.selectedDate.toEpochDay(),
            )
            _txForm.update { it.copy(isLoading = false, isSaved = true) }
        }
    }

    fun deleteTransaction(id: Long, onDone: () -> Unit) {
        viewModelScope.launch { repo.deleteTransaction(id); onDone() }
    }

    fun resetTxForm() { _txForm.value = TransactionFormState() }
}
