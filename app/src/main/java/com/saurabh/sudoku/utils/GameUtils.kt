package com.saurabh.sudoku.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

object GameUtils {
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
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

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