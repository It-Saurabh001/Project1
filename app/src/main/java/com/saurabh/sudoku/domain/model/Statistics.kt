package com.saurabh.sudoku.domain.model

data class Statistics(
    // ── Win-only metrics ──────────────────────────────────────────────────────
    val gamesCompleted: Int = 0,
    val bestTimeEasy: Long = 0L,
    val bestTimeMedium: Long = 0L,
    val bestTimeHard: Long = 0L,
    val bestTimeExpert: Long = 0L,

    // ── All-game metrics ──────────────────────────────────────────────────────
    val totalGamesPlayed: Int = 0,
    val gamesLost: Int = 0,
    val totalPlayTime: Long = 0L,
    val totalHintsUsed: Int = 0,
    val lastPlayedDate: String? = null,

    // ── Streak (win-based) ────────────────────────────────────────────────────
    val currentStreak: Int = 0,
    val longestStreak: Int = 0
) {
    /** Best time for a given difficulty (0 = never won at that difficulty). */
    fun getBestTime(difficulty: Difficulty): Long = when (difficulty) {
        Difficulty.EASY   -> bestTimeEasy
        Difficulty.MEDIUM -> bestTimeMedium
        Difficulty.HARD   -> bestTimeHard
        Difficulty.EXPERT -> bestTimeExpert
    }

    /** Average time across ALL games (wins + losses). */
    fun getAverageTime(): Long =
        if (totalGamesPlayed > 0) totalPlayTime / totalGamesPlayed else 0L

    /** Win rate as 0..100 percentage. Returns 0 if no games played. */
    fun getWinRatePercent(): Int =
        if (totalGamesPlayed > 0) (gamesCompleted * 100) / totalGamesPlayed else 0

    fun hasBestTime(difficulty: Difficulty): Boolean = getBestTime(difficulty) > 0L
}