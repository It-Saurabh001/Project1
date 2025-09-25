package com.saurabh.sudoku.domain.repository

import com.saurabh.sudoku.domain.model.Difficulty
import com.saurabh.sudoku.domain.model.Statistics
import kotlinx.coroutines.flow.Flow

interface StatisticsRepository {
    suspend fun getStatistics(): Statistics
    fun getStatisticsFlow(): Flow<Statistics>
    suspend fun updateStatistics(statistics: Statistics)
    suspend fun recordGameCompleted(difficulty: Difficulty, gameTime: Long, hintsUsed: Int)
    suspend fun resetCurrentStreak()
}