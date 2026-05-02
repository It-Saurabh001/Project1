package com.saurabh.sudoku.presentation.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saurabh.sudoku.domain.model.GameState
import com.saurabh.sudoku.presentation.ui.components.*
import com.saurabh.sudoku.presentation.viewmodel.GameViewModel
import com.saurabh.sudoku.presentation.viewmodel.SettingsViewModel
import com.saurabh.sudoku.presentation.utils.Constants
import com.saurabh.sudoku.presentation.utils.OnLifecycleEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by gameViewModel.uiState.collectAsStateWithLifecycle()
    val highlightErrors by settingsViewModel.highlightErrors.collectAsStateWithLifecycle()
    val notesModeSetting by settingsViewModel.notesModeEnabled.collectAsStateWithLifecycle()
    val soundEnabled by settingsViewModel.soundEnabled.collectAsStateWithLifecycle()

    // Sync sound enabled setting into the ViewModel
    LaunchedEffect(soundEnabled) {
        gameViewModel.updateSoundEnabled(soundEnabled)
    }

    // Load game on first composition
    LaunchedEffect(gameId) {
        gameViewModel.loadGame(gameId)
    }

    // Hint / error messages
    LaunchedEffect(uiState.hintMessage) {
        uiState.hintMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            gameViewModel.dismissHint()
        }
    }

    // Mistake toast
    LaunchedEffect(uiState.showMistakeToast, uiState.mistakeMessage) {
        if (uiState.showMistakeToast && uiState.mistakeMessage != null) {
            Toast.makeText(context, uiState.mistakeMessage, Toast.LENGTH_SHORT).show()
            gameViewModel.dismissMistakeToast()
        }
    }

    // Clear animation states after one frame
    LaunchedEffect(uiState.lastPlacedCell) {
        if (uiState.lastPlacedCell != null) {
            kotlinx.coroutines.delay(350)
            gameViewModel.clearLastPlacedCell()
        }
    }
    LaunchedEffect(uiState.lastMistakeCell) {
        if (uiState.lastMistakeCell != null) {
            kotlinx.coroutines.delay(400)
            gameViewModel.clearLastMistakeCell()
        }
    }

    // Auto-pause on background
    OnLifecycleEvent { _, event ->
        if (event == Lifecycle.Event.ON_PAUSE) {
            if (uiState.game?.state == GameState.PLAYING) {
                gameViewModel.onPauseToggle()
            }
        }
    }

    // Loading spinner
    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Error handling
    uiState.error?.let {
        LaunchedEffect(it) { onNavigateBack(); gameViewModel.clearError() }
        return
    }

    val game = uiState.game ?: return

    // Completion dialog
    if (uiState.showCompletionDialog && uiState.completionTime != null) {
        GameCompletedDialog(
            difficulty = game.difficulty,
            completionTime = uiState.completionTime!!,
            hintsUsed = game.hintsUsed,
            onNewGame = { gameViewModel.dismissCompletionDialog(); gameViewModel.onNewGame() },
            onMainMenu = { gameViewModel.dismissCompletionDialog(); onNavigateBack() }
        )
    }

    // New game generating
    if (uiState.isGeneratingNewGame) {
        LoadingDialog(message = "Generating new puzzle…")
    }

    Scaffold(
        modifier = modifier.testTag("game_screen").fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${game.difficulty.displayName} Sudoku",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
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
            // Toolbar: timer, mistakes, notes toggle, action buttons
            GameToolbar(
                elapsedTime       = game.elapsedTime,
                hintsRemaining    = Constants.MAX_HINTS - game.hintsUsed,
                isPaused          = game.state == GameState.PAUSED,
                mistakes          = game.mistakes,
                maxMistakes       = game.maxMistakes,
                isNotesMode       = if (notesModeSetting) uiState.isNotesMode else false,
                onNotesModeToggle = gameViewModel::toggleNotesMode,
                onPauseClick      = gameViewModel::onPauseToggle,
                onHintClick       = gameViewModel::onHintRequested,
                onUndoClick       = gameViewModel::onUndoMove,
                onRedoClick       = gameViewModel::onRedoMove,
                onResetClick      = gameViewModel::onResetBoard,
                modifier          = Modifier.fillMaxWidth()
            )

            // Game Over inline dialog
            if (uiState.showGameOverDialog) {
                GameOverDialog(
                    difficulty  = game.difficulty,
                    mistakes    = game.mistakes,
                    maxMistakes = game.maxMistakes,
                    onTryAgain  = { gameViewModel.dismissGameOverDialog(); gameViewModel.onNewGame() },
                    onMainMenu  = { gameViewModel.dismissGameOverDialog(); onNavigateBack() }
                )
            }

            // Sudoku board — passes all highlight/animation state
            SudokuBoard(
                board            = game.board,
                notes            = uiState.notes,
                selectedCell     = uiState.selectedCell,
                highlightedCells = uiState.highlightedCells,
                sameNumberCells  = uiState.sameNumberCells,
                conflictCells    = uiState.conflictCells,
                highlightErrors  = highlightErrors,
                isNotesMode      = uiState.isNotesMode,
                lastPlacedCell   = uiState.lastPlacedCell,
                lastMistakeCell  = uiState.lastMistakeCell,
                onCellClick      = gameViewModel::onCellSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            // Number pad (hidden when game is finished)
            AnimatedVisibility(
                visible = game.state != GameState.COMPLETED && game.state != GameState.LOST,
                enter   = fadeIn() + expandVertically(),
                exit    = fadeOut() + shrinkVertically()
            ) {
                NumberPad(
                    numberCounts     = uiState.numberCounts,
                    onNumberSelected = gameViewModel::onNumberSelected,
                    onEraseSelected  = gameViewModel::onEraseSelected,
                    modifier         = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}