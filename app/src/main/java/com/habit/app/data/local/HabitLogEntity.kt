package com.habit.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "habit_logs",
    primaryKeys = ["habitId", "dateEpochDay"],
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("habitId"), Index("dateEpochDay")],
)
data class HabitLogEntity(
    val habitId: Long,
    val dateEpochDay: Long,
    val isCompleted: Boolean,
)
