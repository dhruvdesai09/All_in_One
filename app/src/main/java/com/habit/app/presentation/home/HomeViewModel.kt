package com.habit.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habit.app.data.repository.DayIntensity
import com.habit.app.data.repository.HabitRepository
import com.habit.app.data.repository.HabitTodayItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val items: List<HabitTodayItem> = emptyList(),
    val heatmap: List<DayIntensity> = emptyList(),
    val today: LocalDate = LocalDate.now(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HabitRepository,
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.deleteExpiredOneTimeTasks(LocalDate.now())
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeTodayItems(LocalDate.now()),
        repository.observeHeatmap(
            LocalDate.now().minusWeeks(14),
            LocalDate.now(),
        ),
    ) { items, heat ->
        HomeUiState(items = items, heatmap = heat, today = LocalDate.now())
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HomeUiState(),
    )

    fun toggle(habitId: Long, completed: Boolean) {
        viewModelScope.launch {
            repository.setCompleted(habitId, LocalDate.now(), completed)
            repository.requestWidgetUpdate()
        }
    }
}
