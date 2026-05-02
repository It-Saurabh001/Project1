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

    /**
     * Ensures the statistics singleton row (id=1) exists in the DB.
     * Uses INSERT OR REPLACE, so it's safe to call multiple times.
     * Preserves existing data because Room's @Insert(REPLACE) will restore
     * defaults only if the row truly doesn't exist yet.
     *
     * We call this ONCE on every [recordGameCompleted] before the UPDATE.
     */
    private suspend fun ensureStatisticsRowExists() {
        val existing = statisticsDao.getStatistics()
        if (existing == null) {
            Log.w(TAG, "⚠️ Statistics row missing — seeding with defaults")
            statisticsDao.insertStatistics(StatisticsEntity())
            Log.d(TAG, "✅ Statistics row seeded (id=1)")
        }
    }

    override suspend fun getStatistics(): Statistics {
        val entity = statisticsDao.getStatistics()
        Log.d(TAG, "getStatistics() → ${if (entity == null) "NULL (returning defaults)" else "gamesCompleted=${entity.gamesCompleted}"}")
        return entity?.toDomainModel() ?: Statistics()
    }

    override fun getStatisticsFlow(): Flow<Statistics> {
        return statisticsDao.getStatisticsFlow().map { entity ->
            if (entity == null) {
                Log.w(TAG, "getStatisticsFlow emitted null — returning default Statistics()")
            } else {
                Log.d(TAG, "getStatisticsFlow emitted: gamesCompleted=${entity.gamesCompleted}, " +
                    "bestEasy=${entity.bestTimeEasy}, bestMed=${entity.bestTimeMedium}, " +
                    "streak=${entity.currentStreak}/${entity.longestStreak}")
            }
            entity?.toDomainModel() ?: Statistics()
        }
    }

    override suspend fun updateStatistics(statistics: Statistics) {
        Log.d(TAG, "updateStatistics() called — gamesCompleted=${statistics.gamesCompleted}")
        statisticsDao.updateStatistics(statistics.toEntity())
    }

    override suspend fun recordGameCompleted(
        difficulty: Difficulty,
        gameTime: Long,
        hintsUsed: Int
    ) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        Log.d(TAG, "📊 recordGameCompleted START — difficulty=$difficulty, gameTime=${gameTime}ms, hints=$hintsUsed, date=$currentDate")

        // Step 1: Guarantee the row exists
        ensureStatisticsRowExists()

        // Step 2: Atomically update all statistics fields
        val rowsUpdated = statisticsDao.updateGameCompleted(
            difficulty     = difficulty.name,
            gameTime       = gameTime,
            hintsUsed      = hintsUsed,
            lastPlayedDate = currentDate
        )

        // Step 3: If the UPDATE still hit 0 rows (edge case), retry once with a forced seed
        if (rowsUpdated == 0) {
            Log.e(TAG, "❌ updateGameCompleted affected 0 rows! Forcing re-seed and retry")
            statisticsDao.insertStatistics(StatisticsEntity())
            val retryRows = statisticsDao.updateGameCompleted(
                difficulty     = difficulty.name,
                gameTime       = gameTime,
                hintsUsed      = hintsUsed,
                lastPlayedDate = currentDate
            )
            Log.d(TAG, "Retry result: $retryRows rows updated")
        } else {
            Log.d(TAG, "✅ updateGameCompleted succeeded — $rowsUpdated row(s) updated")
        }

        // Step 4: Read back and log for verification
        val updated = statisticsDao.getStatistics()
        if (updated != null) {
            Log.d(TAG, "📈 Final statistics after completion:")
            Log.d(TAG, "   gamesCompleted  = ${updated.gamesCompleted}")
            Log.d(TAG, "   totalPlayTime   = ${updated.totalPlayTime} ms")
            Log.d(TAG, "   bestTimeEasy    = ${updated.bestTimeEasy} ms")
            Log.d(TAG, "   bestTimeMedium  = ${updated.bestTimeMedium} ms")
            Log.d(TAG, "   bestTimeHard    = ${updated.bestTimeHard} ms")
            Log.d(TAG, "   bestTimeExpert  = ${updated.bestTimeExpert} ms")
            Log.d(TAG, "   currentStreak   = ${updated.currentStreak}")
            Log.d(TAG, "   longestStreak   = ${updated.longestStreak}")
            Log.d(TAG, "   totalHintsUsed  = ${updated.totalHintsUsed}")
            Log.d(TAG, "   lastPlayedDate  = ${updated.lastPlayedDate}")
        } else {
            Log.e(TAG, "❌ getStatistics() still returned null after update — something is seriously wrong")
        }
    }

    override suspend fun resetCurrentStreak() {
        Log.d(TAG, "resetCurrentStreak() called")
        statisticsDao.resetCurrentStreak()
    }

    // -------------------------------------------------------------------------
    // Mappers
    // -------------------------------------------------------------------------

    private fun StatisticsEntity.toDomainModel(): Statistics = Statistics(
        gamesCompleted = gamesCompleted,
        totalPlayTime  = totalPlayTime,
        bestTimeEasy   = bestTimeEasy,
        bestTimeMedium = bestTimeMedium,
        bestTimeHard   = bestTimeHard,
        bestTimeExpert = bestTimeExpert,
        currentStreak  = currentStreak,
        longestStreak  = longestStreak,
        totalHintsUsed = totalHintsUsed,
        lastPlayedDate = lastPlayedDate
    )

    private fun Statistics.toEntity(): StatisticsEntity = StatisticsEntity(
        id             = 1,
        gamesCompleted = gamesCompleted,
        totalPlayTime  = totalPlayTime,
        bestTimeEasy   = bestTimeEasy,
        bestTimeMedium = bestTimeMedium,
        bestTimeHard   = bestTimeHard,
        bestTimeExpert = bestTimeExpert,
        currentStreak  = currentStreak,
        longestStreak  = longestStreak,
        totalHintsUsed = totalHintsUsed,
        lastPlayedDate = lastPlayedDate
    )
}