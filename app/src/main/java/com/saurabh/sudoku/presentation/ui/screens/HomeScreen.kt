package com.saurabh.sudoku.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saurabh.sudoku.domain.model.GameState
import com.saurabh.sudoku.presentation.ui.components.ContinueGameCard
import com.saurabh.sudoku.presentation.ui.components.LoadingDialog
import com.saurabh.sudoku.presentation.ui.components.NewGameSection
import com.saurabh.sudoku.presentation.ui.components.QuickStatsCard
import com.saurabh.sudoku.presentation.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToGame: (String) -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel :HomeViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentGame by viewModel.currentGame.collectAsStateWithLifecycle()
    val statistics by viewModel.statistics.collectAsStateWithLifecycle()

    // Handle navigation
    LaunchedEffect(uiState.navigateToGame, uiState.generatedGameId) {
        if (uiState.navigateToGame && uiState.generatedGameId != null) {
            onNavigateToGame(uiState.generatedGameId!!)
            viewModel.onNavigatedToGame()
        }
    }

    // Show loading dialog
    if (uiState.isGeneratingPuzzle) {
        LoadingDialog(message = "Generating new puzzle...")
    }

    // Show error snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or toast
            viewModel.clearError()
        }
    }

    Column(
        modifier = modifier
            .testTag("home_screen")
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Title
        Text(
            text = "Sudoku",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 32.dp)
        )

        // Continue Game Section
        currentGame?.let { game ->
            if (game.state != GameState.COMPLETED) {
                ContinueGameCard(
                    game = game,
                    onContinueClick = { viewModel.continueCurrentGame() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )
            }
        }

        // New Game Section
        NewGameSection(
            selectedDifficulty = uiState.selectedDifficulty,
            onDifficultySelected = viewModel::onDifficultySelected,
            onStartNewGame = viewModel::startNewGame,
            isLoading = uiState.isGeneratingPuzzle,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        // Quick Stats
        QuickStatsCard(
            statistics = statistics,
            onViewAllStats = onNavigateToStatistics,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        // Settings Button
        OutlinedButton(
            onClick = onNavigateToSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Settings")
        }
    }
}
