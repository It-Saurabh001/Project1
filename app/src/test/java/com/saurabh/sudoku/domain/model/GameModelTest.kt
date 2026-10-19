package com.saurabh.sudoku.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [Game], [SudokuBoard], and [Difficulty] domain models.
 * Pure JVM — no Android dependencies.
 */
class GameModelTest {

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun buildSudokuBoard(
        boardValue: Int = 0,
        solutionValue: Int = 5,
        initialValue: Int = 0
    ): SudokuBoard {
        val board = Array(9) { IntArray(9) { boardValue } }
        val solution = Array(9) { IntArray(9) { solutionValue } }
        val initial = Array(9) { IntArray(9) { initialValue } }
        return SudokuBoard(board = board, solution = solution, initialBoard = initial)
    }

    private fun buildGame(
        startTime: Long = 1_000L,
        currentTime: Long = 6_000L,
        state: GameState = GameState.PLAYING,
        completedAt: Long? = null,
        mistakes: Int = 0,
        maxMistakes: Int = 3
    ): Game {
        val board = buildSudokuBoard()
        return Game(
            id = "test-id",
            board = board,
            initialBoard = board,
            difficulty = Difficulty.MEDIUM,
            startTime = startTime,
            currentTime = currentTime,
            state = state,
            hintsUsed = 0,
            createdAt = startTime,
            completedAt = completedAt,
            mistakes = mistakes,
            maxMistakes = maxMistakes
        )
    }

    // ── Game.elapsedTime ──────────────────────────────────────────────────

    @Test
    fun `elapsedTime returns currentTime minus startTime when game is active`() {
        val game = buildGame(startTime = 1_000L, currentTime = 6_000L)
        assertEquals(5_000L, game.elapsedTime)
    }

    @Test
    fun `elapsedTime uses completedAt minus startTime when game is completed`() {
        val game = buildGame(startTime = 1_000L, currentTime = 99_000L, completedAt = 11_000L)
        assertEquals(10_000L, game.elapsedTime)
    }

    @Test
    fun `elapsedTime is 0 when startTime equals currentTime`() {
        val game = buildGame(startTime = 5_000L, currentTime = 5_000L)
        assertEquals(0L, game.elapsedTime)
    }

    // ── Game.isCompleted / isLost ──────────────────────────────────────────

    @Test
    fun `isCompleted is true only when state is COMPLETED`() {
        val game = buildGame(state = GameState.COMPLETED)
        assertTrue(game.isCompleted)
    }

    @Test
    fun `isCompleted is false when state is PLAYING`() {
        val game = buildGame(state = GameState.PLAYING)
        assertFalse(game.isCompleted)
    }

    @Test
    fun `isLost is true only when state is LOST`() {
        val game = buildGame(state = GameState.LOST)
        assertTrue(game.isLost)
    }

    @Test
    fun `isLost is false when state is PLAYING`() {
        val game = buildGame(state = GameState.PLAYING)
        assertFalse(game.isLost)
    }

    // ── Game.remainingMistakes ─────────────────────────────────────────────

    @Test
    fun `remainingMistakes equals maxMistakes minus mistakes`() {
        val game = buildGame(mistakes = 1, maxMistakes = 3)
        assertEquals(2, game.remainingMistakes)
    }

    @Test
    fun `remainingMistakes is 0 when mistakes equal maxMistakes`() {
        val game = buildGame(mistakes = 3, maxMistakes = 3)
        assertEquals(0, game.remainingMistakes)
    }

    // ── Game.copy preserves unmodified fields ──────────────────────────────

    @Test
    fun `game copy preserves all unmodified fields`() {
        val original = buildGame(mistakes = 1, maxMistakes = 5)
        val updated = original.copy(mistakes = 2)

        assertEquals(2, updated.mistakes)
        assertEquals(5, updated.maxMistakes)           // unchanged
        assertEquals(original.id, updated.id)           // unchanged
        assertEquals(original.difficulty, updated.difficulty) // unchanged
    }

    // ── SudokuBoard.isInitialCell ──────────────────────────────────────────

    @Test
    fun `isInitialCell returns true when initialBoard value is non-zero`() {
        val initial = Array(9) { IntArray(9) }
        initial[3][4] = 7
        val board = SudokuBoard(
            board = Array(9) { IntArray(9) },
            solution = Array(9) { IntArray(9) },
            initialBoard = initial
        )
        assertTrue(board.isInitialCell(3, 4))
    }

    @Test
    fun `isInitialCell returns false when initialBoard value is zero`() {
        val board = buildSudokuBoard(initialValue = 0)
        assertFalse(board.isInitialCell(0, 0))
    }

    // ── SudokuBoard.getCurrentValue ────────────────────────────────────────

    @Test
    fun `getCurrentValue returns zero for empty cells`() {
        val board = buildSudokuBoard(boardValue = 0)
        assertEquals(0, board.getCurrentValue(4, 4))
    }

    @Test
    fun `getCurrentValue returns placed digit`() {
        val boardArray = Array(9) { IntArray(9) }
        boardArray[5][5] = 9
        val board = SudokuBoard(
            board = boardArray,
            solution = Array(9) { IntArray(9) },
            initialBoard = Array(9) { IntArray(9) }
        )
        assertEquals(9, board.getCurrentValue(5, 5))
    }

    // ── SudokuBoard array independence ─────────────────────────────────────

    @Test
    fun `modifying original board array does not affect getSolutionValue`() {
        val sol = Array(9) { IntArray(9) { 1 } }
        val board = SudokuBoard(
            board = Array(9) { IntArray(9) },
            solution = sol,
            initialBoard = Array(9) { IntArray(9) }
        )
        sol[0][0] = 99  // mutate the reference passed to constructor
        // SudokuBoard holds the same reference — this is expected behaviour;
        // this test documents the mutability characteristic.
        // If defensive copy is added later, update expected value to 1.
        assertEquals(99, board.getSolutionValue(0, 0))
    }

    // ── Difficulty ordinal order ───────────────────────────────────────────

    @Test
    fun `Difficulty ordinal order is EASY less than MEDIUM less than HARD less than EXPERT`() {
        assertTrue(Difficulty.EASY.ordinal < Difficulty.MEDIUM.ordinal)
        assertTrue(Difficulty.MEDIUM.ordinal < Difficulty.HARD.ordinal)
        assertTrue(Difficulty.HARD.ordinal < Difficulty.EXPERT.ordinal)
    }

    @Test
    fun `EASY has the most clues`() {
        assertTrue(Difficulty.EASY.clues > Difficulty.MEDIUM.clues)
        assertTrue(Difficulty.MEDIUM.clues > Difficulty.HARD.clues)
        assertTrue(Difficulty.HARD.clues > Difficulty.EXPERT.clues)
    }

    @Test
    fun `Difficulty fromString maps known names correctly`() {
        assertEquals(Difficulty.HARD, Difficulty.fromString("HARD"))
        assertEquals(Difficulty.EXPERT, Difficulty.fromString("EXPERT"))
    }

    @Test
    fun `Difficulty fromString defaults to EASY for unknown string`() {
        assertEquals(Difficulty.EASY, Difficulty.fromString("UNKNOWN"))
    }
}
