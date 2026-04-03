package com.habit.app.data.mapper

import com.habit.app.data.local.HabitEntity
import com.habit.app.domain.decodeWeekdays
import com.habit.app.domain.encodeWeekdays
import com.habit.app.domain.model.Habit

fun HabitEntity.toDomain(): Habit = Habit(
    id = id,
    title = title,
    description = description,
    activeWeekdays = activeWeekdays.decodeWeekdays(),
    createdAtEpochDay = createdAtEpochDay,
    isOneTime = isOneTime,
    targetDateEpochDay = targetDateEpochDay,
)

fun Habit.toEntity(): HabitEntity = HabitEntity(
    id = id,
    title = title,
    description = description,
    activeWeekdays = activeWeekdays.encodeWeekdays(),
    createdAtEpochDay = createdAtEpochDay,
    isOneTime = isOneTime,
    targetDateEpochDay = targetDateEpochDay,
)
