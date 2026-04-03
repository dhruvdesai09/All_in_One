package com.habit.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitLogDao {
    @Query("SELECT * FROM habit_logs WHERE dateEpochDay = :epochDay")
    suspend fun getForDay(epochDay: Long): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND dateEpochDay BETWEEN :start AND :end")
    fun observeForHabitRange(habitId: Long, start: Long, end: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE dateEpochDay BETWEEN :start AND :end")
    fun observeBetween(start: Long, end: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs")
    fun observeAllLogs(): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE dateEpochDay BETWEEN :start AND :end")
    suspend fun getBetweenOnce(start: Long, end: Long): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND dateEpochDay = :epochDay LIMIT 1")
    suspend fun get(habitId: Long, epochDay: Long): HabitLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: HabitLogEntity)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND dateEpochDay = :epochDay")
    suspend fun delete(habitId: Long, epochDay: Long)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteAllForHabit(habitId: Long)
}
