package com.habit.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String?,
    /** ISO weekdays 1..7 (Mon..Sun), comma-separated e.g. "1,3,5" */
    val activeWeekdays: String,
    val createdAtEpochDay: Long,
    val isOneTime: Boolean = false,
    val targetDateEpochDay: Long? = null,
)
