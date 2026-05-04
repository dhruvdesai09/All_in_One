package com.habit.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String,          // e.g. "🍔"
    val colorHex: String,       // e.g. "#FF6D00"
    val limitCents: Long,       // stored in smallest unit to avoid float errors
)
