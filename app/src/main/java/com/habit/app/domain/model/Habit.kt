package com.habit.app.domain.model

data class Habit(
    val id: Long = 0,
    val title: String,
    val description: String?,
    /** ISO DayOfWeek value 1..7 (Monday..Sunday) */
    val activeWeekdays: Set<Int>,
    val createdAtEpochDay: Long,
    val isOneTime: Boolean = false,
    val targetDateEpochDay: Long? = null,
)
