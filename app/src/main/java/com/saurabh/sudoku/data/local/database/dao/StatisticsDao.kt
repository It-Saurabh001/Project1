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

    /**
     * Called on WIN.
     * Updates: gamesCompleted++, totalGamesPlayed++, totalPlayTime, best times,
     *          streak (increment + update longest), totalHintsUsed, lastPlayedDate.
     * Returns: number of rows affected (0 = row didn't exist).
     */
    @Query("""
        UPDATE statistics
        SET gamesCompleted   = gamesCompleted + 1,
            totalGamesPlayed = totalGamesPlayed + 1,
            totalPlayTime    = totalPlayTime + :gameTime,
            bestTimeEasy     = CASE
                WHEN :difficulty = 'EASY'   AND (COALESCE(bestTimeEasy, 0) = 0 OR :gameTime < COALESCE(bestTimeEasy, 0))
                THEN :gameTime ELSE bestTimeEasy   END,
            bestTimeMedium   = CASE
                WHEN :difficulty = 'MEDIUM' AND (COALESCE(bestTimeMedium, 0) = 0 OR :gameTime < COALESCE(bestTimeMedium, 0))
                THEN :gameTime ELSE bestTimeMedium END,
            bestTimeHard     = CASE
                WHEN :difficulty = 'HARD'   AND (COALESCE(bestTimeHard, 0) = 0 OR :gameTime < COALESCE(bestTimeHard, 0))
                THEN :gameTime ELSE bestTimeHard   END,
            bestTimeExpert   = CASE
                WHEN :difficulty = 'EXPERT' AND (COALESCE(bestTimeExpert, 0) = 0 OR :gameTime < COALESCE(bestTimeExpert, 0))
                THEN :gameTime ELSE bestTimeExpert END,
            currentStreak    = currentStreak + 1,
            longestStreak    = CASE
                WHEN currentStreak + 1 > longestStreak
                THEN currentStreak + 1 ELSE longestStreak END,
            totalHintsUsed   = totalHintsUsed + :hintsUsed,
            lastPlayedDate   = :lastPlayedDate
        WHERE id = 1
    """)
    suspend fun updateGameWon(
        difficulty: String,
        gameTime: Long,
        hintsUsed: Int,
        lastPlayedDate: String
    ): Int

    /**
     * Called on LOSS (max mistakes reached).
     * Updates: gamesLost++, totalGamesPlayed++, totalPlayTime, currentStreak = 0,
     *          totalHintsUsed, lastPlayedDate.
     * Best times and gamesCompleted are NOT touched (loss doesn't count as a win).
     * Returns: number of rows affected (0 = row didn't exist).
     */
    @Query("""
        UPDATE statistics
        SET gamesLost        = gamesLost + 1,
            totalGamesPlayed = totalGamesPlayed + 1,
            totalPlayTime    = totalPlayTime + :gameTime,
            currentStreak    = 0,
            totalHintsUsed   = totalHintsUsed + :hintsUsed,
            lastPlayedDate   = :lastPlayedDate
        WHERE id = 1
    """)
    suspend fun updateGameLost(
        gameTime: Long,
        hintsUsed: Int,
        lastPlayedDate: String
    ): Int

    @Query("UPDATE statistics SET currentStreak = 0 WHERE id = 1")
    suspend fun resetCurrentStreak()
}