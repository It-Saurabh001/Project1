package com.saurabh.sudoku.presentation.ui.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.saurabh.sudoku.presentation.utils.Constants

@Composable
fun NumberPad(
    numberCounts: Map<Int, Int>,
    onNumberSelected: (Int) -> Unit,
    onEraseSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = modifier
                .testTag("number_pad")
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Number buttons 1-9
            for (number in 1..9) {
                val count = numberCounts.getOrDefault(number, 0)
                val isEnabled = count < Constants.GRID_SIZE // GRID_SIZE is 9
                NumberButton(
                    number = number,
                    count = count,
                    isEnabled = isEnabled,
                    onClick = { if (isEnabled) onNumberSelected(number) },
                    modifier = Modifier.weight(1f) // Each button takes equal space
                )
            }
        }
        // Erase button at the end
        EraseButton(
            onClick = onEraseSelected,
            modifier = Modifier.width(80.dp) // Adjusted width for erase button
        )
    }
}
