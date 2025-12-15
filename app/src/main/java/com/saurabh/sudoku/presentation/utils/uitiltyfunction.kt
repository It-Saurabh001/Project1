package com.saurabh.sudoku.presentation.utils

import androidx.compose.animation.core.copy
import com.saurabh.sudoku.domain.model.SudokuBoard
import kotlin.collections.map
import kotlin.collections.toTypedArray

// Add this outside the GameViewModel class
fun SudokuBoard.deepCopy(): SudokuBoard {
    val newBoardArray = this.board.map { it.clone() }.toTypedArray()
    return this.copy(board = newBoardArray)
}
