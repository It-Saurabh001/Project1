package com.saurabh.sudoku.presentation.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saurabh.sudoku.domain.model.Statistics
import com.saurabh.sudoku.presentation.utils.formatTime

private const val TAG = "OverallStatsCard"

@Composable
fun OverallStatsCard(
    statistics: Statistics,
    modifier: Modifier = Modifier
) {
    Log.d(TAG, "🎴 OverallStatsCard rendering")
    Log.d(TAG, "   - gamesCompleted: ${statistics.gamesCompleted}")
    Log.d(TAG, "   - totalPlayTime: ${statistics.totalPlayTime}")
    Log.d(TAG, "   - totalHintsUsed: ${statistics.totalHintsUsed}")

    val avgTime = statistics.getAverageTime()
    Log.d(TAG, "   - averageTime: $avgTime")

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Overall Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val gamesValue = statistics.gamesCompleted.toString()
                Log.d(TAG, "   - Games Completed value: $gamesValue")

                StatCard(
                    title = "Games Completed",
                    value = gamesValue,
                    modifier = Modifier.weight(1f)
                )

                val totalTimeValue = if (statistics.totalPlayTime > 0)
                    statistics.totalPlayTime.formatTime()
                else
                    "00:00"
                Log.d(TAG, "   - Total Play Time value: $totalTimeValue")

                StatCard(
                    title = "Total Play Time",
                    value = totalTimeValue,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val avgTimeValue = if (avgTime > 0)
                    avgTime.formatTime()
                else
                    "00:00"
                Log.d(TAG, "   - Average Time value: $avgTimeValue")

                StatCard(
                    title = "Average Time",
                    value = avgTimeValue,
                    modifier = Modifier.weight(1f)
                )

                val hintsValue = statistics.totalHintsUsed.toString()
                Log.d(TAG, "   - Hints Used value: $hintsValue")

                StatCard(
                    title = "Hints Used",
                    value = hintsValue,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatCard1(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Log.d(TAG, "   📇 StatCard - $title: $value")

    Card(
        modifier = modifier.padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}