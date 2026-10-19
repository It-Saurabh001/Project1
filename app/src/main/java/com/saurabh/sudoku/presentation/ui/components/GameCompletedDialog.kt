package com.saurabh.sudoku.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.LottieConstants
import com.saurabh.sudoku.domain.model.Difficulty

/**
 * Full-screen celebration card shown when the player wins.
 *
 * Layout (top → bottom inside a Dialog Card):
 *  ┌───────────────────────────────────────────────────────┐
 *  │  [  ScoreCelebrationEffect — Lottie fills full width  ]│  ← 220 dp tall
 *  │  ─────────────────────────────────────────────────── │
 *  │  🏆 Puzzle Solved!                                    │
 *  │  HARD  •  No hints                                    │
 *  │  ─────────────────────────────────────────────────── │
 *  │   ⏱ Time          🧩 Difficulty     💡 Hints Used     │  ← stats row
 *  │  ─────────────────────────────────────────────────── │
 *  │  [  Home  ]           [  New Game  ]                  │
 *  └───────────────────────────────────────────────────────┘
 *
 * The Dialog fills the screen width (usePlatformDefaultWidth = false) and is
 * horizontally padded by 24 dp so it looks like a card, not an alert box.
 */
@Composable
fun GameCompletedDialog(
    difficulty: Difficulty,
    completionTime: Long,
    hintsUsed: Int,
    onNewGame: () -> Unit,
    onMainMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animate the stats section sliding in after the Lottie starts
    var statsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(400)   // slight delay after dialog appears
        statsVisible = true
    }

    Dialog(
        onDismissRequest = onMainMenu,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,    // we control the width ourselves
            dismissOnClickOutside   = false     // player must explicitly choose
        )
    ) {
        Card(
            modifier = modifier
                .testTag("completion_dialog")
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape  = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier            = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ── Animation banner ──────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Lottie fills the entire banner — no hardcoded size
                    ScoreCelebrationEffect(
                        modifier   = Modifier.fillMaxSize(),
                        iterations = 3              // 3 loops then stops naturally
                    )
                }

                // ── Title ─────────────────────────────────────────────────
                AnimatedVisibility(
                    visible = statsVisible,
                    enter   = fadeIn(tween(400)) + slideInVertically(
                        tween(400),
                        initialOffsetY = { it / 3 }
                    )
                ) {
                    Column(
                        modifier            = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(top = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text       = "🏆 Puzzle Solved!",
                            style      = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color      = MaterialTheme.colorScheme.primary,
                            textAlign  = TextAlign.Center
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text      = buildString {
                                append(difficulty.displayName)
                                if (hintsUsed == 0) append("  •  No hints used ✨")
                            },
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )

                        // ── Stats row ─────────────────────────────────────
                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatChip(label = "Time",       value = formatTime(completionTime))
                            StatChip(label = "Difficulty", value = difficulty.displayName)
                            StatChip(label = "Hints",      value = hintsUsed.toString())
                        }

                        // ── Action buttons ────────────────────────────────
                        Spacer(Modifier.height(28.dp))

                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick  = onMainMenu,
                                modifier = Modifier.weight(1f),
                                shape    = RoundedCornerShape(12.dp)
                            ) {
                                Text("Home", fontWeight = FontWeight.Medium)
                            }

                            Button(
                                onClick  = onNewGame,
                                modifier = Modifier.weight(1f),
                                shape    = RoundedCornerShape(12.dp),
                                colors   = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("New Game", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/** Small pill chip used in the stats row. */
@Composable
private fun StatChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = value,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
        )
    }
}

/** Formats milliseconds → "mm:ss" string. */
private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes      = totalSeconds / 60
    val seconds      = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
private fun GameCompletedDialogPreview() {
    MaterialTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            GameCompletedDialog(
                difficulty     = Difficulty.HARD,
                completionTime = 312_000L,   // 5:12
                hintsUsed      = 0,
                onNewGame      = {},
                onMainMenu     = {}
            )
        }
    }
}
