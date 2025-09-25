package com.saurabh.sudoku.utils

object Constants {
    const val BOARD_SIZE = 9
    const val BOX_SIZE = 3
    const val EMPTY_CELL = 0
    const val MAX_HINTS = 3
    const val DATABASE_NAME = "sudoku_database"
    const val PREFERENCES_NAME = "sudoku_preferences"

    // Game States
    const val GAME_STATE_PLAYING = "playing"
    const val GAME_STATE_PAUSED = "paused"
    const val GAME_STATE_COMPLETED = "completed"

    // Difficulty Levels
    const val EASY_CLUES = 40
    const val MEDIUM_CLUES = 35
    const val HARD_CLUES = 28
    const val EXPERT_CLUES = 22
}