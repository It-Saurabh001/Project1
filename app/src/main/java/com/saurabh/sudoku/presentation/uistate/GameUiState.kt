package com.saurabh.sudoku.presentation.uistate

import com.saurabh.sudoku.domain.model.Game
import com.saurabh.sudoku.presentation.utils.GameUtils

data class GameUiState(
    val game: Game? = null,
    val selectedCell: Pair<Int, Int>? = null,
    val conflictCells: Set<Pair<Int, Int>> = emptySet(),
    val numberCounts: Map<Int, Int> = emptyMap(),
    val showCompletionDialog: Boolean = false,
    val completionTime: Long? = null,
    val showHint: GameUtils.Hint? = null,
    val hintMessage: String? = null,
    val isLoading: Boolean = true,
    val isGeneratingNewGame: Boolean = false,
    val error: String? = null
)
