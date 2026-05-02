package com.saurabh.sudoku.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "statistics")
data class StatisticsEntity(
    @PrimaryKey val id: Int = 1,
    val gamesCompleted: Int = 0,
    val totalPlayTime: Long = 0L,
    val bestTimeEasy: Long = 0L,
    val bestTimeMedium: Long = 0L,
    val bestTimeHard: Long = 0L,
    val bestTimeExpert: Long = 0L,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalHintsUsed: Int = 0,
    val lastPlayedDate: String? = null
)
