package com.habit.app.di

import android.content.Context
import androidx.room.Room
import com.habit.app.data.local.AppDatabase
import com.habit.app.data.local.MIGRATION_1_2
import com.habit.app.data.local.MIGRATION_2_3
import com.habit.app.data.local.MIGRATION_3_4
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "habit.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun providePasswordDao(db: AppDatabase) = db.passwordDao()

    @Provides
    fun provideBudgetDao(db: AppDatabase) = db.budgetDao()

    @Provides
    fun provideTransactionDao(db: AppDatabase) = db.transactionDao()
}
