package com.saurabh.sudoku.data.local.database.dao

import androidx.room.*
import com.saurabh.sudoku.data.local.database.entities.StatisticsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StatisticsDao {

    @Query("SELECT * FROM statistics WHERE id = 1")
    suspend fun getStatistics(): StatisticsEntity?

    @Query("SELECT * FROM statistics WHERE id = 1")
    fun getStatisticsFlow(): Flow<StatisticsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatistics(statistics: StatisticsEntity)

    @Update
    suspend fun updateStatistics(statistics: StatisticsEntity)

    @Query("""
        UPDATE statistics 
        SET gamesCompleted = gamesCompleted + 1,
            totalPlayTime = totalPlayTime + :gameTime,
            bestTimeEasy = CASE 
                WHEN :difficulty = 'EASY' AND :gameTime < bestTimeEasy 
                THEN :gameTime ELSE bestTimeEasy END,
            bestTimeMedium = CASE 
                WHEN :difficulty = 'MEDIUM' AND :gameTime < bestTimeMedium 
                THEN :gameTime ELSE bestTimeMedium END,
            bestTimeHard = CASE 
                WHEN :difficulty = 'HARD' AND :gameTime < bestTimeHard 
                THEN :gameTime ELSE bestTimeHard END,
            bestTimeExpert = CASE 
                WHEN :difficulty = 'EXPERT' AND :gameTime < bestTimeExpert 
                THEN :gameTime ELSE bestTimeExpert END,
            currentStreak = currentStreak + 1,
            longestStreak = CASE 
                WHEN currentStreak + 1 > longestStreak 
                THEN currentStreak + 1 ELSE longestStreak END,
            totalHintsUsed = totalHintsUsed + :hintsUsed
        WHERE id = 1
    """)
    suspend fun updateGameCompleted(difficulty: String, gameTime: Long, hintsUsed: Int)

    @Query("UPDATE statistics SET currentStreak = 0 WHERE id = 1")
    suspend fun resetCurrentStreak()
}