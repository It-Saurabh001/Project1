package com.saurabh.sudoku.data.repository

import com.saurabh.sudoku.data.local.database.dao.StatisticsDao
import com.saurabh.sudoku.data.local.database.entities.StatisticsEntity
import com.saurabh.sudoku.domain.model.Difficulty
import com.saurabh.sudoku.domain.model.Statistics
import com.saurabh.sudoku.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepositoryImpl @Inject constructor(
    private val statisticsDao: StatisticsDao
) : StatisticsRepository{
    override suspend fun getStatistics(): Statistics {
        return statisticsDao.getStatistics()?.toDomainModel() ?: Statistics()
    }

    override fun getStatisticsFlow(): Flow<Statistics> {
        return statisticsDao.getStatisticsFlow().map {
            it?.toDomainModel() ?: Statistics()
        }
    }

    override suspend fun updateStatistics(statistics: Statistics) {
        statisticsDao.insertStatistics(statistics.toEntity())
    }

    override suspend fun recordGameCompleted(difficulty: Difficulty, gameTime: Long, hintsUsed: Int) {
        // Ensure statistics entry exists
        if (statisticsDao.getStatistics() == null) {
            statisticsDao.insertStatistics(StatisticsEntity())
        }

        statisticsDao.updateGameCompleted(difficulty.name, gameTime, hintsUsed)
    }

    override suspend fun resetCurrentStreak() {
        statisticsDao.resetCurrentStreak()
    }
    private fun StatisticsEntity.toDomainModel(): Statistics {
        return Statistics(
            gamesCompleted = gamesCompleted,
            totalPlayTime = totalPlayTime,
            bestTimeEasy = bestTimeEasy,
            bestTimeMedium = bestTimeMedium,
            bestTimeHard = bestTimeHard,
            bestTimeExpert = bestTimeExpert,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalHintsUsed = totalHintsUsed
        )
    }

    private fun Statistics.toEntity(): StatisticsEntity {
        return StatisticsEntity(
            gamesCompleted = gamesCompleted,
            totalPlayTime = totalPlayTime,
            bestTimeEasy = bestTimeEasy,
            bestTimeMedium = bestTimeMedium,
            bestTimeHard = bestTimeHard,
            bestTimeExpert = bestTimeExpert,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalHintsUsed = totalHintsUsed
        )
    }
}