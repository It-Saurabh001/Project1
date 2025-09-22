package com.saurabh.sudoku.di

import android.content.Context
import com.saurabh.sudoku.data.local.database.SudokuDatabase
import com.saurabh.sudoku.data.local.database.dao.GameDao
import com.saurabh.sudoku.data.local.database.dao.PuzzleDao
import com.saurabh.sudoku.data.local.database.dao.StatisticsDao
import com.saurabh.sudoku.data.local.preferences.UserPreferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent :: class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideSudokuDatabase(@ApplicationContext context: Context): SudokuDatabase {
        return SudokuDatabase.getDatabase(context)
    }

    @Provides
    fun provideGameDao(database: SudokuDatabase): GameDao {
        return database.gameDao()
    }

    @Provides
    fun provideStatisticsDao(database: SudokuDatabase): StatisticsDao {
        return database.statisticsDao()
    }

    @Provides
    fun providePuzzleDao(database: SudokuDatabase): PuzzleDao {
        return database.puzzleDao()
    }

    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(@ApplicationContext context: Context): UserPreferencesDataStore {
        return UserPreferencesDataStore(context)
    }

    @Provides
    @Singleton
    fun provideGson(): com.google.gson.Gson {
        return com.google.gson.Gson()
    }
}