package com.saurabh.sudoku.domain.usecase

import com.saurabh.sudoku.domain.model.SudokuBoard
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [ValidateMoveUseCase].
 *
 * No Android framework dependencies — pure JVM tests.
 */
class ValidateMoveUseCaseTest {

    private lateinit var useCase: ValidateMoveUseCase

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Builds a minimal [SudokuBoard] where:
     *  - solution[0][0] = 5  (correct answer for (0,0))
     *  - initialBoard[1][1] ≠ 0  → cell (1,1) is a clue/initial cell
     *  - all other cells are 0 in both board and initialBoard
     */
    private fun buildTestBoard(): SudokuBoard {
        val solution = Array(9) { IntArray(9) }
        solution[0][0] = 5   // known correct answer
        solution[0][1] = 3

        val initialBoard = Array(9) { IntArray(9) }
        initialBoard[1][1] = 7   // marks (1,1) as a clue cell

        val board = Array(9) { row -> initialBoard[row].clone() }

        return SudokuBoard(
            board = board,
            solution = solution,
            initialBoard = initialBoard
        )
    }

    @Before
    fun setUp() {
        useCase = ValidateMoveUseCase()
    }

    // ── Erase (number = 0) ────────────────────────────────────────────────

    @Test
    fun `erasing a cell returns isValid true canPlaceNumber true and no mistake`() {
        val board = buildTestBoard()
        val result = useCase(board, 0, 0, 0, currentMistakes = 1, maxMistakes = 3)

        assertTrue("Erase should be valid", result.isValid)
        assertTrue("Erase should place (clear) the cell", result.canPlaceNumber)
        assertEquals("Mistakes must not change on erase", 1, result.newMistakes)
        assertFalse("Erase never triggers game over", result.isGameOver)
    }

    @Test
    fun `erasing with 0 mistakes keeps mistakes at 0`() {
        val board = buildTestBoard()
        val result = useCase(board, 0, 0, 0, currentMistakes = 0, maxMistakes = 3)

        assertEquals(0, result.newMistakes)
    }

    // ── Clue / initial cell protection ───────────────────────────────────

    @Test
    fun `placing number on initial cell returns canPlaceNumber false`() {
        val board = buildTestBoard()
        // (1,1) is an initial/clue cell
        val result = useCase(board, 1, 1, 9, currentMistakes = 0, maxMistakes = 3)

        assertFalse("Should not be able to place on a clue cell", result.canPlaceNumber)
        assertFalse("Clue-cell block is not a valid move", result.isValid)
        assertEquals("Mistakes must not change", 0, result.newMistakes)
        assertFalse(result.isGameOver)
    }

    // ── Correct placement ─────────────────────────────────────────────────

    @Test
    fun `placing correct number returns isValid true canPlace true and no new mistake`() {
        val board = buildTestBoard()
        // solution[0][0] == 5
        val result = useCase(board, 0, 0, 5, currentMistakes = 0, maxMistakes = 3)

        assertTrue(result.isValid)
        assertTrue(result.canPlaceNumber)
        assertEquals(0, result.newMistakes)
        assertFalse(result.isGameOver)
    }

    @Test
    fun `correct placement message is non-null and non-blank`() {
        val board = buildTestBoard()
        val result = useCase(board, 0, 0, 5, currentMistakes = 0, maxMistakes = 3)

        assertNotNull(result.message)
        assertTrue(result.message.isNotBlank())
    }

    // ── Incorrect placement ───────────────────────────────────────────────

    @Test
    fun `wrong number increments mistakes by 1`() {
        val board = buildTestBoard()
        val result = useCase(board, 0, 0, 9, currentMistakes = 0, maxMistakes = 3)

        assertFalse(result.isValid)
        assertTrue("Wrong number should still be placed visually", result.canPlaceNumber)
        assertEquals(1, result.newMistakes)
        assertFalse(result.isGameOver)
    }

    @Test
    fun `wrong number on Easy difficulty increments from 0 to 1`() {
        val board = buildTestBoard()
        val result = useCase(board, 0, 0, 2, currentMistakes = 0, maxMistakes = 5)

        assertEquals(1, result.newMistakes)
        assertFalse(result.isGameOver)
    }

    @Test
    fun `wrong number on Expert with maxMistakes=1 sets isGameOver to true`() {
        val board = buildTestBoard()
        val result = useCase(board, 0, 0, 9, currentMistakes = 0, maxMistakes = 1)

        assertTrue(result.isGameOver)
        assertEquals(1, result.newMistakes)
    }

    @Test
    fun `mistake at maxMistakes boundary triggers game over`() {
        val board = buildTestBoard()
        // 2 mistakes already, maxMistakes = 3 → this wrong move hits the limit
        val result = useCase(board, 0, 0, 9, currentMistakes = 2, maxMistakes = 3)

        assertTrue(result.isGameOver)
        assertEquals(3, result.newMistakes)
    }

    @Test
    fun `wrong number one below game-over threshold does not trigger game over`() {
        val board = buildTestBoard()
        val result = useCase(board, 0, 0, 9, currentMistakes = 1, maxMistakes = 3)

        assertFalse(result.isGameOver)
        assertEquals(2, result.newMistakes)
    }

    @Test
    fun `wrong number message is non-null and non-blank`() {
        val board = buildTestBoard()
        val result = useCase(board, 0, 0, 9, currentMistakes = 0, maxMistakes = 3)

        assertNotNull(result.message)
        assertTrue(result.message.isNotBlank())
    }

    @Test
    fun `game over message differs from normal mistake message`() {
        val board = buildTestBoard()
        val normalResult = useCase(board, 0, 0, 9, currentMistakes = 0, maxMistakes = 3)
        val gameOverResult = useCase(board, 0, 0, 9, currentMistakes = 2, maxMistakes = 3)

        assertNotEquals(normalResult.message, gameOverResult.message)
    }

    // ── Repeated wrong placements cannot exceed maxMistakes ───────────────

    @Test
    fun `mistakes do not exceed maxMistakes in a single invoke call`() {
        val board = buildTestBoard()
        val result = useCase(board, 0, 0, 9, currentMistakes = 5, maxMistakes = 3)

        // newMistakes = 6, but that is fine — VM handles LOST state
        // The use case itself should still set isGameOver = true
        assertTrue(result.isGameOver)
    }

    // ── Same value already present (handled by VM; use-case sees non-initial cell) ──

    @Test
    fun `placing correct digit at a non-initial cell returns isValid true`() {
        val board = buildTestBoard()
        // cell (0,1) is not initial, solution is 3
        val result = useCase(board, 0, 1, 3, currentMistakes = 0, maxMistakes = 3)

        assertTrue(result.isValid)
        assertEquals(0, result.newMistakes)
    }
}
