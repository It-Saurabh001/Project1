package com.saurabh.sudoku.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.sudoku.data.generator.SudokuGenerator
import com.saurabh.sudoku.domain.model.Difficulty
import com.saurabh.sudoku.domain.model.Game
import com.saurabh.sudoku.domain.model.GameState
import com.saurabh.sudoku.domain.repository.GameRepository
import com.saurabh.sudoku.domain.repository.StatisticsRepository
import com.saurabh.sudoku.presentation.uistate.GameUiState
import com.saurabh.sudoku.presentation.utils.Constants
import com.saurabh.sudoku.presentation.utils.DateUtils
import com.saurabh.sudoku.presentation.utils.GameUtils
import com.saurabh.sudoku.presentation.utils.deepCopy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    // --- START OF REFACTOR ---
    // Inject repositories directly
    private val gameRepository: GameRepository,
    private val statisticsRepository: StatisticsRepository, // For completion logic
    private val sudokuGenerator: SudokuGenerator      // For new game generation
    // --- END OF REFACTOR ---
) : ViewModel() {
    private val TAG = "SudokuDebug"

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var currentGame: Game? = null
    private val moveHistory = mutableListOf<Move>()
    private var moveIndex = -1

    data class Move(val row: Int, val col: Int, val oldValue: Int, val newValue: Int)

    private fun updateNumberCounts(game: Game?) {
        if (game == null) return
        val counts = (1..9).associateWith { number ->
            game.board.board.sumOf { row -> row.count { it == number } }
        }
        _uiState.update { it.copy(numberCounts = counts) }
    }

    fun loadGame(gameId: String) {
        viewModelScope.launch {
            try {
                // Logic from LoadGameUseCase
                val game = gameRepository.getGameById(gameId)
                if (game != null) {
                    val loadedGame = if (game.state == GameState.PAUSED) {
                        game.copy(state = GameState.PLAYING)
                    } else {
                        game
                    }
                    currentGame = loadedGame
                    _uiState.update {
                        it.copy(
                            game = loadedGame,
                            selectedCell = null,
                            conflictCells = emptySet(),
                            isLoading = false,
                            error = null
                        )
                    }
                    updateNumberCounts(loadedGame)
                    startTimer()
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Game not found") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load game: ${e.message}") }
            }
        }
    }

    fun onCellSelected(row: Int, col: Int) {
        val currentState = _uiState.value
        val game = currentState.game ?: return
        if (game.board.isInitialCell(row, col)){
            Log.d(TAG, "onCellSelected: Cell ($row, $col) is an initial (given) cell. Ignoring selection.")
            return
        }
        val newSelectedCell = if (currentState.selectedCell == Pair(row, col)) null else Pair(row, col)
        Log.d(TAG, "onCellSelected: New selected cell is: $newSelectedCell")
        _uiState.update {
            Log.d(TAG, "onCellSelected: New selected cell is update in UI")
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
        Log.d(TAG, "onNumberSelected: Tapped on number '$number'")
        val currentState = _uiState.value
        val selectedCell = currentState.selectedCell

        if (currentState.game == null) {
            Log.e(TAG, "onNumberSelected: EXITING because Game is null!")
            return
        }
        if (selectedCell == null) {
            Log.w(TAG, "onNumberSelected: EXITING because no cell is selected.")
            return
        }

        viewModelScope.launch {
            if (currentGame?.state != GameState.PLAYING) {
                Log.d(TAG, "onNumberSelected: Game is not playing. Resuming...")
                resumeGame()
            }
            val (row, col) = selectedCell
            makeMove(row, col, number)
        }
    }

    fun onEraseSelected() {
        Log.d(TAG, "onEraseSelected: Tapped on erase.")
        val currentState = _uiState.value
        val selectedCell = currentState.selectedCell
        if(selectedCell == null) {
            Log.w(TAG, "onEraseSelected: No cell selected to erase.")
            return
        }
        viewModelScope.launch {
            if (currentGame?.state != GameState.PLAYING) {
                Log.d(TAG, "onEraseSelected: Game is not playing. Resuming...")
                resumeGame()
            }
            makeMove(selectedCell.first, selectedCell.second, 0)
        }
    }

    private suspend fun makeMove(row: Int, col: Int, number: Int) {
        val game = currentGame ?: run {
            Log.e(TAG, "makeMove: Cannot make move, currentGame is null.")
            return
        }

        // Logic from ValidateMoveUseCase
        if (game.board.isInitialCell(row, col)) {
            Log.w(TAG, "makeMove: Cannot change initial cell.")
            return
        }

        val oldValue = game.board.getCurrentValue(row, col)
        if (oldValue == number) {
            Log.d(TAG, "makeMove: No change needed.")
            return
        }
        Log.d(TAG, "makeMove: Placing '$number' at ($row, $col)")

        if (moveIndex < moveHistory.size - 1) {
            moveHistory.subList(moveIndex + 1, moveHistory.size).clear()
        }
        moveHistory.add(Move(row, col, oldValue, number))
        moveIndex++

        val newBoardArray = game.board.board.map { it.clone() }.toTypedArray()
        newBoardArray[row][col] = number
        val updatedBoard = game.board.copy(board = newBoardArray)

        val updatedGame = game.copy(
            board = updatedBoard,
            currentTime = DateUtils.getCurrentTimestamp(),
            state = GameState.PLAYING
        )
        currentGame = updatedGame
        _uiState.value = _uiState.value.copy(game = updatedGame)

        // Logic from SaveGameUseCase
        gameRepository.updateGame(updatedGame)
        Log.d(TAG, "SaveGame: Saving game ID ${updatedGame.id} with state ${updatedGame.state}")

        // Logic from CheckGameCompletionUseCase
        checkGameCompletion(updatedGame, row, col)
    }

    private suspend fun checkGameCompletion(game: Game, row: Int, col: Int) {
        Log.d(TAG, "CheckGameCompletion: Checking game ID ${game.id}")
        val isComplete = GameUtils.isBoardComplete(game.board.board)

        val completionResult = if (isComplete && game.state != GameState.COMPLETED) {
            Log.d(TAG, "CheckGameCompletion: Game ${game.id} is complete!")
            val completedTime = DateUtils.getCurrentTimestamp()
            val gameTime = completedTime - game.startTime
            statisticsRepository.recordGameCompleted(
                difficulty = game.difficulty,
                gameTime = gameTime,
                hintsUsed = game.hintsUsed
            )
            Log.d(TAG, "CheckGameCompletion: Statistics updated for game ${game.id}.")
            val completedGame = game.copy(
                state = GameState.COMPLETED,
                completedAt = completedTime,
                currentTime = completedTime
            )
            Pair(completedGame, gameTime)
        } else {
            Pair(game, null)
        }

        _uiState.update {
            it.copy(
                game = completionResult.first,
                conflictCells = GameUtils.getCellConflicts(completionResult.first.board.board, row, col),
                showCompletionDialog = completionResult.second != null,
                completionTime = completionResult.second
            )
        }
        updateNumberCounts(completionResult.first)
        if (completionResult.second != null) {
            stopTimer()
        }
    }

    fun onUndoMove() {
        if (moveIndex >= 0) {
            val move = moveHistory[moveIndex]
            val game = currentGame ?: return
            val newBoardArray = game.board.board.map { it.clone() }.toTypedArray()
            newBoardArray[move.row][move.col] = move.oldValue
            val updatedBoard = game.board.copy(board = newBoardArray)
            moveIndex--
            val updatedGame = game.copy(
                board = updatedBoard,
                currentTime = DateUtils.getCurrentTimestamp()
            )
            currentGame = updatedGame
            viewModelScope.launch {
                gameRepository.updateGame(updatedGame)
                _uiState.update {
                    it.copy(
                        game = updatedGame,
                        conflictCells = _uiState.value.selectedCell?.let { (r, c) ->
                            GameUtils.getCellConflicts(updatedGame.board.board, r, c)
                        } ?: emptySet()
                    )
                }
                updateNumberCounts(updatedGame)
            }
        }
    }

    fun onRedoMove() {
        if (moveIndex < moveHistory.size - 1) {
            moveIndex++
            val move = moveHistory[moveIndex]
            val game = currentGame ?: return
            val newBoardArray = game.board.board.map { it.clone() }.toTypedArray()
            newBoardArray[move.row][move.col] = move.newValue
            val updatedBoard = game.board.copy(board = newBoardArray)
            val updatedGame = game.copy(
                board = updatedBoard,
                currentTime = DateUtils.getCurrentTimestamp()
            )
            currentGame = updatedGame
            viewModelScope.launch {
                gameRepository.updateGame(updatedGame)
                _uiState.update {
                    it.copy(
                        game = updatedGame,
                        conflictCells = _uiState.value.selectedCell?.let { (r, c) ->
                            GameUtils.getCellConflicts(updatedGame.board.board, r, c)
                        } ?: emptySet()
                    )
                }
                updateNumberCounts(updatedGame)
            }
        }
    }

    fun onNewGame() {
        val currentDifficulty = currentGame?.difficulty ?: Difficulty.MEDIUM
        viewModelScope.launch {_uiState.update { it.copy(isGeneratingNewGame = true) }
            try {
                // Logic from GeneratePuzzleUseCase
                Log.d(TAG, "GeneratePuzzle: Generating new puzzle with difficulty ${currentDifficulty.name}")
                val board = sudokuGenerator.generatePuzzle(currentDifficulty)
                val currentTime = DateUtils.getCurrentTimestamp()
                val newGame = Game(
                    id = UUID.randomUUID().toString(),
                    board = board,
                    initialBoard = board.deepCopy(), // <-- FIX: Provide the initial board here
                    difficulty = currentDifficulty,
                    startTime = currentTime,
                    currentTime = currentTime,
                    state = GameState.PLAYING, // Start playing immediately
                    hintsUsed = 0,
                    createdAt = currentTime
                )
                Log.d(TAG, "GeneratePuzzle: New game created with ID ${newGame.id}. Saving to repository.")
                gameRepository.saveGame(newGame)
                // --- End of UseCase logic

                currentGame = newGame
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
                updateNumberCounts(newGame)
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


    fun onHintRequested() {
        val game = currentGame ?: return
        if (game.hintsUsed >= Constants.MAX_HINTS) return

        val hint = GameUtils.getHint(game.board.board, game.board.solution)

        if (hint != null) {
            val updatedGame = game.copy(
                hintsUsed = game.hintsUsed + 1,
                currentTime = DateUtils.getCurrentTimestamp()
            )
            currentGame = updatedGame
            viewModelScope.launch {
                gameRepository.updateGame(updatedGame)
                _uiState.update {
                    it.copy(
                        game = updatedGame,
                        selectedCell = Pair(hint.row, hint.col),
                        showHint = hint,
                        hintMessage = "Try this cell!"
                    )
                }
            }
        } else {
            _uiState.update { it.copy(hintMessage = "No more hints available or board is full!") }
        }
    }
    fun onResetBoard() = viewModelScope.launch {
        // Ensure we have a game to reset
        val currentGame = _uiState.value.game ?: return@launch

        // Create a new board state by copying the initial puzzle
        val newBoard = currentGame.initialBoard.deepCopy()

        // Update the game state with the reset board
        val updatedGame = currentGame.copy(
            board = newBoard,
            state = GameState.PLAYING, // Ensure game is playable
            hintsUsed = 0 // Reset hints used
        )
        this@GameViewModel.currentGame = updatedGame

        // Clear the move history
        moveHistory.clear()
        moveIndex = -1

        // Update the UI state
        _uiState.update {
            it.copy(
                game = updatedGame,
                selectedCell = null,
                conflictCells = emptySet()
            )
        }
        updateNumberCounts(updatedGame) // Recalculate number counts

        // Persist the changes
        gameRepository.updateGame(updatedGame)
    }

    fun onPauseToggle() {
        when (currentGame?.state) {
            GameState.PLAYING -> pauseGame()
            GameState.PAUSED -> resumeGame()
            else -> {}
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
            gameRepository.updateGame(pausedGame)
            _uiState.update { it.copy(game = pausedGame) }
        }
    }

    private fun resumeGame() {
        val game = currentGame ?: return
        if (game.state == GameState.PLAYING) return
        val resumedGame = game.copy(
            state = GameState.PLAYING,
            currentTime = DateUtils.getCurrentTimestamp()
        )
        currentGame = resumedGame
        _uiState.update { it.copy(game = resumedGame) }
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (currentGame?.state == GameState.PLAYING) {
                delay(1000)
                currentGame?.let { game ->
                    val updatedGame = game.copy(currentTime = game.currentTime + 1000)
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
        _uiState.update { it.copy(showHint = null, hintMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        currentGame?.let { game ->
            if (game.state == GameState.PLAYING) {
                val pausedGame = game.copy(
                    state = GameState.PAUSED,
                    currentTime = DateUtils.getCurrentTimestamp()
                )
                viewModelScope.launch {
                    gameRepository.updateGame(pausedGame)
                }
            }
        }
    }
}
