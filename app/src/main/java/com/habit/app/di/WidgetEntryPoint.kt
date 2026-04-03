package com.habit.app.di

import com.habit.app.data.preferences.UserPreferences
import com.habit.app.data.repository.HabitRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun habitRepository(): HabitRepository
    fun userPreferences(): UserPreferences
}
