package com.saurabh.sudoku.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.sudoku.domain.model.Statistics
import com.saurabh.sudoku.domain.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "StatisticsViewModel"

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    /**
     * Live statistics backed by Room's reactive query.
     * When GameViewModel calls statisticsRepository.recordGameCompleted(),
     * Room triggers a new emission here automatically.
     */
    val statistics: StateFlow<Statistics> = statisticsRepository.getStatisticsFlow()
        .onEach { stats ->
            Log.d(TAG, "📊 statistics flow emitted:")
            Log.d(TAG, "   gamesCompleted = ${stats.gamesCompleted}")
            Log.d(TAG, "   totalPlayTime  = ${stats.totalPlayTime} ms")
            Log.d(TAG, "   bestTimeEasy   = ${stats.bestTimeEasy} ms")
            Log.d(TAG, "   bestTimeMedium = ${stats.bestTimeMedium} ms")
            Log.d(TAG, "   bestTimeHard   = ${stats.bestTimeHard} ms")
            Log.d(TAG, "   bestTimeExpert = ${stats.bestTimeExpert} ms")
            Log.d(TAG, "   currentStreak  = ${stats.currentStreak}")
            Log.d(TAG, "   longestStreak  = ${stats.longestStreak}")
            Log.d(TAG, "   totalHintsUsed = ${stats.totalHintsUsed}")
            Log.d(TAG, "   lastPlayedDate = ${stats.lastPlayedDate}")
            Log.d(TAG, "   totalGamesPlayed = ${stats.totalGamesPlayed}")
            Log.d(TAG, "   gamesLost = ${stats.gamesLost}")
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Statistics()
        )

    /** Force a one-shot read — use this when navigating to the statistics screen. */
    fun refreshStatistics() {
        viewModelScope.launch {
            val stats = statisticsRepository.getStatistics()
            Log.d(TAG, "refreshStatistics() → gamesCompleted=${stats.gamesCompleted}, " +
                "totalGamesPlayed=${stats.totalGamesPlayed}, gamesLost=${stats.gamesLost}, " +
                "streak=${stats.currentStreak}/${stats.longestStreak}")
        }
    }
}