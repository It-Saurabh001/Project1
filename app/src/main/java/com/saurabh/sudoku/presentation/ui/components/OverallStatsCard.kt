package com.saurabh.sudoku.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saurabh.sudoku.domain.model.Statistics
import com.saurabh.sudoku.utils.formatTime


@Composable
fun OverallStatsCard(
    statistics: Statistics,
    modifier: Modifier = Modifier
) {
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
                StatCard(
                    title = "Games Completed",
                    value = statistics.gamesCompleted.toString(),
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Total Play Time",
                    value = statistics.totalPlayTime.formatTime(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    title = "Average Time",
                    value = statistics.getAverageTime().formatTime(),
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Hints Used",
                    value = statistics.totalHintsUsed.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}