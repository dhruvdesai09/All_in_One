package com.habit.app.domain

import com.habit.app.domain.model.Habit
import java.time.LocalDate

/**
 * Streak counts consecutive scheduled days with a completion log, walking backward from [fromDate].
 * If [fromDate] is scheduled but not completed, it is skipped so the same day does not break streak
 * until it is explicitly missed after prior days were completed.
 */
fun computeStreak(
    habit: Habit,
    fromDate: LocalDate,
    isCompleted: (LocalDate) -> Boolean,
): Int {
    var streak = 0
    var d = fromDate
    val created = LocalDate.ofEpochDay(habit.createdAtEpochDay)
    while (!d.isBefore(created)) {
        if (!habit.isScheduledOn(d)) {
            d = d.minusDays(1)
            continue
        }
        val done = isCompleted(d)
        if (!done) {
            if (d == fromDate) {
                d = d.minusDays(1)
                continue
            }
            break
        }
        streak++
        d = d.minusDays(1)
    }
    return streak
}
