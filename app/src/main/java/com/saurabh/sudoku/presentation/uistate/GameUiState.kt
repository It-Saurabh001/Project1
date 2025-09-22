package com.saurabh.sudoku.presentation.uistate

import com.saurabh.sudoku.domain.model.Game
import com.saurabh.sudoku.domain.usecase.GetHintUseCase

data class GameUiState(
    val game: Game? = null,
    val selectedCell: Pair<Int, Int>? = null,
    val conflictCells: Set<Pair<Int, Int>> = emptySet(),
    val showCompletionDialog: Boolean = false,
    val completionTime: Long? = null,
    val showHint: GetHintUseCase.Hint? = null,
    val hintMessage: String? = null,
    val isLoading: Boolean = true,
    val isGeneratingNewGame: Boolean = false,
    val error: String? = null
)
