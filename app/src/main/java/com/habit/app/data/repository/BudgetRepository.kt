package com.habit.app.data.repository

import com.habit.app.data.local.BudgetDao
import com.habit.app.data.local.BudgetEntity
import com.habit.app.data.local.TransactionDao
import com.habit.app.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

data class BudgetItem(
    val entity: BudgetEntity,
    val spentCents: Long,
) {
    val progressFraction: Float
        get() = if (entity.limitCents == 0L) 0f
                else (spentCents.toFloat() / entity.limitCents.toFloat()).coerceAtLeast(0f)
    val isOverBudget: Boolean get() = spentCents > entity.limitCents
}

data class MonthSummary(
    val incomeCents: Long,
    val expenseCents: Long,
) {
    val netCents: Long get() = incomeCents - expenseCents
}

@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao,
    private val txDao: TransactionDao,
) {

    fun observeAllBudgets(): Flow<List<BudgetEntity>> = budgetDao.observeAll()

    fun observeTransactionsByMonth(month: YearMonth): Flow<List<TransactionEntity>> {
        val (start, end) = month.epochDayRange()
        return txDao.observeByMonth(start, end)
    }

    fun observeBudgetsWithSpend(month: YearMonth): Flow<List<BudgetItem>> {
        val (start, end) = month.epochDayRange()
        return combine(
            budgetDao.observeAll(),
            txDao.observeByMonth(start, end),
        ) { budgets, txs ->
            budgets.map { budget ->
                val spent = txs.filter { it.budgetId == budget.id && it.type == "expense" }
                    .sumOf { it.amountCents }
                BudgetItem(budget, spent)
            }
        }
    }

    fun observeMonthSummary(month: YearMonth): Flow<MonthSummary> {
        val (start, end) = month.epochDayRange()
        return txDao.observeByMonth(start, end).map { txs ->
            val income = txs.filter { it.type == "income" }.sumOf { it.amountCents }
            val expense = txs.filter { it.type == "expense" }.sumOf { it.amountCents }
            MonthSummary(income, expense)
        }
    }

    suspend fun upsertBudget(
        id: Long = 0,
        name: String,
        emoji: String,
        colorHex: String,
        limitCents: Long,
    ): Long = budgetDao.upsert(
        BudgetEntity(id = id, name = name, emoji = emoji, colorHex = colorHex, limitCents = limitCents)
    )

    suspend fun deleteBudget(id: Long) = budgetDao.deleteById(id)

    suspend fun getBudgetById(id: Long): BudgetEntity? = budgetDao.getById(id)

    suspend fun addTransaction(
        title: String,
        amountCents: Long,
        type: String,
        budgetId: Long,
        epochDay: Long,
    ) = txDao.insert(
        TransactionEntity(
            title = title,
            amountCents = amountCents,
            type = type,
            budgetId = budgetId,
            epochDay = epochDay,
            createdAt = System.currentTimeMillis(),
        )
    )

    suspend fun deleteTransaction(id: Long) = txDao.deleteById(id)

    suspend fun getTransactionById(id: Long): TransactionEntity? = txDao.getById(id)

    fun observeAllTransactions(): Flow<List<TransactionEntity>> = txDao.observeAll()
}

private fun YearMonth.epochDayRange(): Pair<Long, Long> {
    val start = atDay(1).toEpochDay()
    val end = atEndOfMonth().toEpochDay()
    return start to end
}
