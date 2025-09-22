package com.saurabh.sudoku.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.saurabh.sudoku.presentation.ui.theme.CellDefault
import com.saurabh.sudoku.presentation.ui.theme.CellError
import com.saurabh.sudoku.presentation.ui.theme.CellFixed
import com.saurabh.sudoku.presentation.ui.theme.CellSelected
import com.saurabh.sudoku.presentation.ui.theme.SudokuBlue
import com.saurabh.sudoku.presentation.ui.theme.SudokuCellTextStyle
import com.saurabh.sudoku.presentation.ui.theme.TextError
import com.saurabh.sudoku.presentation.ui.theme.TextPrimary
import com.saurabh.sudoku.presentation.ui.theme.TextSecondary


@Composable
fun SudokuCell(
    value: Int,
    isInitial: Boolean,
    isSelected: Boolean,
    hasConflict: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        hasConflict -> CellError
        isSelected -> CellSelected
        isInitial -> CellFixed
        else -> CellDefault
    }

    val textColor = when {
        hasConflict -> TextError
        isInitial -> TextPrimary
        else -> TextSecondary
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .background(backgroundColor, RoundedCornerShape(2.dp))
            .border(
                width = if (isSelected) 2.dp else 0.5.dp,
                color = if (isSelected) SudokuBlue else Color.Gray,
                shape = RoundedCornerShape(2.dp)
            )
            .clickable { onClick() }
            .padding(2.dp)
    ) {
        if (value != 0) {
            Text(
                text = value.toString(),
                style = SudokuCellTextStyle.copy(
                    color = textColor,
                    fontWeight = if (isInitial) FontWeight.Bold else FontWeight.Normal
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}