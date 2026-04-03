package com.habit.app.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habit.app.data.repository.DayIntensity
import com.habit.app.data.repository.HabitRepository
import com.habit.app.domain.computeStreak
import com.habit.app.domain.model.Habit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

private const val KEY_ID = "habitId"

data class HabitDetailUiState(
    val habit: Habit? = null,
    val heatmap: List<DayIntensity> = emptyList(),
    val streak: Int = 0,
)

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    repository: HabitRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val habitId: Long = checkNotNull(savedStateHandle[KEY_ID])
    private val rangeStart = LocalDate.now().minusWeeks(20)
    private val rangeEnd = LocalDate.now()

    val uiState: StateFlow<HabitDetailUiState> = combine(
        repository.observeAllHabits(),
        repository.observeHabitHeatmap(habitId, rangeStart, rangeEnd),
    ) { habits, heat ->
        val habit = habits.firstOrNull { it.id == habitId }
        val today = LocalDate.now()
        val streak = if (habit != null) {
            computeStreak(habit, today) { d ->
                heat.find { it.date == d }
                    ?.let { it.hadScheduledHabits && it.intensity >= 1f } == true
            }
        } else {
            0
        }
        HabitDetailUiState(habit = habit, heatmap = heat, streak = streak)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HabitDetailUiState(),
    )
}
