package com.saurabh.sudoku.presentation.ui.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.saurabh.sudoku.utils.Constants

@Composable
fun NumberPad(
    numberCounts: Map<Int, Int>,
    onNumberSelected: (Int) -> Unit,
    onEraseSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .testTag("number_pad")
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val rows = (1..9).chunked(3)
        rows.forEach { rowNumbers ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (number in rowNumbers) {
                    val count = numberCounts.getOrDefault(number, 0)
                    val isEnabled = count < Constants.GRID_SIZE // GRID_SIZE is 9
                    NumberButton(
                        number = number,
                        count = count,
                        isEnabled = isEnabled,
                        onClick = { if (isEnabled) onNumberSelected(number) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }


        // Erase button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            EraseButton(
                onClick = onEraseSelected,
                modifier = Modifier.width(120.dp)
            )
        }
    }
}
