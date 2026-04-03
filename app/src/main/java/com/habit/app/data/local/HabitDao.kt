package com.habit.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY title ASC")
    fun observeAll(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY title ASC")
    suspend fun getAllOnce(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getById(id: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HabitEntity): Long

    @Update
    suspend fun update(entity: HabitEntity)

    @Delete
    suspend fun delete(entity: HabitEntity)

    @Query("DELETE FROM habits WHERE isOneTime = 1 AND targetDateEpochDay IS NOT NULL AND targetDateEpochDay < :currentEpochDay")
    suspend fun deleteExpiredOneTime(currentEpochDay: Long)
}
