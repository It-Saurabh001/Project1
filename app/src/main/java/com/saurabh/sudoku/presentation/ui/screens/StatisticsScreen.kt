package com.saurabh.sudoku.presentation.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saurabh.sudoku.presentation.ui.components.AchievementStatsCard
import com.saurabh.sudoku.presentation.ui.components.AchievementsListCard
import com.saurabh.sudoku.presentation.ui.components.BestTimesCard
import com.saurabh.sudoku.presentation.ui.components.OverallStatsCard
import com.saurabh.sudoku.presentation.viewmodel.StatisticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val TAG = "StatisticsScreen"
    Log.d(TAG, "StatisticsScreen composable called")
    val statistics by viewModel.statistics.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier
            .testTag("statistics_screen")
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshStatistics() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        // Sirf statistics show karo - chahe 0 ho ya kuch bhi
        // Koi "Play a Game" button nahi dikhana
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overall Stats - 0 dikhega agar koi game nahi khela
            OverallStatsCard(statistics = statistics)

            // Best Times - "--:--" dikhega agar koi best time nahi hai
            BestTimesCard(statistics = statistics)

            // Achievement Stats - 0 dikhega agar koi streak nahi hai
            AchievementStatsCard(statistics = statistics)

            // Achievements Badge Room
            AchievementsListCard(achievements = achievements)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}