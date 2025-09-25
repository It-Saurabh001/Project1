package com.saurabh.sudoku.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import com.saurabh.sudoku.domain.model.GameState
import com.saurabh.sudoku.presentation.ui.components.GameCompletedDialog
import com.saurabh.sudoku.presentation.ui.components.GameToolbar
import com.saurabh.sudoku.presentation.ui.components.LoadingDialog
import com.saurabh.sudoku.presentation.ui.components.NumberPad
import com.saurabh.sudoku.presentation.ui.components.SudokuBoard
import com.saurabh.sudoku.presentation.viewmodel.GameViewModel
import com.saurabh.sudoku.utils.Constants
import com.saurabh.sudoku.utils.OnLifecycleEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Load game on first composition
    LaunchedEffect(gameId) {
        viewModel.loadGame(gameId)
    }

    // Handle lifecycle events
    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_PAUSE -> {
                if (uiState.game?.state == GameState.PLAYING) {
                    viewModel.onPauseToggle()
                }
            }
            Lifecycle.Event.ON_RESUME -> {
                if (uiState.game?.state == GameState.PAUSED) {
                    viewModel.onPauseToggle()
                }
            }
            else -> {}
        }
    }

    // Show loading
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Show error
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error and navigate back
            onNavigateBack()
            viewModel.clearError()
        }
        return
    }

    val game = uiState.game ?: return

    // Show completion dialog
    if (uiState.showCompletionDialog && uiState.completionTime != null) {
        GameCompletedDialog(
            difficulty = game.difficulty,
            completionTime = uiState.completionTime!!,
            hintsUsed = game.hintsUsed,
            onNewGame = {
                viewModel.dismissCompletionDialog()
                viewModel.onNewGame()
            },
            onMainMenu = {
                viewModel.dismissCompletionDialog()
                onNavigateBack()
            }
        )
    }

    // Show hint dialog
    uiState.showHint?.let { hint ->
        AlertDialog(
            onDismissRequest = viewModel::dismissHint,
            title = { Text("Hint") },
            text = { Text(uiState.hintMessage ?: "") },
            confirmButton = {
                TextButton(onClick = viewModel::dismissHint) {
                    Text("OK")
                }
            }
        )
    }

    // Show new game loading
    if (uiState.isGeneratingNewGame) {
        LoadingDialog(message = "Generating new puzzle...")
    }

    Column(
        modifier = modifier
            .testTag("game_screen")
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text("${game.difficulty.displayName} Sudoku")
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        // Game Toolbar
        GameToolbar(
            elapsedTime = game.elapsedTime,
            hintsRemaining = Constants.MAX_HINTS - game.hintsUsed,
            isPaused = game.state == GameState.PAUSED,
            onPauseClick = viewModel::onPauseToggle,
            onHintClick = viewModel::onHintRequested,
            onUndoClick = viewModel::onUndoMove,
            onRedoClick = viewModel::onRedoMove,
            onNewGameClick = viewModel::onNewGame,
            modifier = Modifier.fillMaxWidth()
        )

        // Game Board
        if (game.state == GameState.PAUSED) {
            // Show paused state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Game Paused",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap resume to continue",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            SudokuBoard(
                board = game.board,
                selectedCell = uiState.selectedCell,
                conflictCells = uiState.conflictCells,
                highlightErrors = true,
                onCellClick = viewModel::onCellSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }

        // Number Pad
        if (game.state != GameState.PAUSED && game.state != GameState.COMPLETED) {
            NumberPad(
                onNumberSelected = viewModel::onNumberSelected,
                onEraseSelected = viewModel::onEraseSelected,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}