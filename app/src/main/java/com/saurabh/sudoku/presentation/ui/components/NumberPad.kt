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

@Composable
fun NumberPad(
    onNumberSelected: (Int) -> Unit,
    onEraseSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .testTag("number_pad")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Numbers 1-3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (number in 1..3) {
                NumberButton(
                    number = number,
                    onClick = { onNumberSelected(number) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Numbers 4-6
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (number in 4..6) {
                NumberButton(
                    number = number,
                    onClick = { onNumberSelected(number) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Numbers 7-9
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (number in 7..9) {
                NumberButton(
                    number = number,
                    onClick = { onNumberSelected(number) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Erase button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            EraseButton(
                onClick = onEraseSelected,
                modifier = Modifier.width(120.dp)
            )
        }
    }
}