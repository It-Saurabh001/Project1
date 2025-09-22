package com.saurabh.sudoku.domain.model

data class Game(
    val id: String,
    val board: SudokuBoard,
    val difficulty: Difficulty,
    val startTime: Long,
    val currentTime: Long,
    val state: GameState,
    val hintsUsed: Int = 0,
    val createdAt: Long,
    val completedAt: Long? = null
) {
    val elapsedTime: Long
        get() = if (completedAt != null) completedAt - startTime else currentTime - startTime

    val isCompleted: Boolean
        get() = state == GameState.COMPLETED
}
