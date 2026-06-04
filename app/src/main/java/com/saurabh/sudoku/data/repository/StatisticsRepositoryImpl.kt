package com.saurabh.sudoku.data.repository

import android.util.Log
import com.saurabh.sudoku.data.local.database.dao.StatisticsDao
import com.saurabh.sudoku.data.local.database.entities.StatisticsEntity
import com.saurabh.sudoku.domain.model.Difficulty
import com.saurabh.sudoku.domain.model.Statistics
import com.saurabh.sudoku.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepositoryImpl @Inject constructor(
    private val statisticsDao: StatisticsDao
) : StatisticsRepository {

    private val TAG = "StatisticsRepo"

    // -------------------------------------------------------------------------
    // Reads
    // -------------------------------------------------------------------------

    override suspend fun getStatistics(): Statistics {
        ensureRowExists()
        val entity = statisticsDao.getStatistics()
        Log.d(TAG, "getStatistics() → ${if (entity == null) "NULL (defaults)" else
            "played=${entity.totalGamesPlayed}, won=${entity.gamesCompleted}, lost=${entity.gamesLost}"}")
        return entity?.toDomainModel() ?: Statistics()
    }

    override fun getStatisticsFlow(): Flow<Statistics> {
        return statisticsDao.getStatisticsFlow().map { entity ->
            if (entity == null) {
                Log.w(TAG, "getStatisticsFlow emitted null — returning default Statistics()")
            } else {
                Log.d(TAG, "getStatisticsFlow: played=${entity.totalGamesPlayed}, " +
                    "won=${entity.gamesCompleted}, lost=${entity.gamesLost}, " +
                    "streak=${entity.currentStreak}/${entity.longestStreak}, " +
                    "bestEasy=${entity.bestTimeEasy}ms")
            }
            entity?.toDomainModel() ?: Statistics()
        }
    }

    override suspend fun updateStatistics(statistics: Statistics) {
        Log.d(TAG, "updateStatistics() called manually")
        statisticsDao.updateStatistics(statistics.toEntity())
    }

    // -------------------------------------------------------------------------
    // Record WIN
    // -------------------------------------------------------------------------

    override suspend fun recordGameCompleted(
        difficulty: Difficulty,
        gameTime: Long,
        hintsUsed: Int
    ) {
        val today = todayString()
        Log.d(TAG, "📊 recordGameCompleted (WIN) — diff=$difficulty, time=${gameTime}ms, hints=$hintsUsed, date=$today")

        ensureRowExists()

        val rows = statisticsDao.updateGameWon(
            difficulty     = difficulty.name,
            gameTime       = gameTime,
            hintsUsed      = hintsUsed,
            lastPlayedDate = today
        )
        if (rows == 0) {
            Log.e(TAG, "❌ updateGameWon hit 0 rows — forcing re-seed + retry")
            statisticsDao.insertStatistics(StatisticsEntity())
            statisticsDao.updateGameWon(
                difficulty     = difficulty.name,
                gameTime       = gameTime,
                hintsUsed      = hintsUsed,
                lastPlayedDate = today
            )
        }

        logCurrentState("after WIN")
    }

    // -------------------------------------------------------------------------
    // Record LOSS
    // -------------------------------------------------------------------------

    override suspend fun recordGameLost(gameTime: Long, hintsUsed: Int) {
        val today = todayString()
        Log.d(TAG, "📊 recordGameLost (LOSS) — time=${gameTime}ms, hints=$hintsUsed, date=$today")

        ensureRowExists()

        val rows = statisticsDao.updateGameLost(
            gameTime       = gameTime,
            hintsUsed      = hintsUsed,
            lastPlayedDate = today
        )
        if (rows == 0) {
            Log.e(TAG, "❌ updateGameLost hit 0 rows — forcing re-seed + retry")
            statisticsDao.insertStatistics(StatisticsEntity())
            statisticsDao.updateGameLost(
                gameTime       = gameTime,
                hintsUsed      = hintsUsed,
                lastPlayedDate = today
            )
        }

        logCurrentState("after LOSS")
    }

    // -------------------------------------------------------------------------
    // Streak reset
    // -------------------------------------------------------------------------

    override suspend fun resetCurrentStreak() {
        Log.d(TAG, "resetCurrentStreak() called")
        statisticsDao.resetCurrentStreak()
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private fun todayString(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private suspend fun ensureRowExists() {
        if (statisticsDao.getStatistics() == null) {
            Log.w(TAG, "⚠️ statistics row missing — seeding defaults")
            statisticsDao.insertStatistics(StatisticsEntity())
        }
    }

    private suspend fun logCurrentState(label: String) {
        val s = statisticsDao.getStatistics() ?: run {
            Log.e(TAG, "❌ getStatistics() returned null $label — DB write failed!")
            return
        }
        Log.d(TAG, "✅ Statistics $label:")
        Log.d(TAG, "   totalGamesPlayed = ${s.totalGamesPlayed}")
        Log.d(TAG, "   gamesCompleted   = ${s.gamesCompleted}")
        Log.d(TAG, "   gamesLost        = ${s.gamesLost}")
        Log.d(TAG, "   totalPlayTime    = ${s.totalPlayTime} ms")
        Log.d(TAG, "   bestTimeEasy     = ${s.bestTimeEasy} ms")
        Log.d(TAG, "   bestTimeMedium   = ${s.bestTimeMedium} ms")
        Log.d(TAG, "   bestTimeHard     = ${s.bestTimeHard} ms")
        Log.d(TAG, "   bestTimeExpert   = ${s.bestTimeExpert} ms")
        Log.d(TAG, "   currentStreak    = ${s.currentStreak}")
        Log.d(TAG, "   longestStreak    = ${s.longestStreak}")
        Log.d(TAG, "   totalHintsUsed   = ${s.totalHintsUsed}")
        Log.d(TAG, "   lastPlayedDate   = ${s.lastPlayedDate}")
    }

    // -------------------------------------------------------------------------
    // Mappers
    // -------------------------------------------------------------------------

    private fun StatisticsEntity.toDomainModel(): Statistics = Statistics(
        gamesCompleted   = gamesCompleted,
        totalGamesPlayed = totalGamesPlayed,
        gamesLost        = gamesLost,
        totalPlayTime    = totalPlayTime,
        bestTimeEasy     = bestTimeEasy,
        bestTimeMedium   = bestTimeMedium,
        bestTimeHard     = bestTimeHard,
        bestTimeExpert   = bestTimeExpert,
        currentStreak    = currentStreak,
        longestStreak    = longestStreak,
        totalHintsUsed   = totalHintsUsed,
        lastPlayedDate   = lastPlayedDate
    )

    private fun Statistics.toEntity(): StatisticsEntity = StatisticsEntity(
        id               = 1,
        gamesCompleted   = gamesCompleted,
        totalGamesPlayed = totalGamesPlayed,
        gamesLost        = gamesLost,
        totalPlayTime    = totalPlayTime,
        bestTimeEasy     = bestTimeEasy,
        bestTimeMedium   = bestTimeMedium,
        bestTimeHard     = bestTimeHard,
        bestTimeExpert   = bestTimeExpert,
        currentStreak    = currentStreak,
        longestStreak    = longestStreak,
        totalHintsUsed   = totalHintsUsed,
        lastPlayedDate   = lastPlayedDate
    )
}