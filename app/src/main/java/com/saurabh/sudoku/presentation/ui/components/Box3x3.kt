package com.saurabh.sudoku.presentation.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saurabh.sudoku.domain.model.Notes
import com.saurabh.sudoku.domain.model.SudokuBoard

@Composable
fun Box3x3(
    board: SudokuBoard,
    notes: Notes,
    boxRow: Int,
    boxCol: Int,
    selectedCell: Pair<Int, Int>?,
    highlightedCells: Set<Pair<Int, Int>>,
    sameNumberCells: Set<Pair<Int, Int>>,
    conflictCells: Set<Pair<Int, Int>>,
    highlightErrors: Boolean,
    isNotesMode: Boolean,
    lastPlacedCell: Pair<Int, Int>?,
    lastMistakeCell: Pair<Int, Int>?,
    onCellClick: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .border(2.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(4.dp))
            .padding(2.dp)
    ) {
        for (row in 0..2) {
            Row(modifier = Modifier.weight(1f)) {
                for (col in 0..2) {
                    val r = boxRow * 3 + row
                    val c = boxCol * 3 + col
                    val cellPair = r to c

                    SudokuCell(
                        value         = board.getCurrentValue(r, c),
                        isInitial     = board.isInitialCell(r, c),
                        isSelected    = selectedCell == cellPair,
                        isHighlighted = cellPair in highlightedCells && selectedCell != cellPair,
                        isSameNumber  = cellPair in sameNumberCells,
                        hasConflict   = cellPair in conflictCells && highlightErrors,
                        notes         = notes.getNotes(r, c),
                        isNotesMode   = isNotesMode,
                        justPlaced    = lastPlacedCell == cellPair,
                        justMistake   = lastMistakeCell == cellPair,
                        onClick       = { onCellClick(r, c) },
                        modifier      = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}