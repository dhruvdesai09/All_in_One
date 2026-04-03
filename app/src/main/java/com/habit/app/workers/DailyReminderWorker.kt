package com.habit.app.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.habit.app.data.preferences.UserPreferences
import com.habit.app.data.repository.HabitRepository
import com.habit.app.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val habitRepository: HabitRepository,
    private val userPreferences: UserPreferences,
    private val notificationHelper: NotificationHelper,
    private val notificationScheduler: NotificationScheduler,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = userPreferences.flow.first()
        if (!prefs.remindersEnabled) {
            notificationScheduler.rescheduleAfterWorker()
            return Result.success()
        }
        val today = LocalDate.now()
        if (!habitRepository.hasPendingScheduledHabits(today)) {
            notificationScheduler.rescheduleAfterWorker()
            return Result.success()
        }
        val pendingTitles = habitRepository.getPendingTitlesForDate(today)
        notificationHelper.showPendingHabitsReminder(pendingTitles)
        notificationScheduler.rescheduleAfterWorker()
        return Result.success()
    }
}
