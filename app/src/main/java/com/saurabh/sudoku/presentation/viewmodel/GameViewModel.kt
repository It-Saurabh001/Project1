package com.saurabh.sudoku.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.sudoku.data.generator.SudokuGenerator
import com.saurabh.sudoku.di.ApplicationScope
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val statisticsRepository: StatisticsRepository,
    private val sudokuGenerator: SudokuGenerator,
    private val validateMoveUseCase: ValidateMoveUseCase,
    private val soundManager: SoundManager,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {

    /**
     * Every action that changes the game (moves, notes, hints, undo/redo, reset)
     * runs inside this mutex and always reads [currentGame] INSIDE the lock.
     * Never capture a Game before the lock - it may be stale by the time the lock is acquired.
     */
    private val moveMutex = Mutex()

    private val TAG = "GameViewModel"

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var loadJob: Job? = null
    private var currentGame: Game? = null

    private data class Move(
        val row: Int,
        val col: Int,
        val oldValue: Int,
        val newValue: Int,
        val notesBefore: Notes,
        val notesAfter: Notes
    )

    private val moveHistory = mutableListOf<Move>()
    private var moveIndex = -1

    private fun Game.isFinished() = state == GameState.LOST || state == GameState.COMPLETED

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun computeHighlights(
        board: Array<IntArray>, row: Int, col: Int
    ): Pair<Set<Pair<Int, Int>>, Set<Pair<Int, Int>>> {
        val related = GameUtils.getRelatedCells(row, col)
        val sameNum = GameUtils.getSameNumberCells(board, row, col)
        return related to sameNum
    }

    private fun computeNumberCounts(board: Array<IntArray>): Map<Int, Int> =
        (1..9).associateWith { number ->
            val used = board.sumOf { row -> row.count { it == number } }
            maxOf(0, 9 - used)
        }

    private fun recomputeConflicts(board: Array<IntArray>, solution: Array<IntArray>): Set<Pair<Int, Int>> =
        GameUtils.computeAllConflicts(board, solution)

    /** Saves whatever the latest game is, in order with other writes. Safe to call from anywhere. */
    private fun persistLatest() {
        viewModelScope.launch {
            moveMutex.withLock { currentGame?.let { gameRepository.updateGame(it) } }
        }
    }

    // -------------------------------------------------------------------------
    // Load game
    // -------------------------------------------------------------------------

    fun loadGame(gameId: String) {
        Log.d(TAG, "loadGame() gameId: $gameId")
        if (currentGame?.id == gameId) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }
        dismissHint()
        _uiState.update { it.copy(isLoading = true) }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                val game = gameRepository.getGameById(gameId)
                if (game != null) {
                    val conflicts = recomputeConflicts(game.board.board, game.board.solution)
                    val loadedGame = if (game.state == GameState.PAUSED || game.state == GameState.PLAYING) {
                        val now = DateUtils.getCurrentTimestamp()
                        val elapsed = game.elapsedTime
                        game.copy(
                            state = GameState.PLAYING,
                            startTime = now - elapsed,
                            currentTime = now
                        )
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
                    Log.e(TAG, "loadGame() gameId $gameId not found")
                    _uiState.update { it.copy(isLoading = false, error = "Game not found") }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "loadGame() exception: ${e.message}", e)
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

        if (state.selectedCell == (row to col)) {
            _uiState.update {
                it.copy(selectedCell = null, highlightedCells = emptySet(), sameNumberCells = emptySet())
            }
        } else {
            val (related, sameNum) = computeHighlights(game.board.board, row, col)
            _uiState.update {
                it.copy(selectedCell = row to col, highlightedCells = related, sameNumberCells = sameNum)
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
        if (game.isFinished()) return
        val (row, col) = selected

        if (game.board.isInitialCell(row, col)) {
            showToast("Cannot modify a clue cell")
            return
        }

        // ---- NOTES MODE ----
        if (state.isNotesMode && game.board.getCurrentValue(row, col) == 0) {
            soundManager.playNote()
            viewModelScope.launch { moveMutex.withLock { toggleNote(row, col, number) } }
            return
        }

        // ---- NUMBER MODE ----
        viewModelScope.launch {
            moveMutex.withLock {
                if (currentGame?.state == GameState.PAUSED) resumeGame()
                makeMove(row, col, number)
            }
        }
    }

    fun onEraseSelected() {
        val (row, col) = _uiState.value.selectedCell ?: return
        viewModelScope.launch {
            moveMutex.withLock {
                val game = currentGame ?: return@withLock
                if (game.isFinished()) return@withLock
                if (game.board.isInitialCell(row, col)) return@withLock

                val currentValue = game.board.getCurrentValue(row, col)
                val hasNotes = game.notes.getNotes(row, col).isNotEmpty()
                if (currentValue == 0 && !hasNotes) return@withLock

                soundManager.playErase()

                if (currentValue == 0) {
                    // erase notes only
                    val newNotes = game.notes.deepCopy()
                    newNotes.clearNotes(row, col)
                    applyNotes(game, newNotes)
                } else {
                    if (game.state == GameState.PAUSED) resumeGame()
                    makeMove(row, col, 0)
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Notes (always called inside moveMutex)
    // -------------------------------------------------------------------------

    private suspend fun toggleNote(row: Int, col: Int, number: Int) {
        val game = currentGame ?: return
        if (game.isFinished()) return
        if (game.board.isInitialCell(row, col)) return
        if (game.board.getCurrentValue(row, col) != 0) return

        val newNotes = game.notes.deepCopy()
        if (newNotes.getNotes(row, col).contains(number)) {
            newNotes.removeNote(row, col, number)
        } else {
            newNotes.addNote(row, col, number)
        }
        applyNotes(game, newNotes)
    }

    private suspend fun applyNotes(game: Game, newNotes: Notes) {
        val updated = game.copy(notes = newNotes)
        currentGame = updated
        _uiState.update { it.copy(game = updated, notes = newNotes) }   // UI first, no suspension before this
        gameRepository.updateGame(updated)
    }

    // -------------------------------------------------------------------------
    // Core move engine (always called inside moveMutex)
    //
    // Rule: compute from currentGame and publish (currentGame + UI) WITHOUT suspending,
    // and only then persist. That way a timer tick can never be lost or overwritten.
    // -------------------------------------------------------------------------

    private suspend fun makeMove(row: Int, col: Int, number: Int) {
        val game = currentGame ?: return
        if (game.state != GameState.PLAYING) {
            Log.w(TAG, "makeMove() ignored: state=${game.state}")
            return
        }
        if (game.board.isInitialCell(row, col)) return

        val oldValue = game.board.getCurrentValue(row, col)
        if (oldValue == number) return

        val notesBefore = game.notes.deepCopy()

        val validation = validateMoveUseCase(
            board = game.board,
            row = row, col = col,
            number = number,
            currentMistakes = game.mistakes,
            maxMistakes = game.maxMistakes
        )

        var updatedGame = game

        // Mistake count changed
        if (validation.newMistakes != game.mistakes) {
            updatedGame = game.copy(mistakes = validation.newMistakes)
            currentGame = updatedGame
            soundManager.playMistake()
            _uiState.update {
                it.copy(
                    game = updatedGame,
                    mistakes = validation.newMistakes,           // keep ui mistakes in sync
                    mistakeMessage = validation.message,
                    showMistakeToast = true,
                    lastMistakeCell = row to col
                )
            }
        }

        // Game over
        if (validation.isGameOver) {
            val lostGame = updatedGame.copy(state = GameState.LOST)
            currentGame = lostGame
            stopTimer()
            _uiState.update { it.copy(game = lostGame, showGameOverDialog = true) }
            gameRepository.updateGame(lostGame)
            statisticsRepository.recordGameLost(lostGame.elapsedTime, lostGame.hintsUsed)
            return
        }

        // Rejected move: still persist the new mistake count
        if (!validation.canPlaceNumber) {
            if (updatedGame !== game) gameRepository.updateGame(updatedGame)
            return
        }

        // Place number
        val newBoard = updatedGame.board.board.map { it.clone() }.toTypedArray()
        newBoard[row][col] = number

        val notesAfter = notesBefore.deepCopy()
        if (number != 0 && validation.isValid) {
            notesAfter.autoRemoveNotes(row, col, number, newBoard)
        } else if (number != 0) {
            notesAfter.clearNotes(row, col)
        }

        if (moveIndex < moveHistory.size - 1) {
            moveHistory.subList(moveIndex + 1, moveHistory.size).clear()
        }
        moveHistory.add(Move(row, col, oldValue, number, notesBefore, notesAfter))
        moveIndex++

        val finalGame = updatedGame.copy(
            board = updatedGame.board.copy(board = newBoard),
            notes = notesAfter
        )
        currentGame = finalGame

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

        gameRepository.updateGame(finalGame)

        if (validation.isValid && number != 0) {
            soundManager.playCorrect()
            checkGameCompletion()
        }
    }

    // -------------------------------------------------------------------------
    // Game completion (always called inside moveMutex)
    // -------------------------------------------------------------------------

    private suspend fun checkGameCompletion() {
        val game = currentGame ?: return          // always the freshest game, never a stale copy
        if (game.state == GameState.COMPLETED) return
        if (!GameUtils.isBoardComplete(game.board.board)) return

        val gameTime = game.elapsedTime
        val completedGame = game.copy(
            state = GameState.COMPLETED,
            completedAt = DateUtils.getCurrentTimestamp()
        )
        currentGame = completedGame
        stopTimer()
        soundManager.playComplete()

        _uiState.update {
            it.copy(
                game = completedGame,
                conflictCells = emptySet(),
                highlightedCells = emptySet(),
                sameNumberCells = emptySet(),
                showCompletionDialog = true,
                completionTime = gameTime,
                numberCounts = computeNumberCounts(completedGame.board.board)
            )
        }

        // Persist the game FIRST, then record stats.
        // If the app dies in between we lose one stat instead of double-counting it later.
        gameRepository.updateGame(completedGame)
        statisticsRepository.recordGameCompleted(
            difficulty = game.difficulty,
            gameTime = gameTime,
            hintsUsed = game.hintsUsed
        )
    }

    // -------------------------------------------------------------------------
    // Undo / Redo
    // -------------------------------------------------------------------------

    fun onUndoMove() {
        if (moveIndex < 0) {
            showToast("Nothing to undo")
            return
        }
        viewModelScope.launch {
            moveMutex.withLock {
                if (currentGame?.state != GameState.PLAYING) return@withLock   // no undo after win/loss/pause
                if (moveIndex < 0) {
                    showToast("Nothing to undo")
                    return@withLock
                }
                val move = moveHistory[moveIndex]
                moveIndex--
                applyBoardChange(move.row, move.col, move.oldValue, move.notesBefore)
                soundManager.playClick()
            }
        }
    }

    fun onRedoMove() {
        if (moveIndex >= moveHistory.size - 1) {
            showToast("Nothing to redo")
            return
        }
        viewModelScope.launch {
            moveMutex.withLock {
                if (currentGame?.state != GameState.PLAYING) return@withLock
                if (moveIndex >= moveHistory.size - 1) {
                    showToast("Nothing to redo")
                    return@withLock
                }
                moveIndex++
                val move = moveHistory[moveIndex]
                applyBoardChange(move.row, move.col, move.newValue, move.notesAfter)
                soundManager.playClick()
            }
        }
    }

    /** suspend + called inside the mutex, so rapid undo/redo are applied strictly in order. */
    private suspend fun applyBoardChange(row: Int, col: Int, value: Int, notes: Notes) {
        val game = currentGame ?: return
        val newBoard = game.board.board.map { it.clone() }.toTypedArray()
        newBoard[row][col] = value
        val updatedGame = game.copy(
            board = game.board.copy(board = newBoard),
            notes = notes
        )
        currentGame = updatedGame

        val conflicts = recomputeConflicts(newBoard, updatedGame.board.solution)
        val sel = _uiState.value.selectedCell
        val (related, sameNum) = if (sel != null) computeHighlights(newBoard, sel.first, sel.second)
        else emptySet<Pair<Int, Int>>() to emptySet()

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
        gameRepository.updateGame(updatedGame)
    }

    // -------------------------------------------------------------------------
    // New Game
    // -------------------------------------------------------------------------

    fun onNewGame() {
        if (_uiState.value.isGeneratingNewGame) return        // ignore double taps
        dismissHint()
        val currentDifficulty = currentGame?.difficulty ?: Difficulty.MEDIUM
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingNewGame = true) }
            try {
                // Heavy CPU work: keep it off the main thread
                val board = withContext(Dispatchers.Default) {
                    sudokuGenerator.generatePuzzle(currentDifficulty)
                }
                val now = DateUtils.getCurrentTimestamp()
                val maxMistakes = Constants.getMaxMistakes(currentDifficulty.name)
                val newGame = Game(
                    id = UUID.randomUUID().toString(),
                    board = board,
                    initialBoard = board.deepCopy(),
                    difficulty = currentDifficulty,
                    startTime = now,
                    currentTime = now,
                    state = GameState.PLAYING,
                    hintsUsed = 0,
                    createdAt = now,
                    completedAt = null,
                    mistakes = 0,
                    maxMistakes = maxMistakes,
                    notes = Notes()
                )
                gameRepository.saveGame(newGame)
                gameRepository.deleteAbandonedGames(newGame.id)

                moveMutex.withLock {
                    currentGame = newGame
                    moveHistory.clear()
                    moveIndex = -1
                }
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
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "onNewGame() failed: ${e.message}", e)
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
        if (game.isFinished()) return
        if (game.hintsUsed >= Constants.MAX_HINTS) {
            showToast("No more hints available (${Constants.MAX_HINTS} used)")
            return
        }
        val (r, c) = _uiState.value.selectedCell ?: run {
            showToast("Tap an empty cell to get a hint")
            return
        }
        if (game.board.getCurrentValue(r, c) != 0) {
            showToast("Hint only works on empty cells")
            return
        }
        if (GameUtils.getHintForCell(game.board.board, game.board.solution, r, c) == null) {
            showToast("No hint available for this cell")
            return
        }
        // Everything is re-validated against the LATEST game inside the lock
        viewModelScope.launch { moveMutex.withLock { applyHintAt(r, c) } }
    }

    private suspend fun applyHintAt(r: Int, c: Int) {
        if (currentGame?.state == GameState.PAUSED) resumeGame()
        val game = currentGame ?: return
        if (game.state != GameState.PLAYING) return
        if (game.hintsUsed >= Constants.MAX_HINTS) return
        if (game.board.getCurrentValue(r, c) != 0) return
        val hint = GameUtils.getHintForCell(game.board.board, game.board.solution, r, c) ?: return
        val value = hint.value

        val notesBefore = game.notes.deepCopy()
        val clearedNotes = game.notes.deepCopy()
        clearedNotes.clearNotes(r, c)

        val newBoard = game.board.board.map { it.clone() }.toTypedArray()
        newBoard[r][c] = value
        clearedNotes.autoRemoveNotes(r, c, value, newBoard)

        if (moveIndex < moveHistory.size - 1) {
            moveHistory.subList(moveIndex + 1, moveHistory.size).clear()
        }
        moveHistory.add(Move(r, c, 0, value, notesBefore, clearedNotes))
        moveIndex++

        val updatedBoard = game.board.copy(board = newBoard)
        val updatedGame = game.copy(
            board = updatedBoard,
            hintsUsed = game.hintsUsed + 1,
            notes = clearedNotes
        )
        currentGame = updatedGame

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
        gameRepository.updateGame(updatedGame)
        checkGameCompletion()
    }

    // -------------------------------------------------------------------------
    // Reset board
    // NOTE: now returns Unit (was Job) and runs under the mutex.
    // -------------------------------------------------------------------------

    fun onResetBoard() {
        viewModelScope.launch {
            moveMutex.withLock {
                val game = currentGame ?: return@withLock
                val freshBoard = game.initialBoard.deepCopy()
                val clearedNotes = Notes()
                val resetGame = game.copy(
                    board = freshBoard,
                    state = GameState.PLAYING,
                    hintsUsed = 0,
                    mistakes = 0,
                    notes = clearedNotes,
                    completedAt = null
                )
                currentGame = resetGame
                moveHistory.clear()
                moveIndex = -1
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
                        numberCounts = computeNumberCounts(freshBoard.board),
                        lastPlacedCell = null,
                        lastMistakeCell = null,
                        isNotesMode = false,
                        showCompletionDialog = false,
                        showGameOverDialog = false,
                        showHint = null,
                        hintMessage = null
                    )
                }
                startTimer()
                gameRepository.updateGame(resetGame)
            }
        }
    }

    // -------------------------------------------------------------------------
    // Pause / Resume
    // -------------------------------------------------------------------------

    fun onPauseToggle() {
        when (currentGame?.state) {
            GameState.PLAYING -> pauseGame()
            GameState.PAUSED -> resumeGame()
            else -> {}
        }
    }

    /** Call this from the screen's ON_STOP lifecycle event so the timer doesn't run in the background. */
    fun onAppBackgrounded() {
        if (currentGame?.state == GameState.PLAYING) pauseGame()
    }

    private fun pauseGame() {
        val game = currentGame ?: return
        if (game.state != GameState.PLAYING) return
        stopTimer()
        val paused = game.copy(state = GameState.PAUSED)
        currentGame = paused
        _uiState.update { it.copy(game = paused) }
        persistLatest()
    }

    private fun resumeGame() {
        val game = currentGame ?: return
        if (game.state != GameState.PAUSED) return   // FIX: never revive LOST / COMPLETED games
        val now = DateUtils.getCurrentTimestamp()
        val elapsed = game.elapsedTime
        val resumed = game.copy(
            state = GameState.PLAYING,
            startTime = now - elapsed,
            currentTime = now
        )
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
            var ticks = 0
            while (currentGame?.state == GameState.PLAYING) {
                delay(1000L)
                val g = currentGame ?: break
                if (g.state != GameState.PLAYING) break
                val updated = g.copy(currentTime = g.currentTime + 1000L)
                currentGame = updated
                _uiState.update { it.copy(game = updated) }
                // Save progress every 10s so a killed process doesn't lose the elapsed time
                if (++ticks % 10 == 0) persistLatest()
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
        val game = currentGame
        if (game != null && game.state == GameState.PLAYING) {
            applicationScope.launch {
                try {
                    // FIX: keep the game's own clock (same as pauseGame), not the wall clock
                    gameRepository.updateGame(game.copy(state = GameState.PAUSED))
                    Log.d(TAG, "Game auto-paused on ViewModel destruction")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to auto-pause game", e)
                }
            }
        }
    }
}