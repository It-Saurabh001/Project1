package com.saurabh.sudoku.data.generator

import com.saurabh.sudoku.data.generator.SudokuSolver
import com.saurabh.sudoku.domain.model.Difficulty
import com.saurabh.sudoku.domain.model.SudokuBoard


class SudokuGenerator {

     val solver = SudokuSolver()

    fun generatePuzzle(difficulty: Difficulty): SudokuBoard {
        val solution = generateCompleteSudoku()
        val puzzle = createPuzzle(solution, difficulty.clues)
        val initialBoard = Array(9) { row ->
            IntArray(9) { col ->
                puzzle[row][col]
            }
        }

        return SudokuBoard(
            board = Array(9) { row -> IntArray(9) { col -> puzzle[row][col] } },
            solution = solution,
            initialBoard = initialBoard
        )
    }

     fun generateCompleteSudoku(): Array<IntArray> {
        val board = Array(9) { IntArray(9) }
        fillBoard(board)
        return board
    }

    private fun fillBoard(board: Array<IntArray>): Boolean {
        for (row in 0..8) {
            for (col in 0..8) {
                if (board[row][col] == 0) {
                    val numbers = (1..9).shuffled()
                    for (num in numbers) {
                        if (isValid(board, row, col, num)) {
                            board[row][col] = num

                            if (fillBoard(board)) {
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

    private fun createPuzzle(solution: Array<IntArray>, clues: Int): Array<IntArray> {
        val puzzle = Array(9) { row ->
            IntArray(9) { col ->
                solution[row][col]
            }
        }

        val totalCells = 81
        val cellsToRemove = totalCells - clues
        val positions = mutableListOf<Pair<Int, Int>>()

        // Create list of all positions
        for (row in 0..8) {
            for (col in 0..8) {
                positions.add(Pair(row, col))
            }
        }

        positions.shuffle()

        var removed = 0
        for ((row, col) in positions) {
            if (removed >= cellsToRemove) break

            val temp = puzzle[row][col]
            puzzle[row][col] = 0

            // Check if puzzle still has unique solution
            val testPuzzle = Array(9) { r ->
                IntArray(9) { c ->
                    puzzle[r][c]
                }
            }

            if (solver.hasUniqueSolution(testPuzzle)) {
                removed++
            } else {
                puzzle[row][col] = temp // Restore if not unique
            }
        }

        return puzzle
    }

    private fun isValid(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        // Check row
        for (x in 0..8) {
            if (board[row][x] == num) return false
        }

        // Check column
        for (x in 0..8) {
            if (board[x][col] == num) return false
        }

        // Check 3x3 box
        val startRow = row - row % 3
        val startCol = col - col % 3

        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i + startRow][j + startCol] == num) return false
            }
        }

        return true
    }
}