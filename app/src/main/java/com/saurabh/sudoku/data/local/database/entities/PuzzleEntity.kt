package com.saurabh.sudoku.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "puzzles")
data class PuzzleEntity(
    @PrimaryKey val id: String,
    val puzzle: String,          // JSON representation of puzzle
    val solution: String,        // JSON representation of solution
    val difficulty: String,
    val createdAt: Long,
    val isUsed: Boolean = false
)

