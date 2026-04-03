package com.habit.app.presentation.addedithabit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habit.app.data.repository.HabitRepository
import com.habit.app.domain.model.Habit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

private const val KEY_HABIT_ID = "habitId"

data class AddEditUiState(
    val habitId: Long? = null,
    val title: String = "",
    val description: String = "",
    val isOneTime: Boolean = false,
    val targetDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val activeDays: Set<Int> = (1..7).toSet(),
    val saved: Boolean = false,
    val deleted: Boolean = false,
)

@HiltViewModel
class AddEditHabitViewModel @Inject constructor(
    private val repository: HabitRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(
        AddEditUiState(habitId = savedStateHandle.get<Long>(KEY_HABIT_ID)),
    )
    val state: StateFlow<AddEditUiState> = _state.asStateFlow()

    init {
        val id = savedStateHandle.get<Long>(KEY_HABIT_ID)
        if (id != null) {
            viewModelScope.launch {
                repository.getHabit(id)?.let { h ->
                    _state.update {
                        it.copy(
                            habitId = h.id,
                            title = h.title,
                            description = h.description.orEmpty(),
                            activeDays = h.activeWeekdays,
                            isOneTime = h.isOneTime,
                            targetDateEpochDay = h.targetDateEpochDay ?: LocalDate.now().toEpochDay(),
                        )
                    }
                }
            }
        }
    }

    fun setTitle(v: String) = _state.update { it.copy(title = v) }
    fun setDescription(v: String) = _state.update { it.copy(description = v) }
    fun setIsOneTime(v: Boolean) = _state.update { it.copy(isOneTime = v) }
    fun setTargetDate(epochDay: Long) = _state.update { it.copy(targetDateEpochDay = epochDay) }
    fun toggleDay(day: Int) = _state.update { s ->
        val next = s.activeDays.toMutableSet()
        if (day in next) next.remove(day) else next.add(day)
        if (next.isEmpty()) s else s.copy(activeDays = next)
    }

    fun save() {
        val s = _state.value
        val t = s.title.trim()
        if (t.isEmpty() || (!s.isOneTime && s.activeDays.isEmpty())) return
        viewModelScope.launch {
            val existing = s.habitId?.let { repository.getHabit(it) }
            val habit = Habit(
                id = s.habitId ?: 0L,
                title = t,
                description = s.description.trim().ifBlank { null },
                activeWeekdays = s.activeDays,
                createdAtEpochDay = existing?.createdAtEpochDay ?: LocalDate.now().toEpochDay(),
                isOneTime = s.isOneTime,
                targetDateEpochDay = if (s.isOneTime) s.targetDateEpochDay else null,
            )
            if (s.habitId == null) {
                repository.insertHabit(habit)
            } else {
                repository.updateHabit(habit.copy(id = s.habitId))
            }
            repository.requestWidgetUpdate()
            _state.update { it.copy(saved = true) }
        }
    }

    fun delete() {
        val id = _state.value.habitId ?: return
        viewModelScope.launch {
            repository.getHabit(id)?.let { repository.deleteHabit(it) }
            repository.requestWidgetUpdate()
            _state.update { it.copy(deleted = true) }
        }
    }
}
