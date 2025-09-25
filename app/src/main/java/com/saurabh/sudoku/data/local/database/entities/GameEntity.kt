package com.saurabh.sudoku.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val id: String,
    val board: String,           // JSON representation of current board
    val solution: String,        // JSON representation of solution
    val initialBoard: String,    // JSON representation of initial puzzle
    val difficulty: String,
    val startTime: Long,
    val currentTime: Long,
    val state: String,
    val hintsUsed: Int,
    val createdAt: Long,
    val completedAt: Long?
)
