package com.saurabh.sudoku.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

object GameUtils {
    data class Hint(val row: Int, val col: Int, val value: Int)
    fun getHint(currentBoard: Array<IntArray>, solution: Array<IntArray>): Hint? {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in currentBoard.indices) {
            for (c in currentBoard[r].indices) {
                if (currentBoard[r][c] == Constants.EMPTY_CELL) {
                    emptyCells.add(r to c)
                }
            }
        }

        if (emptyCells.isEmpty()) {
            return null // No empty cells left to give a hint for
        }
        // Pick a random empty cell
        val (row, col) = emptyCells.random()
        val solutionValue = solution[row][col]

        return Hint(row, col, solutionValue)
    }

    /**
     * Return a hint for the specific selected cell.
     * Returns null if the cell is not empty or no valid solution value found.
     */
    fun getHintForCell(currentBoard: Array<IntArray>, solution: Array<IntArray>, row: Int, col: Int): Hint? {
        if (row !in 0..8 || col !in 0..8) return null
        if (currentBoard[row][col] != Constants.EMPTY_CELL) return null
        val solutionValue = solution[row][col]
        // Always provide hint for empty cell
        return Hint(row, col, solutionValue)
    }

    /**
     * Apply hint value into the board for the selected cell.
     * Returns true if applied successfully.
     */
    fun applyHintToCell(currentBoard: Array<IntArray>, solution: Array<IntArray>, row: Int, col: Int): Boolean {
        val hint = getHintForCell(currentBoard, solution, row, col) ?: return false
        currentBoard[row][col] = hint.value
        return true
    }


    /**
     * Check if a number can be placed at row,col.
     * Alias to isValidMove for compatibility with calls like game.board.canPlaceNumber(...)
     */
    fun canPlaceNumber(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        return isValidMove(board, row, col, num)
    }

    /**
     * Helper to decrement a number count in a numberpad representation.
     * Expects counts size >= 9, index 0 -> number 1.
     * Returns true when decremented, false if count was already zero or invalid input.
     */
    fun decrementNumberPad(counts: IntArray, number: Int): Boolean {
        if (number !in 1..9) return false
        val idx = number - 1
        if (idx >= counts.size) return false
        if (counts[idx] <= 0) return false
        counts[idx] = counts[idx] - 1
        return true
    }


    fun isValidMove(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
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

    fun isBoardComplete(board: Array<IntArray>): Boolean {
        for (i in 0..8) {
            for (j in 0..8) {
                if (board[i][j] == 0) return false
            }
        }
        return isBoardValid(board)
    }

    fun isBoardValid(board: Array<IntArray>): Boolean {
        // Check all rows
        for (row in 0..8) {
            val seen = mutableSetOf<Int>()
            for (col in 0..8) {
                val num = board[row][col]
                if (num != 0) {
                    if (seen.contains(num)) return false
                    seen.add(num)
                }
            }
        }

        // Check all columns
        for (col in 0..8) {
            val seen = mutableSetOf<Int>()
            for (row in 0..8) {
                val num = board[row][col]
                if (num != 0) {
                    if (seen.contains(num)) return false
                    seen.add(num)
                }
            }
        }

        // Check all 3x3 boxes
        for (boxRow in 0..2) {
            for (boxCol in 0..2) {
                val seen = mutableSetOf<Int>()
                for (row in 0..2) {
                    for (col in 0..2) {
                        val num = board[boxRow * 3 + row][boxCol * 3 + col]
                        if (num != 0) {
                            if (seen.contains(num)) return false
                            seen.add(num)
                        }
                    }
                }
            }
        }

        return true
    }

    /**
     * Returns all cells in the same row, column and 3x3 box as (row, col).
     * Used for highlighting "related" cells when a cell is selected.
     */
    fun getRelatedCells(row: Int, col: Int): Set<Pair<Int, Int>> {
        val related = mutableSetOf<Pair<Int, Int>>()
        for (i in 0..8) {
            related.add(row to i)  // same row
            related.add(i to col)  // same column
        }
        val startRow = row - row % 3
        val startCol = col - col % 3
        for (r in startRow until startRow + 3) {
            for (c in startCol until startCol + 3) {
                related.add(r to c)
            }
        }
        related.remove(row to col) // exclude the selected cell itself
        return related
    }

    /**
     * Returns all cells (other than the selected cell) that hold the same non-zero value.
     */
    fun getSameNumberCells(board: Array<IntArray>, row: Int, col: Int): Set<Pair<Int, Int>> {
        val value = board[row][col]
        if (value == 0) return emptySet()
        val result = mutableSetOf<Pair<Int, Int>>()
        for (r in board.indices) {
            for (c in board[r].indices) {
                if ((r != row || c != col) && board[r][c] == value) {
                    result.add(r to c)
                }
            }
        }
        return result
    }

    /**
     * Computes all conflict cells using TWO strategies combined:
     * 1. Cells whose value does not match the solution (wrong answer).
     * 2. Cells that duplicate the same number in their row/col/box (logical conflict).
     * This catches mistakes even when the solution isn't considered.
     */
    fun computeAllConflicts(board: Array<IntArray>, solution: Array<IntArray>): Set<Pair<Int, Int>> {
        val conflicts = mutableSetOf<Pair<Int, Int>>()
        for (r in board.indices) {
            for (c in board[r].indices) {
                val v = board[r][c]
                if (v == 0) continue
                // Strategy 1: wrong vs solution
                if (solution[r][c] != v) {
                    conflicts.add(r to c)
                    continue
                }
                // Strategy 2: duplicate in row/col/box (catches board-level conflicts)
                if (getCellConflicts(board, r, c).isNotEmpty()) {
                    conflicts.add(r to c)
                }
            }
        }
        return conflicts
    }

    fun getCellConflicts(board: Array<IntArray>, row: Int, col: Int): Set<Pair<Int, Int>> {
        val conflicts = mutableSetOf<Pair<Int, Int>>()
        val num = board[row][col]

        if (num == 0) return conflicts

        // Check row conflicts
        for (c in 0..8) {
            if (c != col && board[row][c] == num) {
                conflicts.add(Pair(row, c))
            }
        }

        // Check column conflicts
        for (r in 0..8) {
            if (r != row && board[r][col] == num) {
                conflicts.add(Pair(r, col))
            }
        }

        // Check box conflicts
        val startRow = row - row % 3
        val startCol = col - col % 3

        for (r in startRow until startRow + 3) {
            for (c in startCol until startCol + 3) {
                if ((r != row || c != col) && board[r][c] == num) {
                    conflicts.add(Pair(r, c))
                }
            }
        }

        return conflicts
    }
}

fun Array<IntArray>.flatten(): IntArray {
    val result = IntArray(81)
    for (i in 0..8) {
        for (j in 0..8) {
            result[i * 9 + j] = this[i][j]
        }
    }
    return result
}


fun Long.formatTime(): String {
    val seconds = this / 1000
    val minutes = seconds / 60
    val hours = minutes / 60

    return when {
        hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
        else -> String.format("%02d:%02d", minutes, seconds % 60)
    }
}


@Composable
fun OnLifecycleEvent(onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit) {
    val eventHandler = rememberUpdatedState(onEvent)
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { owner, event ->
            eventHandler.value(owner, event)
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}