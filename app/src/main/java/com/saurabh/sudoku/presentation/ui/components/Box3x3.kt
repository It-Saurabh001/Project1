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
import com.saurabh.sudoku.domain.model.SudokuBoard

@Composable
fun Box3x3(
    board: SudokuBoard,
    boxRow: Int,
    boxCol: Int,
    selectedCell: Pair<Int, Int>?,
    conflictCells: Set<Pair<Int, Int>>,
    highlightErrors: Boolean,
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
                    val actualRow = boxRow * 3 + row
                    val actualCol = boxCol * 3 + col

                    SudokuCell(
                        value = board.getCurrentValue(actualRow, actualCol),
                        isInitial = board.isInitialCell(actualRow, actualCol),
                        isSelected = selectedCell == Pair(actualRow, actualCol),
                        hasConflict = conflictCells.contains(Pair(actualRow, actualCol)) && highlightErrors,
                        onClick = { onCellClick(actualRow, actualCol) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}