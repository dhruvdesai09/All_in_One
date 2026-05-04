package com.habit.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amountCents: Long,      // always positive; type field determines sign
    val type: String,           // "income" | "expense"
    val budgetId: Long,         // 0 = no budget (income or uncategorised)
    val epochDay: Long,         // LocalDate.toEpochDay()
    val createdAt: Long,        // System.currentTimeMillis()
)
