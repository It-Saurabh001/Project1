package com.saurabh.sudoku.domain.model

enum class Difficulty(val displayName: String, val clues: Int) {
    EASY("Easy", 40),
    MEDIUM("Medium", 35),
    HARD("Hard", 28),
    EXPERT("Expert", 22);

    companion object {
        fun fromString(name: String): Difficulty {
            return values().find { it.name == name } ?: EASY
        }
    }
}