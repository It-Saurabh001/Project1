package com.saurabh.sudoku.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.saurabh.sudoku.domain.model.Notes
import com.saurabh.sudoku.domain.model.SudokuBoard

@Composable
fun SudokuBoard(
    board: SudokuBoard,
    notes: Notes,
    selectedCell: Pair<Int, Int>?,
    highlightedCells: Set<Pair<Int, Int>> = emptySet(),
    sameNumberCells: Set<Pair<Int, Int>> = emptySet(),
    conflictCells: Set<Pair<Int, Int>> = emptySet(),
    highlightErrors: Boolean = true,
    isNotesMode: Boolean = false,
    lastPlacedCell: Pair<Int, Int>? = null,
    lastMistakeCell: Pair<Int, Int>? = null,
    onCellClick: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .testTag("sudoku_board")
            .aspectRatio(1f)
            .padding(8.dp)
    ) {
        for (boxRow in 0..2) {
            Row(modifier = Modifier.weight(1f)) {
                for (boxCol in 0..2) {
                    Box3x3(
                        board           = board,
                        notes           = notes,
                        boxRow          = boxRow,
                        boxCol          = boxCol,
                        selectedCell    = selectedCell,
                        highlightedCells = highlightedCells,
                        sameNumberCells = sameNumberCells,
                        conflictCells   = conflictCells,
                        highlightErrors = highlightErrors,
                        isNotesMode     = isNotesMode,
                        lastPlacedCell  = lastPlacedCell,
                        lastMistakeCell = lastMistakeCell,
                        onCellClick     = onCellClick,
                        modifier        = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}