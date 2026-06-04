package com.saurabh.sudoku.data.generator

import android.util.Log
import com.saurabh.sudoku.presentation.utils.GameUtils

class SudokuSolver {

    fun solveSudoku(board: Array<IntArray>): Boolean {
        val TAG = "SudokuSolver"
        Log.d(TAG, "solveSudoku() called")
        for (row in 0..8) {
            for (col in 0..8) {
                if (board[row][col] == 0) {
                    for (num in 1..9) {
                        if (GameUtils.isValidMove(board, row, col, num)) {
                            board[row][col] = num

                            if (solveSudoku(board)) {
                                return true
                            }

                            board[row][col] = 0
                        }
                    }
                    return false
                }
            }
        }
        return true
    }

    fun hasUniqueSolution(board: Array<IntArray>): Boolean {
        val TAG = "SudokuSolver"
        Log.d(TAG, "hasUniqueSolution() called")
        val solutions = mutableListOf<Array<IntArray>>()
        findAllSolutions(board, solutions, 2) // Only need to find 2 to check uniqueness
        val unique = solutions.size == 1
        Log.d(TAG, "hasUniqueSolution() result: $unique (${solutions.size} solutions found)")
        return unique
    }

    private fun findAllSolutions(
        board: Array<IntArray>,
        solutions: MutableList<Array<IntArray>>,
        maxSolutions: Int
    ) {
        if (solutions.size >= maxSolutions) return

        val emptyCell = findEmptyCell(board)
        if (emptyCell == null) {
            // Board is complete, add solution
            val solution = Array(9) { row ->
                IntArray(9) { col ->
                    board[row][col]
                }
            }
            solutions.add(solution)
            return
        }

        val (row, col) = emptyCell
        for (num in 1..9) {
            if (GameUtils.isValidMove(board, row, col, num)) {
                board[row][col] = num
                findAllSolutions(board, solutions, maxSolutions)
                board[row][col] = 0

                if (solutions.size >= maxSolutions) return
            }
        }
    }

    private fun findEmptyCell(board: Array<IntArray>): Pair<Int, Int>? {
        for (row in 0..8) {
            for (col in 0..8) {
                if (board[row][col] == 0) {
                    return Pair(row, col)
                }
            }
        }
        return null
    }
}