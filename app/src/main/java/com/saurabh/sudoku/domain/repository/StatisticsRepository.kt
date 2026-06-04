package com.saurabh.sudoku.domain.repository

import com.saurabh.sudoku.domain.model.Difficulty
import com.saurabh.sudoku.domain.model.Statistics
import kotlinx.coroutines.flow.Flow

interface StatisticsRepository {
    suspend fun getStatistics(): Statistics
    fun getStatisticsFlow(): Flow<Statistics>
    suspend fun updateStatistics(statistics: Statistics)

    /** Call when a puzzle is solved (board fully correct). */
    suspend fun recordGameCompleted(difficulty: Difficulty, gameTime: Long, hintsUsed: Int)

    /** Call when the player runs out of mistakes (LOST state). */
    suspend fun recordGameLost(gameTime: Long, hintsUsed: Int)

    suspend fun resetCurrentStreak()
}