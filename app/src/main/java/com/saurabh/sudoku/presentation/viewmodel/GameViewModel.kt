package com.saurabh.sudoku.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.sudoku.domain.model.Game
import com.saurabh.sudoku.domain.model.GameState
import com.saurabh.sudoku.domain.usecase.CheckGameCompletionUseCase
import com.saurabh.sudoku.domain.usecase.GeneratePuzzleUseCase
import com.saurabh.sudoku.domain.usecase.GetHintUseCase
import com.saurabh.sudoku.domain.usecase.LoadGameUseCase
import com.saurabh.sudoku.domain.usecase.SaveGameUseCase
import com.saurabh.sudoku.domain.usecase.ValidateMoveUseCase
import com.saurabh.sudoku.presentation.uistate.GameUiState
import com.saurabh.sudoku.utils.Constants
import com.saurabh.sudoku.utils.DateUtils
import com.saurabh.sudoku.utils.GameUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val loadGameUseCase: LoadGameUseCase,
    private val saveGameUseCase: SaveGameUseCase,
    private val validateMoveUseCase: ValidateMoveUseCase,
    private val getHintUseCase: GetHintUseCase,
    private val checkGameCompletionUseCase: CheckGameCompletionUseCase,
    private val generatePuzzleUseCase: GeneratePuzzleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var currentGame: Game? = null
    private val moveHistory = mutableListOf<Move>()
    private var moveIndex = -1

    fun loadGame(gameId: String) {
        viewModelScope.launch {
            try {
                val game = loadGameUseCase.getGameById(gameId)
                if (game != null) {
                    currentGame = game
                    _uiState.update {
                        it.copy(
                            game = game,
                            selectedCell = null,
                            conflictCells = emptySet(),
                            isLoading = false,
                            error = null
                        )
                    }
                    startTimer()
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Game not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load game: ${e.message}"
                    )
                }
            }
        }
    }

    fun onCellSelected(row: Int, col: Int) {
        val currentState = _uiState.value
        val game = currentState.game ?: return

        if (game.board.isInitialCell(row, col)) return

        val newSelectedCell = if (currentState.selectedCell == Pair(row, col)) {
            null
        } else {
            Pair(row, col)
        }

        _uiState.update {
            it.copy(
                selectedCell = newSelectedCell,
                conflictCells = if (newSelectedCell != null) {
                    GameUtils.getCellConflicts(game.board.board, newSelectedCell.first, newSelectedCell.second)
                } else {
                    emptySet()
                }
            )
        }
    }

    fun onNumberSelected(number: Int) {
        val currentState = _uiState.value
        val game = currentState.game ?: return
        val selectedCell = currentState.selectedCell ?: return

        if (game.state != GameState.PLAYING) return

        val (row, col) = selectedCell
        val validation = validateMoveUseCase(game.board, row, col, number)

        if (validation.isValid) {
            makeMove(row, col, number)
        }
    }

    fun onEraseSelected() {
        val currentState = _uiState.value
        val selectedCell = currentState.selectedCell ?: return

        makeMove(selectedCell.first, selectedCell.second, 0)
    }

    private fun makeMove(row: Int, col: Int, number: Int) {
        val game = currentGame ?: return
        val oldValue = game.board.getCurrentValue(row, col)

        // Save move to history
        if (moveIndex < moveHistory.size - 1) {
            // Remove moves after current index
            moveHistory.removeAll { moveHistory.indexOf(it) > moveIndex }
        }
        moveHistory.add(Move(row, col, oldValue, number))
        moveIndex = moveHistory.size - 1

        // Update board
        game.board.board[row][col] = number

        // Update game
        val updatedGame = game.copy(currentTime = DateUtils.getCurrentTimestamp())
        currentGame = updatedGame

        viewModelScope.launch {
            // Save game
            saveGameUseCase(updatedGame)

            // Check completion
            val completionResult = checkGameCompletionUseCase(updatedGame)

            _uiState.update {
                it.copy(
                    game = completionResult.updatedGame,
                    conflictCells = GameUtils.getCellConflicts(
                        completionResult.updatedGame.board.board,
                        row,
                        col
                    ),
                    showCompletionDialog = completionResult.isCompleted,
                    completionTime = completionResult.completionTime
                )
            }

            if (completionResult.isCompleted) {
                stopTimer()
                currentGame = completionResult.updatedGame
            }
        }
    }

    fun onUndoMove() {
        if (moveIndex >= 0) {
            val move = moveHistory[moveIndex]
            val game = currentGame ?: return

            game.board.board[move.row][move.col] = move.oldValue
            moveIndex--

            val updatedGame = game.copy(currentTime = DateUtils.getCurrentTimestamp())
            currentGame = updatedGame

            viewModelScope.launch {
                saveGameUseCase(updatedGame)
                _uiState.update {
                    it.copy(
                        game = updatedGame,
                        conflictCells = _uiState.value.selectedCell?.let { (row, col) ->
                            GameUtils.getCellConflicts(updatedGame.board.board, row, col)
                        } ?: emptySet()
                    )
                }
            }
        }
    }

    fun onRedoMove() {
        if (moveIndex < moveHistory.size - 1) {
            moveIndex++
            val move = moveHistory[moveIndex]
            val game = currentGame ?: return

            game.board.board[move.row][move.col] = move.newValue

            val updatedGame = game.copy(currentTime = DateUtils.getCurrentTimestamp())
            currentGame = updatedGame

            viewModelScope.launch {
                saveGameUseCase(updatedGame)
                _uiState.update {
                    it.copy(
                        game = updatedGame,
                        conflictCells = _uiState.value.selectedCell?.let { (row, col) ->
                            GameUtils.getCellConflicts(updatedGame.board.board, row, col)
                        } ?: emptySet()
                    )
                }
            }
        }
    }

    fun onHintRequested() {
        val game = currentGame ?: return
        if (game.hintsUsed >= Constants.MAX_HINTS) return

        val hintResult = getHintUseCase(game)
        if (hintResult.hint != null) {
            val hint = hintResult.hint
            val updatedGame = game.copy(
                hintsUsed = game.hintsUsed + 1,
                currentTime = DateUtils.getCurrentTimestamp()
            )

            currentGame = updatedGame

            viewModelScope.launch {
                saveGameUseCase(updatedGame)
                _uiState.update {
                    it.copy(
                        game = updatedGame,
                        selectedCell = Pair(hint.row, hint.col),
                        showHint = hint,
                        hintMessage = hintResult.message
                    )
                }
            }
        }
    }

    fun onPauseToggle() {
        val game = currentGame ?: return

        when (game.state) {
            GameState.PLAYING -> {
                pauseGame()
            }
            GameState.PAUSED -> {
                resumeGame()
            }
            else -> return
        }
    }

    private fun pauseGame() {
        val game = currentGame ?: return
        stopTimer()

        val pausedGame = game.copy(
            state = GameState.PAUSED,
            currentTime = DateUtils.getCurrentTimestamp()
        )
        currentGame = pausedGame

        viewModelScope.launch {
            saveGameUseCase(pausedGame)
            _uiState.update { it.copy(game = pausedGame) }
        }
    }

    private fun resumeGame() {
        val game = currentGame ?: return

        val resumedGame = game.copy(
            state = GameState.PLAYING,
            currentTime = DateUtils.getCurrentTimestamp()
        )
        currentGame = resumedGame

        viewModelScope.launch {
            saveGameUseCase(resumedGame)
            _uiState.update { it.copy(game = resumedGame) }
        }

        startTimer()
    }

    fun onNewGame() {
        val game = currentGame ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingNewGame = true) }

            try {
                val newGame = generatePuzzleUseCase(game.difficulty)
                currentGame = newGame

                // Clear history
                moveHistory.clear()
                moveIndex = -1

                _uiState.update {
                    it.copy(
                        game = newGame,
                        selectedCell = null,
                        conflictCells = emptySet(),
                        showCompletionDialog = false,
                        isGeneratingNewGame = false,
                        showHint = null,
                        hintMessage = null
                    )
                }

                startTimer()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGeneratingNewGame = false,
                        error = "Failed to generate new game: ${e.message}"
                    )
                }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (currentGame?.state == GameState.PLAYING) {
                delay(1000)
                currentGame?.let { game ->
                    val updatedGame = game.copy(currentTime = DateUtils.getCurrentTimestamp())
                    currentGame = updatedGame
                    _uiState.update { it.copy(game = updatedGame) }
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun dismissCompletionDialog() {
        _uiState.update { it.copy(showCompletionDialog = false) }
    }

    fun dismissHint() {
        _uiState.update {
            it.copy(
                showHint = null,
                hintMessage = null
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()

        // Auto-save current game
        currentGame?.let { game ->
            if (game.state == GameState.PLAYING) {
                val pausedGame = game.copy(
                    state = GameState.PAUSED,
                    currentTime = DateUtils.getCurrentTimestamp()
                )
                viewModelScope.launch {
                    saveGameUseCase(pausedGame)
                }
            }
        }
    }

    private data class Move(
        val row: Int,
        val col: Int,
        val oldValue: Int,
        val newValue: Int
    )
}