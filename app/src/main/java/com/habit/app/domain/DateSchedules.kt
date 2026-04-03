package com.habit.app.domain

import com.habit.app.domain.model.Habit
import java.time.LocalDate

fun Habit.isScheduledOn(date: LocalDate): Boolean =
    if (isOneTime) targetDateEpochDay == date.toEpochDay()
    else activeWeekdays.contains(date.dayOfWeek.value)

fun Set<Int>.encodeWeekdays(): String = sorted().joinToString(",")

fun String.decodeWeekdays(): Set<Int> =
    if (isBlank()) emptySet()
    else split(",").mapNotNull { it.toIntOrNull() }.filter { it in 1..7 }.toSet()
