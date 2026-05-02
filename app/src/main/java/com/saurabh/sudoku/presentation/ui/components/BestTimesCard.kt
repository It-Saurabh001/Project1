package com.saurabh.sudoku.presentation.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saurabh.sudoku.domain.model.Difficulty
import com.saurabh.sudoku.domain.model.Statistics
import com.saurabh.sudoku.presentation.utils.formatTime

private const val TAG = "BestTimesCard"

@Composable
fun BestTimesCard(
    statistics: Statistics,
    modifier: Modifier = Modifier
) {
    Log.d(TAG, "🏆 BestTimesCard rendering")

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Best Times",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Difficulty.values().forEach { difficulty ->
                val bestTime = statistics.getBestTime(difficulty)
                val timeString = if (bestTime == 0L) "--:--" else bestTime.formatTime()

                Log.d(TAG, "   - ${difficulty.name}: bestTime=$bestTime, display=$timeString")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = difficulty.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (bestTime > 0L)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = timeString,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (bestTime > 0L)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (difficulty != Difficulty.EXPERT) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}