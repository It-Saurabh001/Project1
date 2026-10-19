package com.saurabh.sudoku.domain.model

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [Notes].
 * Pure JVM — no Android dependencies.
 */
class NotesTest {

    private lateinit var notes: Notes

    // A blank 9x9 board (all zeros) used for autoRemoveNotes tests
    private val blankBoard = Array(9) { IntArray(9) }

    @Before
    fun setUp() {
        notes = Notes()
    }

    // ── Constructor ───────────────────────────────────────────────────────

    @Test
    fun `default constructor initialises all 81 cells to empty sets`() {
        for (r in 0..8) {
            for (c in 0..8) {
                assertTrue(
                    "Cell ($r,$c) should start empty",
                    notes.getNotes(r, c).isEmpty()
                )
            }
        }
    }

    // ── addNote ───────────────────────────────────────────────────────────

    @Test
    fun `addNote stores candidate at correct position`() {
        notes.addNote(3, 4, 7)
        assertTrue(notes.getNotes(3, 4).contains(7))
    }

    @Test
    fun `addNote with digit 1 is stored correctly`() {
        notes.addNote(0, 0, 1)
        assertTrue(notes.getNotes(0, 0).contains(1))
    }

    @Test
    fun `addNote with digit 9 is stored correctly`() {
        notes.addNote(8, 8, 9)
        assertTrue(notes.getNotes(8, 8).contains(9))
    }

    @Test
    fun `addNote ignores digit 0`() {
        notes.addNote(0, 0, 0)
        assertFalse(notes.getNotes(0, 0).contains(0))
    }

    @Test
    fun `addNote ignores digit 10`() {
        notes.addNote(0, 0, 10)
        assertFalse(notes.getNotes(0, 0).contains(10))
    }

    @Test
    fun `setting notes on one cell does not affect adjacent cells`() {
        notes.addNote(4, 4, 5)
        // Adjacent cells should remain empty
        assertTrue(notes.getNotes(4, 3).isEmpty())
        assertTrue(notes.getNotes(3, 4).isEmpty())
        assertTrue(notes.getNotes(4, 5).isEmpty())
        assertTrue(notes.getNotes(5, 4).isEmpty())
    }

    // ── removeNote ────────────────────────────────────────────────────────

    @Test
    fun `removeNote removes only the specified candidate`() {
        notes.addNote(2, 2, 5)
        notes.addNote(2, 2, 6)
        notes.removeNote(2, 2, 5)

        assertFalse(notes.getNotes(2, 2).contains(5))
        assertTrue(notes.getNotes(2, 2).contains(6))
    }

    @Test
    fun `removeNote on absent candidate is a no-op`() {
        notes.addNote(0, 0, 3)
        notes.removeNote(0, 0, 9)   // 9 was never added
        assertTrue(notes.getNotes(0, 0).contains(3))
    }

    // ── toggleNote ────────────────────────────────────────────────────────

    @Test
    fun `toggleNote adds candidate when absent`() {
        notes.toggleNote(1, 1, 4)
        assertTrue(notes.getNotes(1, 1).contains(4))
    }

    @Test
    fun `toggleNote removes candidate when present`() {
        notes.addNote(1, 1, 4)
        notes.toggleNote(1, 1, 4)
        assertFalse(notes.getNotes(1, 1).contains(4))
    }

    @Test
    fun `toggleNote twice results in same state as no operation`() {
        notes.toggleNote(5, 5, 8)
        notes.toggleNote(5, 5, 8)
        assertFalse(notes.getNotes(5, 5).contains(8))
    }

    // ── clearNotes ────────────────────────────────────────────────────────

    @Test
    fun `clearNotes removes all candidates from a cell`() {
        notes.addNote(6, 6, 1)
        notes.addNote(6, 6, 2)
        notes.addNote(6, 6, 9)
        notes.clearNotes(6, 6)

        assertTrue(notes.getNotes(6, 6).isEmpty())
    }

    @Test
    fun `clearNotes on empty cell is a no-op`() {
        notes.clearNotes(0, 0) // should not throw
        assertTrue(notes.getNotes(0, 0).isEmpty())
    }

    // ── getNotes ─────────────────────────────────────────────────────────

    @Test
    fun `getNotes returns empty set for pristine cell`() {
        assertTrue(notes.getNotes(7, 3).isEmpty())
    }

    @Test
    fun `getNotes returns all added candidates`() {
        notes.addNote(0, 0, 2)
        notes.addNote(0, 0, 4)
        notes.addNote(0, 0, 6)
        val result = notes.getNotes(0, 0)
        assertEquals(setOf(2, 4, 6), result)
    }

    // ── deepCopy ─────────────────────────────────────────────────────────

    @Test
    fun `deepCopy produces independent copy — mutations do not alias`() {
        notes.addNote(3, 3, 5)
        val copy = notes.deepCopy()

        // Mutate original
        notes.addNote(3, 3, 6)

        // Copy should remain unaffected
        assertFalse("Copy should not see mutations to the original", copy.getNotes(3, 3).contains(6))
        assertTrue("Copy must retain the original value", copy.getNotes(3, 3).contains(5))
    }

    @Test
    fun `deepCopy of empty Notes produces empty Notes`() {
        val copy = notes.deepCopy()
        for (r in 0..8) {
            for (c in 0..8) {
                assertTrue(copy.getNotes(r, c).isEmpty())
            }
        }
    }

    // ── autoRemoveNotes ───────────────────────────────────────────────────

    @Test
    fun `autoRemoveNotes clears number from entire row`() {
        for (c in 0..8) notes.addNote(2, c, 5)

        val board = blankBoard.map { it.clone() }.toTypedArray()
        notes.autoRemoveNotes(2, 4, 5, board)

        for (c in 0..8) {
            assertFalse(
                "Col $c in row 2 should no longer have 5",
                notes.getNotes(2, c).contains(5)
            )
        }
    }

    @Test
    fun `autoRemoveNotes clears number from entire column`() {
        for (r in 0..8) notes.addNote(r, 3, 7)

        val board = blankBoard.map { it.clone() }.toTypedArray()
        notes.autoRemoveNotes(5, 3, 7, board)

        for (r in 0..8) {
            assertFalse(
                "Row $r in col 3 should no longer have 7",
                notes.getNotes(r, 3).contains(7)
            )
        }
    }

    @Test
    fun `autoRemoveNotes clears number from 3x3 box`() {
        // Fill box (top-left) with candidate 3
        for (r in 0..2) for (c in 0..2) notes.addNote(r, c, 3)

        val board = blankBoard.map { it.clone() }.toTypedArray()
        notes.autoRemoveNotes(1, 1, 3, board)

        for (r in 0..2) {
            for (c in 0..2) {
                assertFalse("Box cell ($r,$c) should not have 3", notes.getNotes(r, c).contains(3))
            }
        }
    }

    @Test
    fun `autoRemoveNotes does not clear unrelated numbers`() {
        notes.addNote(0, 0, 4)  // different number
        notes.addNote(0, 1, 4)

        val board = blankBoard.map { it.clone() }.toTypedArray()
        notes.autoRemoveNotes(0, 5, 9, board)   // removing 9, not 4

        assertTrue("Unrelated candidate 4 should survive", notes.getNotes(0, 0).contains(4))
    }

    @Test
    fun `autoRemoveNotes skips cells that already have a value in board`() {
        // Place a value in board[2][5] so that cell is not empty
        val board = blankBoard.map { it.clone() }.toTypedArray()
        board[2][5] = 8

        notes.addNote(2, 5, 5)  // note on a "filled" board cell

        notes.autoRemoveNotes(2, 0, 5, board)

        // Cell (2,5) has board value 8 != 0 → autoRemoveNotes should skip it
        // The note should remain (the cell is considered non-empty)
        assertTrue(
            "Note on a filled board cell should not be touched by autoRemoveNotes",
            notes.getNotes(2, 5).contains(5)
        )
    }
}
