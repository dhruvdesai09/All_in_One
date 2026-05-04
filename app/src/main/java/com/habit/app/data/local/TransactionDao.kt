package com.habit.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query(
        """
        SELECT * FROM transactions
        WHERE epochDay >= :startEpochDay AND epochDay <= :endEpochDay
        ORDER BY epochDay DESC, createdAt DESC
        """
    )
    fun observeByMonth(startEpochDay: Long, endEpochDay: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY epochDay DESC, createdAt DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT COALESCE(SUM(amountCents), 0) FROM transactions
        WHERE budgetId = :budgetId
          AND type = 'expense'
          AND epochDay >= :startEpochDay
          AND epochDay <= :endEpochDay
        """
    )
    suspend fun sumExpensesByBudget(budgetId: Long, startEpochDay: Long, endEpochDay: Long): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tx: TransactionEntity): Long

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TransactionEntity?
}
