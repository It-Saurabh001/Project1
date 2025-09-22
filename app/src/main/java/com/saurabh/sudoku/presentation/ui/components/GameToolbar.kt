package com.saurabh.sudoku.presentation.ui.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saurabh.sudoku.presentation.ui.theme.SudokuTimerTextStyle
import com.saurabh.sudoku.utils.formatTime

@Composable
fun GameToolbar(
    elapsedTime: Long,
    hintsRemaining: Int,
    isPaused: Boolean,
    onPauseClick: () -> Unit,
    onHintClick: () -> Unit,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onNewGameClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .testTag("game_toolbar")
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Timer
        Text(
            text = elapsedTime.formatTime(),
            style = SudokuTimerTextStyle,
            fontWeight = FontWeight.Medium
        )

        // Action buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ToolbarButton(
                icon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                onClick = onPauseClick,
                contentDescription = if (isPaused) "Resume" else "Pause"
            )

            ToolbarButton(
                icon = Icons.Default.Lightbulb,
                onClick = onHintClick,
                enabled = hintsRemaining > 0,
                contentDescription = "Hint ($hintsRemaining remaining)"
            )

            ToolbarButton(
                icon = Icons.Default.Undo,
                onClick = onUndoClick,
                contentDescription = "Undo"
            )

            ToolbarButton(
                icon = Icons.Default.Redo,
                onClick = onRedoClick,
                contentDescription = "Redo"
            )

            ToolbarButton(
                icon = Icons.Default.Refresh,
                onClick = onNewGameClick,
                contentDescription = "New Game"
            )
        }
    }
}