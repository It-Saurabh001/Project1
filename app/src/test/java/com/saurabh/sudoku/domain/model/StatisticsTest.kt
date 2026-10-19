package com.saurabh.sudoku.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [Statistics] domain model.
 * Pure JVM — no Android dependencies.
 */
class StatisticsTest {

    // ── Default constructor ────────────────────────────────────────────────

    @Test
    fun `default Statistics has all numeric fields at zero`() {
        val stats = Statistics()
        assertEquals(0, stats.gamesCompleted)
        assertEquals(0L, stats.bestTimeEasy)
        assertEquals(0L, stats.bestTimeMedium)
        assertEquals(0L, stats.bestTimeHard)
        assertEquals(0L, stats.bestTimeExpert)
        assertEquals(0, stats.totalGamesPlayed)
        assertEquals(0, stats.gamesLost)
        assertEquals(0L, stats.totalPlayTime)
        assertEquals(0, stats.totalHintsUsed)
        assertEquals(0, stats.currentStreak)
        assertEquals(0, stats.longestStreak)
    }

    // ── getWinRatePercent ─────────────────────────────────────────────────

    @Test
    fun `winRate returns 0 when gamesPlayed is 0 to avoid division by zero`() {
        val stats = Statistics(gamesCompleted = 5, totalGamesPlayed = 0)
        assertEquals(0, stats.getWinRatePercent())
    }

    @Test
    fun `winRate calculates correctly for 7 wins out of 10 games`() {
        val stats = Statistics(gamesCompleted = 7, totalGamesPlayed = 10)
        assertEquals(70, stats.getWinRatePercent())
    }

    @Test
    fun `winRate is 100 for all wins`() {
        val stats = Statistics(gamesCompleted = 5, totalGamesPlayed = 5)
        assertEquals(100, stats.getWinRatePercent())
    }

    @Test
    fun `winRate is 0 for all losses`() {
        val stats = Statistics(gamesCompleted = 0, totalGamesPlayed = 10)
        assertEquals(0, stats.getWinRatePercent())
    }

    @Test
    fun `winRate rounds down for fractional percentages`() {
        // 1 win / 3 games = 33%
        val stats = Statistics(gamesCompleted = 1, totalGamesPlayed = 3)
        assertEquals(33, stats.getWinRatePercent())
    }

    // ── getAverageTime ────────────────────────────────────────────────────

    @Test
    fun `averageTime returns 0 when totalGamesPlayed is 0`() {
        val stats = Statistics(totalPlayTime = 60_000L, totalGamesPlayed = 0)
        assertEquals(0L, stats.getAverageTime())
    }

    @Test
    fun `averageTime divides totalPlayTime by totalGamesPlayed`() {
        val stats = Statistics(totalPlayTime = 90_000L, totalGamesPlayed = 3)
        assertEquals(30_000L, stats.getAverageTime())
    }

    @Test
    fun `averageTime truncates rather than rounding for non-integer result`() {
        val stats = Statistics(totalPlayTime = 100_000L, totalGamesPlayed = 3)
        // 100_000 / 3 = 33_333 (integer division)
        assertEquals(33_333L, stats.getAverageTime())
    }

    // ── getBestTime ───────────────────────────────────────────────────────

    @Test
    fun `getBestTime returns correct value for EASY`() {
        val stats = Statistics(bestTimeEasy = 120_000L)
        assertEquals(120_000L, stats.getBestTime(Difficulty.EASY))
    }

    @Test
    fun `getBestTime returns correct value for EXPERT`() {
        val stats = Statistics(bestTimeExpert = 600_000L)
        assertEquals(600_000L, stats.getBestTime(Difficulty.EXPERT))
    }

    // ── hasBestTime ───────────────────────────────────────────────────────

    @Test
    fun `hasBestTime returns false when bestTime is 0`() {
        val stats = Statistics()
        assertFalse(stats.hasBestTime(Difficulty.MEDIUM))
    }

    @Test
    fun `hasBestTime returns true when bestTime is greater than 0`() {
        val stats = Statistics(bestTimeMedium = 300_000L)
        assertTrue(stats.hasBestTime(Difficulty.MEDIUM))
    }

    // ── copy ──────────────────────────────────────────────────────────────

    @Test
    fun `copy correctly updates a single field without affecting others`() {
        val original = Statistics(gamesCompleted = 3, currentStreak = 5)
        val updated = original.copy(currentStreak = 6)

        assertEquals(6, updated.currentStreak)
        assertEquals(3, updated.gamesCompleted)   // unchanged
    }
}
