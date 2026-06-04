package com.saurabh.sudoku.presentation.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
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
    val TAG = "NumberPad"
    Log.d(TAG, "NumberPad composed with numberCounts: $numberCounts")
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = modifier
                .testTag("number_pad")
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (number in 1..9) {
                val count = numberCounts.getOrDefault(number, 0)
                // Disable a number button only when all 9 of that number are placed
                val isEnabled = count > 0
                NumberButton(
                    number = number,
                    count = count,
                    isEnabled = isEnabled,
                    onClick = { if (isEnabled) onNumberSelected(number) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        EraseButton(
            onClick = onEraseSelected,
            modifier = Modifier.width(80.dp)
        )
    }
}
