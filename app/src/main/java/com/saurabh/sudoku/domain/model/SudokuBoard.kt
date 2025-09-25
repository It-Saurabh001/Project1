package com.saurabh.sudoku.domain.model

data class SudokuBoard(
    val board: Array<IntArray>,
    val solution: Array<IntArray>,
    val initialBoard: Array<IntArray>
) {
    fun isInitialCell(row: Int, col: Int): Boolean {
        return initialBoard[row][col] != 0
    }

    fun getCurrentValue(row: Int, col: Int): Int {
        return board[row][col]
    }

    fun getSolutionValue(row: Int, col: Int): Int {
        return solution[row][col]
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SudokuBoard

        if (!board.contentDeepEquals(other.board)) return false
        if (!solution.contentDeepEquals(other.solution)) return false
        if (!initialBoard.contentDeepEquals(other.initialBoard)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = board.contentDeepHashCode()
        result = 31 * result + solution.contentDeepHashCode()
        result = 31 * result + initialBoard.contentDeepHashCode()
        return result
    }
}