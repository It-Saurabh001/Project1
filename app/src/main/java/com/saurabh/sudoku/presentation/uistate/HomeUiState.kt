package com.saurabh.sudoku.presentation.uistate

import com.saurabh.sudoku.domain.model.Difficulty

data class HomeUiState(
    val selectedDifficulty: Difficulty = Difficulty.EASY,
    val isGeneratingPuzzle: Boolean = false,
    val navigateToGame: Boolean = false,
    val generatedGameId: String? = null,
    val error: String? = null
)
