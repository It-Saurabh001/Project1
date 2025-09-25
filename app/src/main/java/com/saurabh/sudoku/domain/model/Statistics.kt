package com.saurabh.sudoku.domain.model

data class Statistics(
    val gamesCompleted: Int = 0,
    val totalPlayTime: Long = 0L,
    val bestTimeEasy: Long = Long.MAX_VALUE,
    val bestTimeMedium: Long = Long.MAX_VALUE,
    val bestTimeHard: Long = Long.MAX_VALUE,
    val bestTimeExpert: Long = Long.MAX_VALUE,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalHintsUsed: Int = 0
) {
    fun getBestTime(difficulty: Difficulty): Long {
        return when (difficulty) {
            Difficulty.EASY -> bestTimeEasy
            Difficulty.MEDIUM -> bestTimeMedium
            Difficulty.HARD -> bestTimeHard
            Difficulty.EXPERT -> bestTimeExpert
        }
    }

    fun getAverageTime(): Long {
        return if (gamesCompleted > 0) totalPlayTime / gamesCompleted else 0L
    }
}