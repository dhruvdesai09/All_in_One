package com.habit.app.workers

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.habit.app.data.preferences.UserPreferences
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val userPreferences: UserPreferences,
) {
    suspend fun scheduleFromCurrentPreferences() {
        val p = userPreferences.flow.first()
        if (!p.remindersEnabled) {
            workManager.cancelUniqueWork(UNIQUE_WORK)
            return
        }
        enqueueOneShot(p.reminderHour, p.reminderMinute)
    }

    private fun enqueueOneShot(hour: Int, minute: Int) {
        val delayMs = nextDelayMillis(hour, minute, ZoneId.systemDefault())
        val request = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniqueWork(
            UNIQUE_WORK,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    suspend fun rescheduleAfterWorker() {
        val p = userPreferences.flow.first()
        if (!p.remindersEnabled) return
        enqueueOneShot(p.reminderHour, p.reminderMinute)
    }

    companion object {
        const val UNIQUE_WORK = "habit_daily_reminder"
    }
}

private fun nextDelayMillis(hour: Int, minute: Int, zone: ZoneId): Long {
    val now = ZonedDateTime.now(zone)
    var next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
    if (!next.isAfter(now)) {
        next = next.plusDays(1)
    }
    return Duration.between(now.toInstant(), next.toInstant()).toMillis().coerceAtLeast(60_000L)
}
