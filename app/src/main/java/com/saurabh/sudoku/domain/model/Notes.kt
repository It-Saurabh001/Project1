package com.saurabh.sudoku.domain.model

import java.io.Serializable  // <-- CHANGE: Parcelable ki jagah Serializable

data class Notes(
    val notes: Array<Array<MutableSet<Int>>> = Array(9) { Array(9) { mutableSetOf() } }
) : Serializable {  // <-- CHANGE: Parcelable → Serializable

    fun addNote(row: Int, col: Int, number: Int) {
        if (number in 1..9) {
            notes[row][col].add(number)
        }
    }

    fun removeNote(row: Int, col: Int, number: Int) {
        notes[row][col].remove(number)
    }

    fun toggleNote(row: Int, col: Int, number: Int) {
        if (notes[row][col].contains(number)) {
            notes[row][col].remove(number)
        } else {
            notes[row][col].add(number)
        }
    }

    fun getNotes(row: Int, col: Int): Set<Int> = notes[row][col]

    fun clearNotes(row: Int, col: Int) {
        notes[row][col].clear()
    }

    fun clearAllNotes() {
        for (row in 0..8) {
            for (col in 0..8) {
                notes[row][col].clear()
            }
        }
    }

    fun autoRemoveNotes(row: Int, col: Int, number: Int, board: Array<IntArray>) {
        // Remove this number from notes in same row
        for (c in 0..8) {
            if (board[row][c] == 0) {
                notes[row][c].remove(number)
            }
        }

        // Remove this number from notes in same column
        for (r in 0..8) {
            if (board[r][col] == 0) {
                notes[r][col].remove(number)
            }
        }

        // Remove this number from notes in same 3x3 box
        val startRow = row - row % 3
        val startCol = col - col % 3
        for (r in startRow until startRow + 3) {
            for (c in startCol until startCol + 3) {
                if (board[r][c] == 0) {
                    notes[r][c].remove(number)
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Notes

        for (i in 0..8) {
            for (j in 0..8) {
                if (notes[i][j] != other.notes[i][j]) return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        return notes.contentDeepHashCode()
    }
}