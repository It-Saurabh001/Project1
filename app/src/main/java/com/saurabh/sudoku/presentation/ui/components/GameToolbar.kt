package com.saurabh.sudoku.presentation.ui.components

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saurabh.sudoku.presentation.ui.theme.SudokuTimerTextStyle
import com.saurabh.sudoku.presentation.utils.formatTime

@Composable
fun GameToolbar(
    elapsedTime: Long,
    hintsRemaining: Int,
    isPaused: Boolean,
    mistakes: Int,
    maxMistakes: Int,
    isNotesMode: Boolean,
    onNotesModeToggle: () -> Unit,
    onPauseClick: () -> Unit,
    onHintClick: () -> Unit,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val TAG = "GameToolbar"
    Log.d(TAG, "GameToolbar composed with elapsedTime=$elapsedTime, hintsRemaining=$hintsRemaining, isPaused=$isPaused, mistakes=$mistakes/$maxMistakes, isNotesMode=$isNotesMode")
    val notesBgColor by animateColorAsState(
        targetValue = if (isNotesMode)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(200),
        label = "notesBg"
    )
    val notesIconColor by animateColorAsState(
        targetValue = if (isNotesMode)
            MaterialTheme.colorScheme.onPrimaryContainer
        else
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        animationSpec = tween(200),
        label = "notesIcon"
    )

    Column(
        modifier = modifier
            .testTag("game_toolbar")
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        // ── Row 1: Timer | Mistakes | Notes toggle ────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Timer
            Text(
                text = elapsedTime.formatTime(),
                style = SudokuTimerTextStyle,
                fontWeight = FontWeight.SemiBold
            )

            // Mistakes pill
            MistakeCounter(currentMistakes = mistakes, maxMistakes = maxMistakes)

            // Notes mode toggle pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(notesBgColor)
                    .padding(0.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onNotesModeToggle) {
                    Icon(
                        imageVector = if (isNotesMode) Icons.Default.EditNote else Icons.Outlined.EditNote,
                        contentDescription = if (isNotesMode) "Switch to Number mode" else "Switch to Notes mode",
                        tint = notesIconColor
                    )
                }
            }
        }

        // ── Row 2: Action buttons ─────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolbarButton(
                icon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                onClick = onPauseClick,
                contentDescription = if (isPaused) "Resume game" else "Pause game"
            )
            ToolbarButton(
                icon = Icons.Default.Lightbulb,
                onClick = onHintClick,
                enabled = hintsRemaining > 0,
                contentDescription = "Hint ($hintsRemaining left)"
            )
            ToolbarButton(
                icon = Icons.AutoMirrored.Filled.Undo,
                onClick = onUndoClick,
                contentDescription = "Undo last move"
            )
            ToolbarButton(
                icon = Icons.AutoMirrored.Filled.Redo,
                onClick = onRedoClick,
                contentDescription = "Redo move"
            )
            ToolbarButton(
                icon = Icons.Default.Refresh,
                onClick = onResetClick,
                contentDescription = "Reset board"
            )
        }
    }
}