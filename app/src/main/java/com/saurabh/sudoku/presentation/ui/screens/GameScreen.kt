package com.saurabh.sudoku.presentation.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saurabh.sudoku.domain.model.GameState
import com.saurabh.sudoku.presentation.ui.components.GameCompletedDialog
import com.saurabh.sudoku.presentation.ui.components.GameToolbar
import com.saurabh.sudoku.presentation.ui.components.LoadingDialog
import com.saurabh.sudoku.presentation.ui.components.NumberPad
import com.saurabh.sudoku.presentation.ui.components.SudokuBoard
import com.saurabh.sudoku.presentation.viewmodel.GameViewModel
import com.saurabh.sudoku.presentation.utils.Constants
import com.saurabh.sudoku.presentation.utils.OnLifecycleEvent
import com.saurabh.sudoku.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {

    val uiState by gameViewModel.uiState.collectAsStateWithLifecycle()
    val highlightErrors by settingsViewModel.highlightErrors.collectAsStateWithLifecycle()

    // Load game on first composition
    LaunchedEffect(gameId) {
        gameViewModel.loadGame(gameId)
    }

    // Handle lifecycle events
    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_PAUSE -> {
                if (uiState.game?.state == GameState.PLAYING) {
                    gameViewModel.onPauseToggle()
                }
            }
            // No need to auto-resume, user can do it manually.
            // This prevents the game from unpausing unexpectedly.
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
    uiState.error?.let {
        LaunchedEffect(it) {
            onNavigateBack()
            gameViewModel.clearError()
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
                gameViewModel.dismissCompletionDialog()
                gameViewModel.onNewGame()
            },
            onMainMenu = {
                gameViewModel.dismissCompletionDialog()
                onNavigateBack()
            }
        )
    }

    // Show hint dialog
    uiState.showHint?.let {
        AlertDialog(
            onDismissRequest = gameViewModel::dismissHint,
            title = { Text("Hint") },
            text = { Text(uiState.hintMessage ?: "") },
            confirmButton = {
                TextButton(onClick = gameViewModel::dismissHint) {
                    Text("OK")
                }
            }
        )
    }

    // Show new game loading
    if (uiState.isGeneratingNewGame) {
        LoadingDialog(message = "Generating new puzzle...")
    }

    Scaffold(
        modifier = modifier
            .testTag("game_screen")
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("${game.difficulty.displayName} Sudoku") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Game Toolbar
            GameToolbar(
                elapsedTime = game.elapsedTime,
                hintsRemaining = Constants.MAX_HINTS - game.hintsUsed,
                isPaused = game.state == GameState.PAUSED,
                onPauseClick = gameViewModel::onPauseToggle,
                onHintClick = gameViewModel::onHintRequested,
                onUndoClick = gameViewModel::onUndoMove,
                onRedoClick = gameViewModel::onRedoMove,
                onResetClick = gameViewModel::onResetBoard,
                modifier = Modifier.fillMaxWidth()
            )

            // --- UPDATED LOGIC ---
            // The SudokuBoard and NumberPad are now always visible,
            // regardless of the pause state.

            // Game Board
            SudokuBoard(
                board = game.board,
                selectedCell = uiState.selectedCell,
                conflictCells = uiState.conflictCells,
                highlightErrors = highlightErrors,
                onCellClick = gameViewModel::onCellSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            // Number Pad (only hidden when game is completed)
            if (game.state != GameState.COMPLETED) {
                NumberPad(
                    numberCounts = uiState.numberCounts,
                    onNumberSelected = gameViewModel::onNumberSelected,
                    onEraseSelected = gameViewModel::onEraseSelected,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
