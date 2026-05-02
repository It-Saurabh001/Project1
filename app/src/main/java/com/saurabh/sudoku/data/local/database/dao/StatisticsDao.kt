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

    /**
     * Inserts or replaces the statistics row.
     * Safe to call with the default StatisticsEntity() to seed the row.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatistics(statistics: StatisticsEntity)

    @Update
    suspend fun updateStatistics(statistics: StatisticsEntity)

    /**
     * Atomically updates all statistics fields for one game completion.
     *
     * NOTE: This only runs if the row with id=1 already exists.
     * Always call insertStatistics() first if getStatistics() returns null.
     */
    @Query("""
        UPDATE statistics 
        SET gamesCompleted = gamesCompleted + 1,
            totalPlayTime = totalPlayTime + :gameTime,
            bestTimeEasy = CASE 
                WHEN :difficulty = 'EASY' AND (bestTimeEasy = 0 OR :gameTime < bestTimeEasy) 
                THEN :gameTime ELSE bestTimeEasy END,
            bestTimeMedium = CASE 
                WHEN :difficulty = 'MEDIUM' AND (bestTimeMedium = 0 OR :gameTime < bestTimeMedium) 
                THEN :gameTime ELSE bestTimeMedium END,
            bestTimeHard = CASE 
                WHEN :difficulty = 'HARD' AND (bestTimeHard = 0 OR :gameTime < bestTimeHard) 
                THEN :gameTime ELSE bestTimeHard END,
            bestTimeExpert = CASE 
                WHEN :difficulty = 'EXPERT' AND (bestTimeExpert = 0 OR :gameTime < bestTimeExpert) 
                THEN :gameTime ELSE bestTimeExpert END,
            currentStreak = currentStreak + 1,
            longestStreak = CASE 
                WHEN currentStreak + 1 > longestStreak 
                THEN currentStreak + 1 ELSE longestStreak END,
            totalHintsUsed = totalHintsUsed + :hintsUsed,
            lastPlayedDate = :lastPlayedDate
        WHERE id = 1
    """)
    suspend fun updateGameCompleted(
        difficulty: String,
        gameTime: Long,
        hintsUsed: Int,
        lastPlayedDate: String
    ): Int   // Returns number of rows affected — 0 means the row didn't exist!

    @Query("UPDATE statistics SET currentStreak = 0 WHERE id = 1")
    suspend fun resetCurrentStreak()
}