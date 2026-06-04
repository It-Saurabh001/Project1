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
    Log.d(TAG, "   - totalGamesPlayed: ${statistics.totalGamesPlayed}")
    Log.d(TAG, "   - gamesLost: ${statistics.gamesLost}")
    Log.d(TAG, "   - totalPlayTime: ${statistics.totalPlayTime}")
    Log.d(TAG, "   - totalHintsUsed: ${statistics.totalHintsUsed}")

    val avgTime = statistics.getAverageTime()
    val winRate = statistics.getWinRatePercent()
    Log.d(TAG, "   - averageTime: $avgTime")
    Log.d(TAG, "   - winRate: $winRate%")

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
                val totalGamesValue = statistics.totalGamesPlayed.toString()
                Log.d(TAG, "   - Total Games Played value: $totalGamesValue")

                StatCard(
                    title = "Total Games Played",
                    value = totalGamesValue,
                    modifier = Modifier.weight(1f)
                )

                val gamesWonValue = statistics.gamesCompleted.toString()
                Log.d(TAG, "   - Games Won value: $gamesWonValue")

                StatCard(
                    title = "Games Won",
                    value = gamesWonValue,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val gamesLostValue = statistics.gamesLost.toString()
                Log.d(TAG, "   - Games Lost value: $gamesLostValue")

                StatCard(
                    title = "Games Lost",
                    value = gamesLostValue,
                    modifier = Modifier.weight(1f)
                )

                val winRateValue = "${winRate}%"
                Log.d(TAG, "   - Win Rate value: $winRateValue")

                StatCard(
                    title = "Win Rate",
                    value = winRateValue,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
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
