package com.habit.app.data.repository

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.habit.app.data.local.AppDatabase
import com.habit.app.data.local.HabitLogEntity
import com.habit.app.data.mapper.toDomain
import com.habit.app.data.mapper.toEntity
import com.habit.app.domain.computeStreak
import com.habit.app.domain.isScheduledOn
import com.habit.app.domain.model.Habit
import com.habit.app.widget.HabitWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

data class HabitTodayItem(
    val habit: Habit,
    val completed: Boolean,
    val streak: Int,
)

data class DayIntensity(
    val date: LocalDate,
    /** 0f..1f portion of scheduled habits completed when [hadScheduledHabits] */
    val intensity: Float,
    val hadScheduledHabits: Boolean,
)

@Singleton
class HabitRepository @Inject constructor(
    private val db: AppDatabase,
    @ApplicationContext private val appContext: Context,
) {
    private val habitDao get() = db.habitDao()
    private val logDao get() = db.habitLogDao()

    fun observeAllHabits(): Flow<List<Habit>> =
        habitDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getHabit(id: Long): Habit? = withContext(Dispatchers.IO) {
        habitDao.getById(id)?.toDomain()
    }

    suspend fun insertHabit(habit: Habit): Long = withContext(Dispatchers.IO) {
        habitDao.insert(habit.copy(id = 0).toEntity())
    }

    suspend fun updateHabit(habit: Habit) = withContext(Dispatchers.IO) {
        habitDao.update(habit.toEntity())
    }

    suspend fun deleteHabit(habit: Habit) = withContext(Dispatchers.IO) {
        habitDao.delete(habit.toEntity())
    }

    suspend fun deleteExpiredOneTimeTasks(today: LocalDate) = withContext(Dispatchers.IO) {
        habitDao.deleteExpiredOneTime(today.toEpochDay())
        requestWidgetUpdate()
    }

    fun observeTodayItems(today: LocalDate): Flow<List<HabitTodayItem>> {
        val epoch = today.toEpochDay()
        return combine(habitDao.observeAll(), logDao.observeBetween(epoch, epoch), logDao.observeAllLogs()) {
            habits,
            todayLogs,
            allLogs,
            ->
            val completedMap = todayLogs.associate { it.habitId to it.isCompleted }
            val completedByHabit = allLogs
                .filter { it.isCompleted }
                .groupBy { it.habitId }
                .mapValues { (_, logs) -> logs.map { it.dateEpochDay }.toSet() }
            habits.map { it.toDomain() }
                .filter { it.isScheduledOn(today) }
                .map { h ->
                    val done = completedMap[h.id] == true
                    val doneDays = completedByHabit[h.id].orEmpty()
                    HabitTodayItem(
                        habit = h,
                        completed = done,
                        streak = computeStreak(h, today) { d -> doneDays.contains(d.toEpochDay()) },
                    )
                }
        }.flowOn(Dispatchers.Default)
    }

    suspend fun setCompleted(habitId: Long, date: LocalDate, completed: Boolean) =
        withContext(Dispatchers.IO) {
            val day = date.toEpochDay()
            if (completed) {
                logDao.upsert(HabitLogEntity(habitId, day, true))
            } else {
                logDao.delete(habitId, day)
            }
        }

    suspend fun isCompleted(habitId: Long, date: LocalDate): Boolean =
        withContext(Dispatchers.IO) {
            logDao.get(habitId, date.toEpochDay())?.isCompleted == true
        }

    suspend fun hasPendingScheduledHabits(date: LocalDate): Boolean = withContext(Dispatchers.IO) {
        val habits = habitDao.getAllOnce().map { it.toDomain() }.filter { it.isScheduledOn(date) }
        if (habits.isEmpty()) return@withContext false
        val logs = logDao.getForDay(date.toEpochDay()).associate { it.habitId to it.isCompleted }
        habits.any { logs[it.id] != true }
    }

    suspend fun getPendingTitlesForDate(date: LocalDate): List<String> = withContext(Dispatchers.IO) {
        val habits = habitDao.getAllOnce().map { it.toDomain() }.filter { it.isScheduledOn(date) }
        val logs = logDao.getForDay(date.toEpochDay()).associate { it.habitId to it.isCompleted }
        habits.filter { logs[it.id] != true }.map { it.title }
    }

    suspend fun heatmapSnapshot(start: LocalDate, end: LocalDate): List<DayIntensity> =
        withContext(Dispatchers.IO) {
            val domainHabits = habitDao.getAllOnce().map { it.toDomain() }
            val logs = logDao.getBetweenOnce(start.toEpochDay(), end.toEpochDay())
            val logMap = logs.groupBy { it.dateEpochDay }
                .mapValues { (_, v) -> v.associate { it.habitId to it.isCompleted } }
            buildList {
                var d = start
                while (!d.isAfter(end)) {
                    val epoch = d.toEpochDay()
                    val scheduled = domainHabits.filter { it.isScheduledOn(d) && epoch >= it.createdAtEpochDay }
                    val had = scheduled.isNotEmpty()
                    val intensity = if (!had) {
                        0f
                    } else {
                        val map = logMap[epoch].orEmpty()
                        val done = scheduled.count { map[it.id] == true }
                        done.toFloat() / scheduled.size
                    }
                    add(DayIntensity(d, intensity, had))
                    d = d.plusDays(1)
                }
            }
        }

    fun observeHeatmap(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<DayIntensity>> {
        val s = start.toEpochDay()
        val e = end.toEpochDay()
        return combine(habitDao.observeAll(), logDao.observeBetween(s, e)) { habits, logs ->
            val domainHabits = habits.map { it.toDomain() }
            val logMap = logs.groupBy { it.dateEpochDay }
                .mapValues { (_, v) -> v.associate { it.habitId to it.isCompleted } }
            val result = mutableListOf<DayIntensity>()
            var d = start
            while (!d.isAfter(end)) {
                val epoch = d.toEpochDay()
                val scheduled = domainHabits.filter { it.isScheduledOn(d) && epoch >= it.createdAtEpochDay }
                val had = scheduled.isNotEmpty()
                val intensity = if (!had) {
                    0f
                } else {
                    val map = logMap[epoch].orEmpty()
                    val done = scheduled.count { map[it.id] == true }
                    done.toFloat() / scheduled.size
                }
                result.add(DayIntensity(d, intensity, had))
                d = d.plusDays(1)
            }
            result
        }.flowOn(Dispatchers.Default)
    }

    /** One entry per calendar day in range; [hadScheduledHabits] false on off days. */
    fun observeHabitHeatmap(
        habitId: Long,
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<DayIntensity>> {
        val s = start.toEpochDay()
        val e = end.toEpochDay()
        return combine(habitDao.observeAll(), logDao.observeForHabitRange(habitId, s, e)) { habits, habitLogs ->
            val habit = habits.firstOrNull { it.id == habitId }?.toDomain() ?: return@combine emptyList()
            val logMap = habitLogs.associate { it.dateEpochDay to it.isCompleted }
            buildList {
                var d = start
                while (!d.isAfter(end)) {
                    val epoch = d.toEpochDay()
                    val scheduled = habit.isScheduledOn(d) && epoch >= habit.createdAtEpochDay
                    val intensity = if (!scheduled) {
                        0f
                    } else {
                        if (logMap[epoch] == true) 1f else 0f
                    }
                    add(DayIntensity(d, intensity, scheduled))
                    d = d.plusDays(1)
                }
            }
        }.flowOn(Dispatchers.Default)
    }

    suspend fun requestWidgetUpdate() = withContext(Dispatchers.Main) {
        try { HabitWidget().updateAll(appContext) } catch (_: Exception) {}
    }
}
