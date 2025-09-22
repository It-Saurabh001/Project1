package com.saurabh.sudoku.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "statistics")
data class StatisticsEntity(
    @PrimaryKey val id: Int = 1,
    val gamesCompleted: Int = 0,
    val totalPlayTime: Long = 0L,
    val bestTimeEasy: Long = Long.MAX_VALUE,
    val bestTimeMedium: Long = Long.MAX_VALUE,
    val bestTimeHard: Long = Long.MAX_VALUE,
    val bestTimeExpert: Long = Long.MAX_VALUE,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalHintsUsed: Int = 0
)
