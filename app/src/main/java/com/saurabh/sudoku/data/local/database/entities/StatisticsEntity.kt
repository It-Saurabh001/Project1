package com.saurabh.sudoku.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "statistics")
data class StatisticsEntity(
    @PrimaryKey val id: Int = 1,

    // ── Win-only metrics ──────────────────────────────────────────────────────
    val gamesCompleted: Int = 0,        // Wins only
    val bestTimeEasy: Long = 0L,        // Best win time, Easy
    val bestTimeMedium: Long = 0L,      // Best win time, Medium
    val bestTimeHard: Long = 0L,        // Best win time, Hard
    val bestTimeExpert: Long = 0L,      // Best win time, Expert

    // ── All-game metrics ──────────────────────────────────────────────────────
    val totalGamesPlayed: Int = 0,      // Wins + Losses
    val gamesLost: Int = 0,             // Losses only
    val totalPlayTime: Long = 0L,       // Cumulative time across all games
    val totalHintsUsed: Int = 0,        // Cumulative hints across all games
    val lastPlayedDate: String? = null, // Updated on every game end

    // ── Streak (win-based) ────────────────────────────────────────────────────
    val currentStreak: Int = 0,         // Consecutive wins; reset to 0 on loss
    val longestStreak: Int = 0          // All-time max streak
)
