package com.saurabh.sudoku.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saurabh.sudoku.domain.model.Difficulty
import com.saurabh.sudoku.domain.model.Statistics
import com.saurabh.sudoku.utils.formatTime


@Composable
fun BestTimesCard(
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
                text = "Best Times",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Difficulty.values().forEach { difficulty ->
                val bestTime = statistics.getBestTime(difficulty)
                DifficultyTimeRow(
                    difficulty = difficulty,
                    time = if (bestTime == Long.MAX_VALUE) "--:--" else bestTime.formatTime()
                )
                if (difficulty != Difficulty.EXPERT) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
