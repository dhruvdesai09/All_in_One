package com.habit.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habit.app.data.preferences.UserPreferences
import com.habit.app.data.preferences.UserPreferencesState
import com.habit.app.data.repository.HabitRepository
import com.habit.app.domain.WidgetDisplayMode
import com.habit.app.workers.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val notificationScheduler: NotificationScheduler,
    private val habitRepository: HabitRepository,
) : ViewModel() {

    val prefs: StateFlow<UserPreferencesState> = userPreferences.flow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        UserPreferencesState(),
    )

    fun setReminder(hour: Int, minute: Int) {
        viewModelScope.launch {
            userPreferences.setReminderTime(hour, minute)
            notificationScheduler.scheduleFromCurrentPreferences()
        }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setRemindersEnabled(enabled)
            notificationScheduler.scheduleFromCurrentPreferences()
        }
    }

    fun setWidgetMode(mode: WidgetDisplayMode) {
        viewModelScope.launch {
            userPreferences.setWidgetMode(mode)
            habitRepository.requestWidgetUpdate()
        }
    }
}
