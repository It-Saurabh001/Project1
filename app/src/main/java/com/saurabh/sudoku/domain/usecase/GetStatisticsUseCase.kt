package com.saurabh.sudoku.domain.usecase

import com.saurabh.sudoku.domain.model.Statistics
import com.saurabh.sudoku.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStatisticsUseCase @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) {

    suspend fun getStatistics(): Statistics {
        return statisticsRepository.getStatistics()
    }

    fun getStatisticsFlow(): Flow<Statistics> {
        return statisticsRepository.getStatisticsFlow()
    }
}