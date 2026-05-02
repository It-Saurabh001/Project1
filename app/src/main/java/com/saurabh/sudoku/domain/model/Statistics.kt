package com.saurabh.sudoku.domain.model

data class Statistics(
    val gamesCompleted: Int = 0,
    val totalPlayTime: Long = 0L,
    val bestTimeEasy: Long =  0L,
    val bestTimeMedium: Long =  0L,
    val bestTimeHard: Long = 0L,
    val bestTimeExpert: Long =  0L,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalHintsUsed: Int = 0,
    val lastPlayedDate: String? = null
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

    fun hasBestTime(difficulty: Difficulty): Boolean {
        return getBestTime(difficulty) > 0L
    }
}