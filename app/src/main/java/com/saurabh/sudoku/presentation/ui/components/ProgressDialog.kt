package com.saurabh.sudoku.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.saurabh.sudoku.domain.model.Difficulty

@Composable
fun GameCompletedDialog(
    difficulty: Difficulty,
    completionTime: Long,
    hintsUsed: Int,
    onNewGame: () -> Unit,
    onMainMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onMainMenu) {
        Card(
            modifier = modifier
                .testTag("completion_dialog")
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎉 Congratulations! 🎉",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "You completed the ${difficulty.displayName} puzzle!",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Stats
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatRow(label = "Time:", value = completionTime.formatTime())
                    StatRow(label = "Difficulty:", value = difficulty.displayName)
                    StatRow(label = "Hints used:", value = hintsUsed.toString())
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onMainMenu,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Main Menu")
                    }

                    Button(
                        onClick = onNewGame,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("New Game")
                    }
                }
            }
        }
    }
}