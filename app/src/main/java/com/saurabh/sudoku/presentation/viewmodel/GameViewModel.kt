package com.saurabh.sudoku.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.sudoku.data.generator.SudokuGenerator
import com.saurabh.sudoku.domain.model.*
import com.saurabh.sudoku.domain.repository.GameRepository
import com.saurabh.sudoku.domain.repository.StatisticsRepository
import com.saurabh.sudoku.domain.usecase.ValidateMoveUseCase
import com.saurabh.sudoku.presentation.uistate.GameUiState
import com.saurabh.sudoku.presentation.utils.Constants
import com.saurabh.sudoku.presentation.utils.DateUtils
import com.saurabh.sudoku.presentation.utils.GameUtils
import com.saurabh.sudoku.presentation.utils.SoundManager
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
    private val gameRepository: GameRepository,
    private val statisticsRepository: StatisticsRepository,
    private val sudokuGenerator: SudokuGenerator,
    private val validateMoveUseCase: ValidateMoveUseCase,
    private val soundManager: SoundManager
) : ViewModel() {

    private val TAG = "GameViewModel"
    private val DEBUG = android.os.Build.TYPE != "user"

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var currentGame: Game? = null

    /**
     * Extended Move stores the FULL notes snapshot before and after.
     * This ensures undo/redo correctly restores the notes state.
     */
    private data class Move(
        val row: Int,
        val col: Int,
        val oldValue: Int,
        val newValue: Int,
        val notesBefore: Notes,    // snapshot of notes BEFORE this move
        val notesAfter: Notes      // snapshot of notes AFTER this move
    )

    private val moveHistory = mutableListOf<Move>()
    private var moveIndex = -1

    // -------------------------------------------------------------------------
    // Highlight helpers
    // -------------------------------------------------------------------------

    private fun computeHighlights(board: Array<IntArray>, row: Int, col: Int): Pair<Set<Pair<Int, Int>>, Set<Pair<Int, Int>>> {
        val related = GameUtils.getRelatedCells(row, col)
        val sameNum = GameUtils.getSameNumberCells(board, row, col)
        return related to sameNum
    }

    private fun updateHighlights(board: Array<IntArray>, selectedCell: Pair<Int, Int>?) {
        if (selectedCell == null) {
            _uiState.update { it.copy(highlightedCells = emptySet(), sameNumberCells = emptySet()) }
            return
        }
        val (related, sameNum) = computeHighlights(board, selectedCell.first, selectedCell.second)
        _uiState.update { it.copy(highlightedCells = related, sameNumberCells = sameNum) }
    }

    // -------------------------------------------------------------------------
    // Number counts
    // -------------------------------------------------------------------------

    private fun computeNumberCounts(board: Array<IntArray>): Map<Int, Int> {
        return (1..9).associateWith { number ->
            val used = board.sumOf { row -> row.count { it == number } }
            maxOf(0, 9 - used)
        }
    }

    private fun updateNumberCounts(board: Array<IntArray>) {
        _uiState.update { it.copy(numberCounts = computeNumberCounts(board)) }
    }

    // -------------------------------------------------------------------------
    // Conflict detection (uses improved dual-strategy detector)
    // -------------------------------------------------------------------------

    private fun recomputeConflicts(board: Array<IntArray>, solution: Array<IntArray>): Set<Pair<Int, Int>> {
        return GameUtils.computeAllConflicts(board, solution)
    }

    // -------------------------------------------------------------------------
    // Load game
    // -------------------------------------------------------------------------

    fun loadGame(gameId: String) {
        dismissHint()
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val game = gameRepository.getGameById(gameId)
                if (game != null) {
                    val conflicts = recomputeConflicts(game.board.board, game.board.solution)
                    // Auto-resume paused games
                    val loadedGame = if (game.state == GameState.PAUSED) {
                        game.copy(state = GameState.PLAYING)
                    } else game
                    currentGame = loadedGame
                    moveHistory.clear()
                    moveIndex = -1
                    _uiState.update {
                        it.copy(
                            game = loadedGame,
                            selectedCell = null,
                            conflictCells = conflicts,
                            highlightedCells = emptySet(),
                            sameNumberCells = emptySet(),
                            isLoading = false,
                            error = null,
                            isNotesMode = false,
                            notes = loadedGame.notes,
                            numberCounts = computeNumberCounts(loadedGame.board.board),
                            mistakes = loadedGame.mistakes,
                            maxMistakes = loadedGame.maxMistakes
                        )
                    }
                    if (loadedGame.state == GameState.PLAYING) startTimer()
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Game not found") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load: ${e.message}") }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Cell selection
    // -------------------------------------------------------------------------

    fun onCellSelected(row: Int, col: Int) {
        val state = _uiState.value
        val game = state.game ?: return
        soundManager.playClick()

        val newSelected = if (state.selectedCell == (row to col)) null else (row to col)
        if (newSelected == null) {
            _uiState.update {
                it.copy(selectedCell = null, highlightedCells = emptySet(), sameNumberCells = emptySet())
            }
        } else {
            val (related, sameNum) = computeHighlights(game.board.board, row, col)
            _uiState.update {
                it.copy(
                    selectedCell = newSelected,
                    highlightedCells = related,
                    sameNumberCells = sameNum
                )
            }
        }
    }

    // -------------------------------------------------------------------------
    // Number placement
    // -------------------------------------------------------------------------

    fun onNumberSelected(number: Int) {
        val state = _uiState.value
        val selected = state.selectedCell ?: run {
            showToast("Select a cell first")
            return
        }
        val game = state.game ?: return
        val (row, col) = selected

        if (game.board.isInitialCell(row, col)) {
            showToast("Cannot modify a clue cell")
            return
        }

        val currentValue = game.board.getCurrentValue(row, col)

        // ---- NOTES MODE ----
        if (state.isNotesMode && currentValue == 0) {
            val notesBefore = game.notes.copy()
            val newNotes = game.notes.copy()
            if (newNotes.getNotes(row, col).contains(number)) {
                newNotes.removeNote(row, col, number)
            } else {
                newNotes.addNote(row, col, number)
            }
            soundManager.playNote()

            // Notes moves are NOT added to undo/redo history (keeps history clean for number moves)
            val updatedGame = game.copy(notes = newNotes, currentTime = DateUtils.getCurrentTimestamp())
            currentGame = updatedGame
            viewModelScope.launch {
                gameRepository.updateGame(updatedGame)
                _uiState.update { it.copy(game = updatedGame, notes = newNotes) }
            }
            return
        }

        // ---- NUMBER MODE ----
        // Ensure game is running
        if (currentGame?.state != GameState.PLAYING) resumeGame()
        viewModelScope.launch { makeMove(row, col, number, game.notes.copy()) }
    }

    fun onEraseSelected() {
        val state = _uiState.value
        val selected = state.selectedCell ?: return
        val (row, col) = selected
        val game = state.game ?: return

        if (game.board.isInitialCell(row, col)) return

        val currentValue = game.board.getCurrentValue(row, col)
        val hasNotes = game.notes.getNotes(row, col).isNotEmpty()

        if (currentValue == 0 && !hasNotes) return // Nothing to erase

        soundManager.playErase()

        // Erase notes first (if cell is empty but has notes)
        if (currentValue == 0 && hasNotes) {
            val newNotes = game.notes.copy()
            newNotes.clearNotes(row, col)
            val updatedGame = game.copy(notes = newNotes, currentTime = DateUtils.getCurrentTimestamp())
            currentGame = updatedGame
            viewModelScope.launch {
                gameRepository.updateGame(updatedGame)
                _uiState.update { it.copy(game = updatedGame, notes = newNotes) }
            }
            return
        }

        // Erase the number — push to undo history
        if (currentGame?.state != GameState.PLAYING) resumeGame()
        viewModelScope.launch { makeMove(row, col, 0, game.notes.copy()) }
    }

    // -------------------------------------------------------------------------
    // Core move engine
    // -------------------------------------------------------------------------

    private suspend fun makeMove(row: Int, col: Int, number: Int, notesBefore: Notes) {
        val game = currentGame ?: return

        if (game.state == GameState.LOST || game.state == GameState.COMPLETED) return
        if (game.board.isInitialCell(row, col)) return

        val oldValue = game.board.getCurrentValue(row, col)
        if (oldValue == number) return // No change

        // Validate
        val validation = validateMoveUseCase(
            board = game.board,
            row = row, col = col,
            number = number,
            currentMistakes = game.mistakes,
            maxMistakes = game.maxMistakes
        )

        var updatedGame = game

        // Apply mistake count change
        if (validation.newMistakes != game.mistakes) {
            updatedGame = game.copy(mistakes = validation.newMistakes)
            currentGame = updatedGame
            soundManager.playMistake()
            _uiState.update {
                it.copy(
                    game = updatedGame,
                    mistakeMessage = validation.message,
                    showMistakeToast = true,
                    lastMistakeCell = row to col
                )
            }
        }

        // Game over
        if (validation.isGameOver) {
            val lostGame = updatedGame.copy(
                state = GameState.LOST,
                currentTime = DateUtils.getCurrentTimestamp()
            )
            currentGame = lostGame
            gameRepository.updateGame(lostGame)
            _uiState.update { it.copy(game = lostGame, showGameOverDialog = true) }
            stopTimer()
            return
        }

        // Place number on board
        if (validation.canPlaceNumber) {
            val newBoard = updatedGame.board.board.map { it.clone() }.toTypedArray()
            newBoard[row][col] = number

            // Auto-remove notes in same row/col/box when placing a correct number
            val notesAfter = notesBefore.copy()
            if (number != 0 && validation.isValid) {
                notesAfter.autoRemoveNotes(row, col, number, newBoard)
            } else if (number != 0) {
                notesAfter.clearNotes(row, col) // Also clear notes at this cell
            }

            // Push to undo/redo history
            if (moveIndex < moveHistory.size - 1) {
                moveHistory.subList(moveIndex + 1, moveHistory.size).clear()
            }
            moveHistory.add(Move(row, col, oldValue, number, notesBefore, notesAfter))
            moveIndex++

            val updatedBoard = updatedGame.board.copy(board = newBoard)
            val finalGame = updatedGame.copy(
                board = updatedBoard,
                currentTime = DateUtils.getCurrentTimestamp(),
                state = GameState.PLAYING,
                notes = notesAfter
            )
            currentGame = finalGame
            gameRepository.updateGame(finalGame)

            val conflicts = recomputeConflicts(newBoard, finalGame.board.solution)
            val (related, sameNum) = if (_uiState.value.selectedCell != null) {
                computeHighlights(newBoard, row, col)
            } else emptySet<Pair<Int, Int>>() to emptySet()

            _uiState.update {
                it.copy(
                    game = finalGame,
                    notes = notesAfter,
                    conflictCells = conflicts,
                    highlightedCells = related,
                    sameNumberCells = sameNum,
                    lastPlacedCell = if (validation.isValid && number != 0) row to col else null,
                    numberCounts = computeNumberCounts(newBoard)
                )
            }

            if (validation.isValid && number != 0) {
                soundManager.playCorrect()
                checkGameCompletion(finalGame)
            }
        }
    }

    // -------------------------------------------------------------------------
    // Game completion
    // -------------------------------------------------------------------------

    private suspend fun checkGameCompletion(game: Game) {
        val isComplete = GameUtils.isBoardComplete(game.board.board)
        Log.d(TAG, "checkGameCompletion: isBoardComplete=$isComplete, state=${game.state}, id=${game.id.take(8)}")

        if (!isComplete) return
        if (game.state == GameState.COMPLETED) {
            Log.w(TAG, "checkGameCompletion: already COMPLETED — skipping duplicate call")
            return
        }

        // elapsedTime = currentTime - startTime (cumulative timer, pauses excluded)
        val gameTime = game.elapsedTime
        Log.d(TAG, "🏁 Game COMPLETED! difficulty=${game.difficulty}, elapsedTime=${gameTime}ms, hints=${game.hintsUsed}")

        Log.d(TAG, "→ Calling statisticsRepository.recordGameCompleted()")
        statisticsRepository.recordGameCompleted(
            difficulty = game.difficulty,
            gameTime   = gameTime,
            hintsUsed  = game.hintsUsed
        )
        Log.d(TAG, "← statisticsRepository.recordGameCompleted() returned")

        val completedAt = DateUtils.getCurrentTimestamp()
        val completedGame = game.copy(
            state       = GameState.COMPLETED,
            completedAt = completedAt,
            currentTime = game.currentTime  // keep the timer value; don't overwrite with wall clock
        )
        currentGame = completedGame

        Log.d(TAG, "→ Persisting COMPLETED state to Room for game ${game.id.take(8)}")
        gameRepository.updateGame(completedGame)
        Log.d(TAG, "← Room update complete — game is now COMPLETED in DB")

        soundManager.playComplete()
        _uiState.update {
            it.copy(
                game = completedGame,
                conflictCells    = emptySet(),
                highlightedCells = emptySet(),
                sameNumberCells  = emptySet(),
                showCompletionDialog = true,
                completionTime   = gameTime,
                numberCounts     = computeNumberCounts(completedGame.board.board)
            )
        }
        stopTimer()
        Log.d(TAG, "✅ checkGameCompletion done — dialog shown, timer stopped")
    }


    // -------------------------------------------------------------------------
    // Undo / Redo (now with full notes restoration)
    // -------------------------------------------------------------------------

    fun onUndoMove() {
        if (moveIndex < 0) {
            showToast("Nothing to undo")
            return
        }
        val move = moveHistory[moveIndex]
        moveIndex--
        applyBoardChange(move.row, move.col, move.oldValue, move.notesBefore)
        soundManager.playClick()
    }

    fun onRedoMove() {
        if (moveIndex >= moveHistory.size - 1) {
            showToast("Nothing to redo")
            return
        }
        moveIndex++
        val move = moveHistory[moveIndex]
        applyBoardChange(move.row, move.col, move.newValue, move.notesAfter)
        soundManager.playClick()
    }

    private fun applyBoardChange(row: Int, col: Int, value: Int, notes: Notes) {
        val game = currentGame ?: return
        val newBoard = game.board.board.map { it.clone() }.toTypedArray()
        newBoard[row][col] = value
        val updatedBoard = game.board.copy(board = newBoard)
        val updatedGame = game.copy(
            board = updatedBoard,
            notes = notes,
            currentTime = DateUtils.getCurrentTimestamp()
        )
        currentGame = updatedGame
        val conflicts = recomputeConflicts(newBoard, updatedGame.board.solution)
        val sel = _uiState.value.selectedCell
        val (related, sameNum) = if (sel != null) computeHighlights(newBoard, sel.first, sel.second)
                                  else emptySet<Pair<Int, Int>>() to emptySet()
        viewModelScope.launch {
            gameRepository.updateGame(updatedGame)
            _uiState.update {
                it.copy(
                    game = updatedGame,
                    notes = notes,
                    conflictCells = conflicts,
                    highlightedCells = related,
                    sameNumberCells = sameNum,
                    numberCounts = computeNumberCounts(newBoard)
                )
            }
        }
    }

    // -------------------------------------------------------------------------
    // New Game
    // -------------------------------------------------------------------------

    fun onNewGame() {
        dismissHint()
        val currentDifficulty = currentGame?.difficulty ?: Difficulty.MEDIUM
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingNewGame = true) }
            try {
                val board = sudokuGenerator.generatePuzzle(currentDifficulty)
                val currentTime = DateUtils.getCurrentTimestamp()
                val maxMistakes = Constants.getMaxMistakes(currentDifficulty.name)
                val newGame = Game(
                    id = UUID.randomUUID().toString(),
                    board = board,
                    initialBoard = board.deepCopy(),
                    difficulty = currentDifficulty,
                    startTime = currentTime,
                    currentTime = currentTime,
                    state = GameState.PLAYING,
                    hintsUsed = 0,
                    createdAt = currentTime,
                    completedAt = null,
                    mistakes = 0,
                    maxMistakes = maxMistakes,
                    notes = Notes()
                )
                gameRepository.saveGame(newGame)
                currentGame = newGame
                moveHistory.clear()
                moveIndex = -1
                _uiState.update {
                    it.copy(
                        game = newGame,
                        selectedCell = null,
                        conflictCells = emptySet(),
                        highlightedCells = emptySet(),
                        sameNumberCells = emptySet(),
                        showCompletionDialog = false,
                        showGameOverDialog = false,
                        isGeneratingNewGame = false,
                        showHint = null,
                        hintMessage = null,
                        isNotesMode = false,
                        notes = Notes(),
                        mistakes = 0,
                        maxMistakes = maxMistakes,
                        numberCounts = computeNumberCounts(board.board),
                        lastPlacedCell = null,
                        lastMistakeCell = null
                    )
                }
                startTimer()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isGeneratingNewGame = false, error = "Failed to generate puzzle: ${e.message}")
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Hints
    // -------------------------------------------------------------------------

    fun onHintRequested() {
        val game = currentGame ?: return
        if (game.hintsUsed >= Constants.MAX_HINTS) {
            showToast("No more hints available (${Constants.MAX_HINTS} used)")
            return
        }
        val selected = _uiState.value.selectedCell
        if (selected == null) {
            showToast("Tap an empty cell to get a hint")
            return
        }
        val (r, c) = selected
        if (game.board.getCurrentValue(r, c) != 0) {
            showToast("Hint only works on empty cells")
            return
        }
        val hint = GameUtils.getHintForCell(game.board.board, game.board.solution, r, c) ?: run {
            showToast("No hint available for this cell")
            return
        }
        viewModelScope.launch { applyHintAt(game, r, c, hint.value) }
    }

    private suspend fun applyHintAt(game: Game, r: Int, c: Int, value: Int) {
        val clearedNotes = game.notes.copy()
        clearedNotes.clearNotes(r, c)

        val newBoard = game.board.board.map { it.clone() }.toTypedArray()
        newBoard[r][c] = value
        clearedNotes.autoRemoveNotes(r, c, value, newBoard)

        // Hints also push to undo history
        val notesBefore = game.notes.copy()
        if (moveIndex < moveHistory.size - 1) {
            moveHistory.subList(moveIndex + 1, moveHistory.size).clear()
        }
        moveHistory.add(Move(r, c, 0, value, notesBefore, clearedNotes))
        moveIndex++

        val updatedBoard = game.board.copy(board = newBoard)
        val updatedGame = game.copy(
            board = updatedBoard,
            hintsUsed = game.hintsUsed + 1,
            currentTime = DateUtils.getCurrentTimestamp(),
            notes = clearedNotes
        )
        currentGame = updatedGame
        gameRepository.updateGame(updatedGame)

        val conflicts = recomputeConflicts(newBoard, updatedBoard.solution)
        val (related, sameNum) = computeHighlights(newBoard, r, c)
        val hintsLeft = Constants.MAX_HINTS - updatedGame.hintsUsed

        _uiState.update {
            it.copy(
                game = updatedGame,
                selectedCell = r to c,
                highlightedCells = related,
                sameNumberCells = sameNum,
                showHint = GameUtils.Hint(r, c, value),
                hintMessage = if (hintsLeft > 0) "Hint applied ($hintsLeft remaining)" else "Last hint used!",
                conflictCells = conflicts,
                notes = clearedNotes,
                numberCounts = computeNumberCounts(newBoard),
                lastPlacedCell = r to c
            )
        }
        soundManager.playCorrect()
        checkGameCompletion(updatedGame)
    }

    // -------------------------------------------------------------------------
    // Reset board
    // -------------------------------------------------------------------------

    fun onResetBoard() = viewModelScope.launch {
        val game = _uiState.value.game ?: return@launch
        val freshBoard = game.initialBoard.deepCopy()
        val clearedNotes = Notes()
        val resetGame = game.copy(
            board = freshBoard,
            state = GameState.PLAYING,
            hintsUsed = 0,
            mistakes = 0,
            notes = clearedNotes,
            currentTime = DateUtils.getCurrentTimestamp()
        )
        currentGame = resetGame
        moveHistory.clear()
        moveIndex = -1
        gameRepository.updateGame(resetGame)
        soundManager.playClick()
        _uiState.update {
            it.copy(
                game = resetGame,
                selectedCell = null,
                conflictCells = emptySet(),
                highlightedCells = emptySet(),
                sameNumberCells = emptySet(),
                notes = clearedNotes,
                mistakes = 0,
                numberCounts = computeNumberCounts(freshBoard.board),  // ← fixes the reset count bug
                lastPlacedCell = null,
                lastMistakeCell = null,
                isNotesMode = false
            )
        }
        if (resetGame.state == GameState.PLAYING) startTimer()
    }

    // -------------------------------------------------------------------------
    // Pause / Resume
    // -------------------------------------------------------------------------

    fun onPauseToggle() {
        when (currentGame?.state) {
            GameState.PLAYING -> pauseGame()
            GameState.PAUSED  -> resumeGame()
            else -> {}
        }
    }

    private fun pauseGame() {
        val game = currentGame ?: return
        stopTimer()
        val paused = game.copy(state = GameState.PAUSED, currentTime = DateUtils.getCurrentTimestamp())
        currentGame = paused
        viewModelScope.launch {
            gameRepository.updateGame(paused)
            _uiState.update { it.copy(game = paused) }
        }
    }

    private fun resumeGame() {
        val game = currentGame ?: return
        if (game.state == GameState.PLAYING) return
        val resumed = game.copy(state = GameState.PLAYING)
        currentGame = resumed
        _uiState.update { it.copy(game = resumed) }
        startTimer()
    }

    // -------------------------------------------------------------------------
    // Timer
    // -------------------------------------------------------------------------

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (currentGame?.state == GameState.PLAYING) {
                delay(1000L)
                currentGame?.let { g ->
                    val updated = g.copy(currentTime = g.currentTime + 1000L)
                    currentGame = updated
                    _uiState.update { it.copy(game = updated) }
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    // -------------------------------------------------------------------------
    // Notes mode toggle
    // -------------------------------------------------------------------------

    fun toggleNotesMode() {
        val newMode = !_uiState.value.isNotesMode
        soundManager.playClick()
        _uiState.update {
            it.copy(
                isNotesMode = newMode,
                hintMessage = if (newMode) "✏️ Notes mode ON" else "🔢 Number mode ON"
            )
        }
    }

    // -------------------------------------------------------------------------
    // UI helpers
    // -------------------------------------------------------------------------

    private fun showToast(message: String) {
        _uiState.update { it.copy(hintMessage = message) }
    }

    fun updateSoundEnabled(enabled: Boolean) {
        soundManager.isSoundEnabled = enabled
    }

    fun dismissCompletionDialog() = _uiState.update { it.copy(showCompletionDialog = false) }
    fun dismissHint()            = _uiState.update { it.copy(showHint = null, hintMessage = null) }
    fun clearError()             = _uiState.update { it.copy(error = null) }
    fun dismissGameOverDialog()  = _uiState.update { it.copy(showGameOverDialog = false) }
    fun dismissMistakeToast()    = _uiState.update { it.copy(showMistakeToast = false, mistakeMessage = null) }
    fun clearLastPlacedCell()    = _uiState.update { it.copy(lastPlacedCell = null) }
    fun clearLastMistakeCell()   = _uiState.update { it.copy(lastMistakeCell = null) }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        currentGame?.let { game ->
            if (game.state == GameState.PLAYING) {
                viewModelScope.launch {
                    gameRepository.updateGame(
                        game.copy(state = GameState.PAUSED, currentTime = DateUtils.getCurrentTimestamp())
                    )
                }
            }
        }
    }
}