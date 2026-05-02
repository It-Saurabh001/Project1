package com.saurabh.sudoku.presentation.utils

object Constants {
    const val GRID_SIZE = 9
    const val BOX_SIZE = 3
    const val EMPTY_CELL = 0
    const val MAX_HINTS = 3
    const val DATABASE_NAME = "sudoku_database"
    const val PREFERENCES_NAME = "sudoku_preferences"

    // Game States
    const val GAME_STATE_PLAYING = "playing"
    const val GAME_STATE_PAUSED = "paused"
    const val GAME_STATE_COMPLETED = "completed"
    const val GAME_STATE_LOST = "lost"  // <-- ADD THIS

    // Difficulty Clues
    const val EASY_CLUES = 40
    const val MEDIUM_CLUES = 35
    const val HARD_CLUES = 28
    const val EXPERT_CLUES = 22

    // Mistake Limits per Difficulty  // <-- ADD THIS SECTION
    const val MISTAKES_EASY = 5
    const val MISTAKES_MEDIUM = 3
    const val MISTAKES_HARD = 2
    const val MISTAKES_EXPERT = 1

    fun getMaxMistakes(difficulty: String): Int {
        return when (difficulty) {
            "EASY" -> MISTAKES_EASY
            "MEDIUM" -> MISTAKES_MEDIUM
            "HARD" -> MISTAKES_HARD
            "EXPERT" -> MISTAKES_EXPERT
            else -> MISTAKES_MEDIUM
        }
    }
}