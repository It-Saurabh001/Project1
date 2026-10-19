package com.saurabh.sudoku.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.viewModelScope
import com.saurabh.sudoku.data.generator.SudokuGenerator
import com.saurabh.sudoku.domain.model.Difficulty
import com.saurabh.sudoku.domain.model.Game
import com.saurabh.sudoku.domain.model.GameState
import com.saurabh.sudoku.domain.model.SudokuBoard
import com.saurabh.sudoku.domain.repository.GameRepository
import com.saurabh.sudoku.domain.repository.StatisticsRepository
import com.saurabh.sudoku.domain.usecase.ValidateMoveUseCase
import com.saurabh.sudoku.presentation.utils.SoundManager
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

/**
 * Unit tests for [GameViewModel].
 *
 * ── WHY vmTest() IS MANDATORY ────────────────────────────────────────────────
 * loadGame() auto-starts a `while (PLAYING) { delay(1000) }` timer inside
 * viewModelScope.  If that scope is still alive when a test calls
 * advanceUntilIdle(), the TestScheduler loops forever trying to drain the
 * endless delay — the test hangs.
 *
 * Solution: every test that loads a PLAYING game uses [vmTest], which cancels
 * viewModelScope in a finally block, stopping the timer before runTest ends.
 *
 * ── WHICH ADVANCE CALL TO USE ─────────────────────────────────────────────────
 *   runCurrent()         — runs only already-enqueued coroutines; safe after any
 *                          action when a PLAYING timer is alive.
 *   advanceTimeBy(ms)    — jumps virtual clock by exactly ms; use when you need
 *                          the timer to fire a specific number of ticks.
 *   advanceUntilIdle()   — ONLY safe when the game is PAUSED/COMPLETED/LOST
 *                          (no infinite delay loop is running).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @RelaxedMockK lateinit var gameRepository: GameRepository
    @RelaxedMockK lateinit var statisticsRepository: StatisticsRepository
    @RelaxedMockK lateinit var sudokuGenerator: SudokuGenerator
    @RelaxedMockK lateinit var soundManager: SoundManager

    private lateinit var validateMoveUseCase: ValidateMoveUseCase
    private lateinit var appScope: TestScope
    private lateinit var viewModel: GameViewModel

    // ── Safe test runner ──────────────────────────────────────────────────────
    //
    // Always cancels viewModelScope in a finally block so the timer loop
    // (started by loadGame / startTimer) cannot outlive the test.
    // Timeout = 10 s; a hung test will fail fast instead of blocking CI.

    private fun vmTest(block: suspend TestScope.() -> Unit) =
        runTest(testDispatcher, timeout = 10.seconds) {
            try {
                block()
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Builds a fully-playable [Game]:
     *  - solution[0][0] = 5  (correct answer)
     *  - all other solution cells = 1
     *  - initial board: cell (1,1) = 4 (clue), everything else 0
     */
    private fun buildTestGame(
        state: GameState = GameState.PLAYING,
        mistakes: Int = 0,
        maxMistakes: Int = 3
    ): Game {
        val solution = Array(9) { IntArray(9) { 1 } }
        solution[0][0] = 5

        val initial = Array(9) { IntArray(9) }
        initial[1][1] = 4

        val board = Array(9) { r -> initial[r].clone() }
        val sudokuBoard = SudokuBoard(board = board, solution = solution, initialBoard = initial)
        val now = System.currentTimeMillis()
        return Game(
            id = "test-game-id",
            board = sudokuBoard,
            initialBoard = sudokuBoard,
            difficulty = Difficulty.MEDIUM,
            startTime = now,
            currentTime = now,
            state = state,
            createdAt = now,
            mistakes = mistakes,
            maxMistakes = maxMistakes
        )
    }

    /**
     * Stubs the repository and triggers loadGame(), then drains the load
     * coroutine with [runCurrent].
     *
     * IMPORTANT: this uses runCurrent(), NOT advanceUntilIdle(), because
     * after loadGame the timer is running — advanceUntilIdle() would hang.
     */
    private fun TestScope.loadGameSync(game: Game) {
        coEvery { gameRepository.getGameById("test-game-id") } returns game
        viewModel.loadGame("test-game-id")
        runCurrent()   // drains the launch{} in loadGame; timer is now live but we don't advance
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        validateMoveUseCase = ValidateMoveUseCase()
        appScope = TestScope(testDispatcher)

        viewModel = GameViewModel(
            gameRepository = gameRepository,
            statisticsRepository = statisticsRepository,
            sudokuGenerator = sudokuGenerator,
            validateMoveUseCase = validateMoveUseCase,
            soundManager = soundManager,
            applicationScope = appScope
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    // ── loadGame ──────────────────────────────────────────────────────────────

    @Test
    fun `loadGame sets isLoading true then false on success`() = vmTest {
        val game = buildTestGame()
        coEvery { gameRepository.getGameById("test-game-id") } returns game

        viewModel.loadGame("test-game-id")
        assertTrue("isLoading must be true before coroutine runs", viewModel.uiState.value.isLoading)

        runCurrent()
        assertFalse("isLoading must be false after load completes", viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadGame populates uiState game with correct data`() = vmTest {
        val game = buildTestGame()
        loadGameSync(game)

        assertEquals("test-game-id", viewModel.uiState.value.game?.id)
        assertEquals(Difficulty.MEDIUM, viewModel.uiState.value.game?.difficulty)
    }

    @Test
    fun `loadGame sets error when repository returns null`() = vmTest {
        coEvery { gameRepository.getGameById(any()) } returns null
        viewModel.loadGame("missing-id")
        runCurrent()

        assertNotNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadGame auto-resumes PAUSED game to PLAYING state`() = vmTest {
        val pausedGame = buildTestGame(state = GameState.PAUSED)
        loadGameSync(pausedGame)

        assertEquals(GameState.PLAYING, viewModel.uiState.value.game?.state)
    }

    @Test
    fun `loadGame is idempotent when called twice with same gameId`() = vmTest {
        val game = buildTestGame()
        coEvery { gameRepository.getGameById("test-game-id") } returns game

        viewModel.loadGame("test-game-id")
        runCurrent()
        viewModel.loadGame("test-game-id")   // second call — should be short-circuited
        runCurrent()

        // Repository must only have been hit once
        coVerify(exactly = 1) { gameRepository.getGameById("test-game-id") }
    }

    // ── onCellSelected ────────────────────────────────────────────────────────

    @Test
    fun `selecting a cell updates selectedCell in uiState`() = vmTest {
        loadGameSync(buildTestGame())

        viewModel.onCellSelected(3, 5)

        assertEquals(3 to 5, viewModel.uiState.value.selectedCell)
    }

    @Test
    fun `selecting same cell again deselects it`() = vmTest {
        loadGameSync(buildTestGame())

        viewModel.onCellSelected(3, 5)
        viewModel.onCellSelected(3, 5)

        assertNull(viewModel.uiState.value.selectedCell)
    }

    @Test
    fun `selecting a cell computes non-empty highlightedCells`() = vmTest {
        loadGameSync(buildTestGame())

        viewModel.onCellSelected(4, 4)

        assertTrue(viewModel.uiState.value.highlightedCells.isNotEmpty())
    }

    // ── onNumberSelected — correct move ───────────────────────────────────────

    @Test
    fun `placing correct number updates board cell value`() = vmTest {
        loadGameSync(buildTestGame())

        viewModel.onCellSelected(0, 0)
        viewModel.onNumberSelected(5)   // solution[0][0] == 5
        runCurrent()                    // drains the launch{} in onNumberSelected

        assertEquals(5, viewModel.uiState.value.game?.board?.getCurrentValue(0, 0))
    }

    @Test
    fun `placing number on clue cell leaves board unchanged`() = vmTest {
        loadGameSync(buildTestGame())

        viewModel.onCellSelected(1, 1)   // clue cell (initial[1][1] == 4)
        viewModel.onNumberSelected(9)
        runCurrent()

        assertEquals(4, viewModel.uiState.value.game?.board?.getCurrentValue(1, 1))
    }

    // ── onNumberSelected — wrong move / mistakes ───────────────────────────────

    @Test
    fun `placing wrong number increments mistake count`() = vmTest {
        loadGameSync(buildTestGame())

        viewModel.onCellSelected(0, 0)
        viewModel.onNumberSelected(9)   // solution is 5
        runCurrent()

        assertEquals(1, viewModel.uiState.value.game?.mistakes)
    }

    @Test
    fun `placing wrong number on last-mistake Expert triggers showGameOverDialog`() = vmTest {
        loadGameSync(buildTestGame(maxMistakes = 1))

        viewModel.onCellSelected(0, 0)
        viewModel.onNumberSelected(9)
        runCurrent()

        assertTrue(viewModel.uiState.value.showGameOverDialog)
    }

    @Test
    fun `placing wrong number triggers showMistakeToast`() = vmTest {
        loadGameSync(buildTestGame())

        viewModel.onCellSelected(0, 0)
        viewModel.onNumberSelected(9)
        runCurrent()

        assertTrue(viewModel.uiState.value.showMistakeToast)
    }

    @Test
    fun `placing number without selected cell sets hintMessage`() = vmTest {
        loadGameSync(buildTestGame())

        viewModel.onNumberSelected(5)   // no cell selected
        runCurrent()

        assertNotNull(viewModel.uiState.value.hintMessage)
    }

    // ── Concurrency / mutex ────────────────────────────────────────────────────

    @Test
    fun `rapid concurrent moves result in consistent sequential state`() = vmTest {
        loadGameSync(buildTestGame())

        viewModel.onCellSelected(0, 0)
        repeat(3) { viewModel.onNumberSelected(5) }
        runCurrent()   // drain all 3 launches sequentially (mutex serialises them)

        val boardValue = viewModel.uiState.value.game?.board?.getCurrentValue(0, 0)
        assertNotNull(boardValue)
        assertEquals(5, boardValue)
    }

    // ── onEraseSelected ───────────────────────────────────────────────────────

    @Test
    fun `erasing a filled non-initial cell sets it to 0`() = vmTest {
        loadGameSync(buildTestGame())

        viewModel.onCellSelected(0, 0)
        viewModel.onNumberSelected(5)
        runCurrent()

        // After onNumberSelected, makeMove does NOT clear selectedCell → (0,0) is still selected.
        // Calling onCellSelected(0,0) again would TOGGLE-DESELECT it (setting selectedCell=null),
        // causing onEraseSelected() to return early. Just call erase directly.
        viewModel.onEraseSelected()
        runCurrent()

        assertEquals(0, viewModel.uiState.value.game?.board?.getCurrentValue(0, 0))
    }

    @Test
    fun `erasing an empty cell with no notes does nothing`() = vmTest {
        loadGameSync(buildTestGame())

        viewModel.onCellSelected(0, 0)   // empty, no notes
        viewModel.onEraseSelected()
        runCurrent()

        assertEquals(0, viewModel.uiState.value.game?.board?.getCurrentValue(0, 0))
    }

    @Test
    fun `erasing a clue cell does nothing`() = vmTest {
        loadGameSync(buildTestGame())

        viewModel.onCellSelected(1, 1)   // clue cell
        viewModel.onEraseSelected()
        runCurrent()

        assertEquals(4, viewModel.uiState.value.game?.board?.getCurrentValue(1, 1))
    }

    // ── Undo / Redo ───────────────────────────────────────────────────────────

    @Test
    fun `onUndoMove restores previous cell value`() = vmTest {
        loadGameSync(buildTestGame())

        viewModel.onCellSelected(0, 0)
        viewModel.onNumberSelected(5)
        runCurrent()

        viewModel.onUndoMove()
        runCurrent()

        assertEquals(0, viewModel.uiState.value.game?.board?.getCurrentValue(0, 0))
    }

    @Test
    fun `onRedoMove re-applies undone move`() = vmTest {
        loadGameSync(buildTestGame())

        viewModel.onCellSelected(0, 0)
        viewModel.onNumberSelected(5)
        runCurrent()

        viewModel.onUndoMove()
        runCurrent()

        viewModel.onRedoMove()
        runCurrent()

        assertEquals(5, viewModel.uiState.value.game?.board?.getCurrentValue(0, 0))
    }

    @Test
    fun `onUndoMove on empty history sets hintMessage`() = vmTest {
        loadGameSync(buildTestGame())

        viewModel.onUndoMove()
        runCurrent()

        assertNotNull(viewModel.uiState.value.hintMessage)
    }

    @Test
    fun `onRedoMove on empty future history sets hintMessage`() = vmTest {
        loadGameSync(buildTestGame())

        viewModel.onRedoMove()
        runCurrent()

        assertNotNull(viewModel.uiState.value.hintMessage)
    }

    @Test
    fun `new move after undo clears forward redo stack`() = vmTest {
        loadGameSync(buildTestGame())

        viewModel.onCellSelected(0, 0)
        viewModel.onNumberSelected(5)
        runCurrent()

        viewModel.onUndoMove()
        runCurrent()

        viewModel.onCellSelected(0, 0)
        viewModel.onNumberSelected(1)   // branching move — clears redo stack
        runCurrent()

        viewModel.onRedoMove()   // should show toast, nothing to redo
        runCurrent()
        assertNotNull(viewModel.uiState.value.hintMessage)
    }

    @Test
    fun `undo then redo sequence is fully reversible`() = vmTest {
        loadGameSync(buildTestGame())

        viewModel.onCellSelected(0, 0)
        viewModel.onNumberSelected(5)
        runCurrent()
        val afterPlace = viewModel.uiState.value.game?.board?.getCurrentValue(0, 0)

        viewModel.onUndoMove()
        runCurrent()
        val afterUndo = viewModel.uiState.value.game?.board?.getCurrentValue(0, 0)

        viewModel.onRedoMove()
        runCurrent()
        val afterRedo = viewModel.uiState.value.game?.board?.getCurrentValue(0, 0)

        assertEquals(5, afterPlace)
        assertEquals(0, afterUndo)
        assertEquals(5, afterRedo)
    }

    // ── Notes mode ─────────────────────────────────────────────────────────────

    @Test
    fun `toggleNotesMode flips isNotesMode flag`() = vmTest {
        loadGameSync(buildTestGame())

        assertFalse(viewModel.uiState.value.isNotesMode)
        viewModel.toggleNotesMode()
        assertTrue(viewModel.uiState.value.isNotesMode)
    }

    @Test
    fun `adding note in notes mode does not push to undo history`() = vmTest {
        loadGameSync(buildTestGame())

        viewModel.toggleNotesMode()
        viewModel.onCellSelected(0, 0)
        viewModel.onNumberSelected(3)
        runCurrent()

        // Undo should show toast (no board-move recorded in history)
        viewModel.onUndoMove()
        runCurrent()
        assertNotNull(viewModel.uiState.value.hintMessage)
    }

    // ── Pause / Resume ──────────────────────────────────────────────────────────

    @Test
    fun `onPauseToggle switches PLAYING to PAUSED`() = vmTest {
        loadGameSync(buildTestGame(state = GameState.PLAYING))

        viewModel.onPauseToggle()
        // pauseGame() is synchronous (no launch) — state changes immediately
        assertEquals(GameState.PAUSED, viewModel.uiState.value.game?.state)

        // With game PAUSED the timer is stopped — advanceUntilIdle() is now safe
        advanceUntilIdle()
        assertEquals(GameState.PAUSED, viewModel.uiState.value.game?.state)
    }

    // ── UI helpers ─────────────────────────────────────────────────────────────

    @Test
    fun `dismissCompletionDialog sets showCompletionDialog to false`() = vmTest {
        viewModel.dismissCompletionDialog()
        assertFalse(viewModel.uiState.value.showCompletionDialog)
    }

    @Test
    fun `dismissGameOverDialog sets showGameOverDialog to false`() = vmTest {
        viewModel.dismissGameOverDialog()
        assertFalse(viewModel.uiState.value.showGameOverDialog)
    }

    @Test
    fun `clearError sets error to null`() = vmTest {
        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `dismissMistakeToast sets showMistakeToast to false`() = vmTest {
        viewModel.dismissMistakeToast()
        assertFalse(viewModel.uiState.value.showMistakeToast)
    }

    // ── onResetBoard ───────────────────────────────────────────────────────────

    @Test
    fun `onResetBoard restores initial board and clears notes and mistakes`() = vmTest {
        loadGameSync(buildTestGame())

        viewModel.onCellSelected(0, 0)
        viewModel.onNumberSelected(5)
        runCurrent()

        viewModel.onResetBoard()
        runCurrent()

        assertEquals(0, viewModel.uiState.value.game?.board?.getCurrentValue(0, 0))
        assertEquals(0, viewModel.uiState.value.game?.mistakes)
        assertTrue(viewModel.uiState.value.notes.getNotes(0, 0).isEmpty())
    }

    @Test
    fun `onResetBoard clears move history so undo shows toast`() = vmTest {
        loadGameSync(buildTestGame())

        viewModel.onCellSelected(0, 0)
        viewModel.onNumberSelected(5)
        runCurrent()

        viewModel.onResetBoard()
        runCurrent()

        viewModel.onUndoMove()
        runCurrent()
        assertNotNull(viewModel.uiState.value.hintMessage)
    }

    // ── numberCounts ───────────────────────────────────────────────────────────

    @Test
    fun `loadGame initialises numberCounts with 9 entries, one per digit`() = vmTest {
        loadGameSync(buildTestGame())

        val counts = viewModel.uiState.value.numberCounts
        assertEquals(9, counts.size)
        for (n in 1..9) {
            assertTrue("numberCounts must contain key $n", counts.containsKey(n))
        }
    }

    // ── Timer advances virtual time ────────────────────────────────────────────

    @Test
    fun `timer increments elapsed time after 3 seconds of virtual time`() = vmTest {
        loadGameSync(buildTestGame())

        val timeBefore = viewModel.uiState.value.game?.currentTime ?: 0L
        advanceTimeBy(3_001L)   // skip 3 timer ticks

        val timeAfter = viewModel.uiState.value.game?.currentTime ?: 0L
        assertTrue("currentTime must have increased after 3 virtual seconds", timeAfter > timeBefore)
    }

    @Test
    fun `paused game timer does not advance`() = vmTest {
        loadGameSync(buildTestGame())

        viewModel.onPauseToggle()   // pause immediately — stopTimer() is called synchronously
        val timeBefore = viewModel.uiState.value.game?.currentTime ?: 0L

        advanceTimeBy(5_001L)   // 5 virtual seconds
        val timeAfter = viewModel.uiState.value.game?.currentTime ?: 0L

        // Timer is cancelled on pause — currentTime must not change
        assertEquals("Paused game time must not advance", timeBefore, timeAfter)
    }
}
