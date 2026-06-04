package com.saurabh.sudoku.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.sudoku.domain.model.Achievement
import com.saurabh.sudoku.domain.model.Statistics
import com.saurabh.sudoku.domain.repository.GameRepository
import com.saurabh.sudoku.domain.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "StatisticsViewModel"

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
    private val gameRepository: GameRepository
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

    /**
     * Reactive achievements list combining statistics and completed games statistics.
     */
    val achievements: StateFlow<List<Achievement>> = combine(
        statistics,
        gameRepository.getHintlessWinsCountFlow(),
        gameRepository.getPerfectWinsCountFlow()
    ) { stats, hintlessCount, perfectCount ->
        listOf(
            Achievement(
                id = "first_victory",
                title = "First Victory",
                description = "Win your first Sudoku game.",
                emoji = "🏆",
                isUnlocked = stats.gamesCompleted >= 1,
                currentProgress = minOf(stats.gamesCompleted, 1),
                maxProgress = 1,
                displayProgress = false
            ),
            Achievement(
                id = "easy_rider",
                title = "Easy Rider",
                description = "Win an Easy difficulty game.",
                emoji = "🟢",
                isUnlocked = stats.bestTimeEasy > 0,
                currentProgress = if (stats.bestTimeEasy > 0) 1 else 0,
                maxProgress = 1,
                displayProgress = false
            ),
            Achievement(
                id = "medium_challenger",
                title = "Medium Challenger",
                description = "Win a Medium difficulty game.",
                emoji = "🟡",
                isUnlocked = stats.bestTimeMedium > 0,
                currentProgress = if (stats.bestTimeMedium > 0) 1 else 0,
                maxProgress = 1,
                displayProgress = false
            ),
            Achievement(
                id = "hard_hitter",
                title = "Hard Hitter",
                description = "Win a Hard difficulty game.",
                emoji = "🔴",
                isUnlocked = stats.bestTimeHard > 0,
                currentProgress = if (stats.bestTimeHard > 0) 1 else 0,
                maxProgress = 1,
                displayProgress = false
            ),
            Achievement(
                id = "expert_solver",
                title = "Expert Solver",
                description = "Win an Expert difficulty game.",
                emoji = "🔥",
                isUnlocked = stats.bestTimeExpert > 0,
                currentProgress = if (stats.bestTimeExpert > 0) 1 else 0,
                maxProgress = 1,
                displayProgress = false
            ),
            Achievement(
                id = "streak_builder",
                title = "Streak Builder",
                description = "Achieve a win streak of 3 games.",
                emoji = "⚡",
                isUnlocked = stats.longestStreak >= 3,
                currentProgress = stats.longestStreak,
                maxProgress = 3,
                displayProgress = true
            ),
            Achievement(
                id = "streak_master",
                title = "Streak Master",
                description = "Achieve a win streak of 5 games.",
                emoji = "👑",
                isUnlocked = stats.longestStreak >= 5,
                currentProgress = stats.longestStreak,
                maxProgress = 5,
                displayProgress = true
            ),
            Achievement(
                id = "mind_reader",
                title = "Mind Reader",
                description = "Win a game without using any hints.",
                emoji = "🧠",
                isUnlocked = hintlessCount >= 1,
                currentProgress = minOf(hintlessCount, 1),
                maxProgress = 1,
                displayProgress = false
            ),
            Achievement(
                id = "flawless_victory",
                title = "Flawless Victory",
                description = "Win a game with 0 mistakes.",
                emoji = "⭐",
                isUnlocked = perfectCount >= 1,
                currentProgress = minOf(perfectCount, 1),
                maxProgress = 1,
                displayProgress = false
            ),
            Achievement(
                id = "speed_demon",
                title = "Speed Demon",
                description = "Win a game in under 5 minutes.",
                emoji = "⏱️",
                isUnlocked = (stats.bestTimeEasy in 1..300000) ||
                             (stats.bestTimeMedium in 1..300000) ||
                             (stats.bestTimeHard in 1..300000) ||
                             (stats.bestTimeExpert in 1..300000),
                currentProgress = if ((stats.bestTimeEasy in 1..300000) ||
                                     (stats.bestTimeMedium in 1..300000) ||
                                     (stats.bestTimeHard in 1..300000) ||
                                     (stats.bestTimeExpert in 1..300000)) 1 else 0,
                maxProgress = 1,
                displayProgress = false
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
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