package com.saurabh.sudoku.presentation.utils

import com.saurabh.sudoku.domain.model.SudokuBoard

// Add this outside the GameViewModel class
fun SudokuBoard.deepCopy(): SudokuBoard {
    val newBoardArray = this.board.map { it.clone() }.toTypedArray()
    return this.copy(board = newBoardArray)
}


// Allow callsites that expect game.board.canPlaceNumber(row,col,num) or with Pair
fun Array<IntArray>.canPlaceNumber(row: Int, col: Int, num: Int): Boolean =
    GameUtils.canPlaceNumber(this, row, col, num)

fun Array<IntArray>.canPlaceNumber(cell: Pair<Int, Int>, num: Int): Boolean =
    GameUtils.canPlaceNumber(this, cell.first, cell.second, num)