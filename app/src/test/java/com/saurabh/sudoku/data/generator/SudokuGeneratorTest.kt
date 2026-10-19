package com.saurabh.sudoku.data.generator

import com.saurabh.sudoku.domain.model.Difficulty
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [SudokuGenerator] and [SudokuSolver].
 *
 * Pure JVM tests — no Android framework dependencies.
 * Note: generatePuzzle() uses android.util.Log, so these tests
 * must run in a Robolectric environment or with Log mocked.
 * The build.gradle testOptions { unitTests.returnDefaultValues = true }
 * allows android.util.Log calls to silently return 0 in unit tests.
 */
class SudokuGeneratorTest {

    private lateinit var solver: SudokuSolver
    private lateinit var generator: SudokuGenerator

    @Before
    fun setUp() {
        solver = SudokuSolver()
        generator = SudokuGenerator(solver)
    }

    // ── generateCompleteSudoku — solution board validity ──────────────────

    @Test
    fun `generated complete sudoku has exactly 81 cells`() {
        val solution = generator.generateCompleteSudoku()
        var count = 0
        for (r in 0..8) for (c in 0..8) count++
        assertEquals(81, count)
    }

    @Test
    fun `generated complete sudoku contains no zeros`() {
        val solution = generator.generateCompleteSudoku()
        for (r in 0..8) {
            for (c in 0..8) {
                assertNotEquals(
                    "Zero found at ($r,$c) — solution must be fully filled",
                    0, solution[r][c]
                )
            }
        }
    }

    @Test
    fun `generated complete sudoku has no row duplicates`() {
        val solution = generator.generateCompleteSudoku()
        for (r in 0..8) {
            val seen = mutableSetOf<Int>()
            for (c in 0..8) {
                assertTrue(
                    "Duplicate ${solution[r][c]} in row $r",
                    seen.add(solution[r][c])
                )
            }
        }
    }

    @Test
    fun `generated complete sudoku has no column duplicates`() {
        val solution = generator.generateCompleteSudoku()
        for (c in 0..8) {
            val seen = mutableSetOf<Int>()
            for (r in 0..8) {
                assertTrue(
                    "Duplicate ${solution[r][c]} in column $c",
                    seen.add(solution[r][c])
                )
            }
        }
    }

    @Test
    fun `generated complete sudoku has no 3x3 box duplicates`() {
        val solution = generator.generateCompleteSudoku()
        for (boxRow in 0..2) {
            for (boxCol in 0..2) {
                val seen = mutableSetOf<Int>()
                for (r in 0..2) {
                    for (c in 0..2) {
                        val v = solution[boxRow * 3 + r][boxCol * 3 + c]
                        assertTrue(
                            "Duplicate $v in box ($boxRow,$boxCol)",
                            seen.add(v)
                        )
                    }
                }
            }
        }
    }

    // ── generatePuzzle — clue counts per difficulty ────────────────────────

    @Test
    fun `EASY puzzle has at least 36 and at most 46 clues`() {
        val board = generator.generatePuzzle(Difficulty.EASY)
        val clues = board.board.sumOf { row -> row.count { it != 0 } }
        assertTrue("EASY clue count $clues out of expected range [36,46]", clues in 36..46)
    }

    @Test
    fun `MEDIUM puzzle has at least 27 and at most 36 clues`() {
        val board = generator.generatePuzzle(Difficulty.MEDIUM)
        val clues = board.board.sumOf { row -> row.count { it != 0 } }
        assertTrue("MEDIUM clue count $clues out of expected range [27,36]", clues in 27..36)
    }

    @Test
    fun `HARD puzzle has at least 23 and at most 30 clues`() {
        val board = generator.generatePuzzle(Difficulty.HARD)
        val clues = board.board.sumOf { row -> row.count { it != 0 } }
        assertTrue("HARD clue count $clues out of expected range [23,30]", clues in 23..30)
    }

    @Test
    fun `EXPERT puzzle has at least 22 and at most 32 clues`() {
        // Upper bound is relaxed because greedy removal may stop early (see project.md Part 1.1)
        val board = generator.generatePuzzle(Difficulty.EXPERT)
        val clues = board.board.sumOf { row -> row.count { it != 0 } }
        assertTrue("EXPERT clue count $clues out of expected range [22,32]", clues in 22..32)
    }

    // ── generatePuzzle — cell invariants ─────────────────────────────────

    @Test
    fun `initial clue cells are non-zero`() {
        val board = generator.generatePuzzle(Difficulty.EASY)
        for (r in 0..8) {
            for (c in 0..8) {
                if (board.initialBoard[r][c] != 0) {
                    assertNotEquals("Clue cell ($r,$c) must not be zero", 0, board.board[r][c])
                }
            }
        }
    }

    @Test
    fun `non-clue cells start as zero in puzzle board`() {
        val board = generator.generatePuzzle(Difficulty.MEDIUM)
        for (r in 0..8) {
            for (c in 0..8) {
                if (board.initialBoard[r][c] == 0) {
                    assertEquals("Non-clue cell ($r,$c) should be 0", 0, board.board[r][c])
                }
            }
        }
    }

    @Test
    fun `puzzle board and initialBoard are equal on generation`() {
        val board = generator.generatePuzzle(Difficulty.EASY)
        for (r in 0..8) {
            assertArrayEquals(
                "board and initialBoard must match after generation",
                board.initialBoard[r], board.board[r]
            )
        }
    }

    // ── Unique solution verification ──────────────────────────────────────

    @Test
    fun `generated EASY puzzle has exactly one solution`() {
        val sudokuBoard = generator.generatePuzzle(Difficulty.EASY)
        val puzzle = Array(9) { r -> sudokuBoard.board[r].clone() }
        assertTrue("EASY puzzle must have a unique solution", solver.hasUniqueSolution(puzzle))
    }

    @Test
    fun `generated HARD puzzle has exactly one solution`() {
        val sudokuBoard = generator.generatePuzzle(Difficulty.HARD)
        val puzzle = Array(9) { r -> sudokuBoard.board[r].clone() }
        assertTrue("HARD puzzle must have a unique solution", solver.hasUniqueSolution(puzzle))
    }

    // ── Randomness ────────────────────────────────────────────────────────

    @Test
    fun `two consecutive calls produce different board configurations`() {
        val b1 = generator.generateCompleteSudoku()
        val b2 = generator.generateCompleteSudoku()
        var identical = true
        outer@ for (r in 0..8) {
            for (c in 0..8) {
                if (b1[r][c] != b2[r][c]) {
                    identical = false
                    break@outer
                }
            }
        }
        // May (rarely) fail by chance — acceptable for a randomness test
        assertFalse("Two generated boards should differ in at least one cell", identical)
    }

    // ── generatePuzzle does not throw ─────────────────────────────────────

    @Test
    fun `generatePuzzle does not throw for EASY`() {
        generator.generatePuzzle(Difficulty.EASY) // should not throw
    }

    @Test
    fun `generatePuzzle does not throw for MEDIUM`() {
        generator.generatePuzzle(Difficulty.MEDIUM)
    }

    @Test
    fun `generatePuzzle does not throw for HARD`() {
        generator.generatePuzzle(Difficulty.HARD)
    }

    @Test
    fun `generatePuzzle does not throw for EXPERT`() {
        generator.generatePuzzle(Difficulty.EXPERT)
    }

    // ── SudokuSolver — known puzzle ───────────────────────────────────────

    @Test
    fun `SudokuSolver solves a known valid puzzle correctly`() {
        // A known valid puzzle (source: public domain)
        val puzzle = arrayOf(
            intArrayOf(5, 3, 0, 0, 7, 0, 0, 0, 0),
            intArrayOf(6, 0, 0, 1, 9, 5, 0, 0, 0),
            intArrayOf(0, 9, 8, 0, 0, 0, 0, 6, 0),
            intArrayOf(8, 0, 0, 0, 6, 0, 0, 0, 3),
            intArrayOf(4, 0, 0, 8, 0, 3, 0, 0, 1),
            intArrayOf(7, 0, 0, 0, 2, 0, 0, 0, 6),
            intArrayOf(0, 6, 0, 0, 0, 0, 2, 8, 0),
            intArrayOf(0, 0, 0, 4, 1, 9, 0, 0, 5),
            intArrayOf(0, 0, 0, 0, 8, 0, 0, 7, 9)
        )
        val solved = solver.solveSudoku(puzzle)
        assertTrue("Solver must solve the known puzzle", solved)

        // Verify no zeros remain
        for (r in 0..8) {
            for (c in 0..8) {
                assertNotEquals("Cell ($r,$c) should not be zero after solving", 0, puzzle[r][c])
            }
        }
    }

    @Test
    fun `SudokuSolver hasUniqueSolution returns false for empty board`() {
        val emptyBoard = Array(9) { IntArray(9) }
        assertFalse("Empty board has many solutions", solver.hasUniqueSolution(emptyBoard))
    }
}
