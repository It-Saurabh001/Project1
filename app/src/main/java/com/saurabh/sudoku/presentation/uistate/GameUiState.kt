package com.saurabh.sudoku.presentation.uistate

import com.saurabh.sudoku.domain.model.Game
import com.saurabh.sudoku.domain.model.Notes
import com.saurabh.sudoku.presentation.utils.Constants
import com.saurabh.sudoku.presentation.utils.GameUtils

data class GameUiState(
    val game: Game? = null,
    val selectedCell: Pair<Int, Int>? = null,
    val conflictCells: Set<Pair<Int, Int>> = emptySet(),
    /** Cells in the same row/col/box as the selected cell — highlighted in soft blue */
    val highlightedCells: Set<Pair<Int, Int>> = emptySet(),
    /** Cells that hold the same number as the selected cell — highlighted in accent blue */
    val sameNumberCells: Set<Pair<Int, Int>> = emptySet(),
    val numberCounts: Map<Int, Int> = (1..9).associateWith { Constants.GRID_SIZE },
    val isLoading: Boolean = true,
    val isGeneratingNewGame: Boolean = false,
    val showCompletionDialog: Boolean = false,
    val completionTime: Long? = null,
    val showHint: GameUtils.Hint? = null,
    val hintMessage: String? = null,
    val error: String? = null,
    val mistakes: Int = 0,
    val maxMistakes: Int = 3,
    val showGameOverDialog: Boolean = false,
    val mistakeMessage: String? = null,
    val showMistakeToast: Boolean = false,
    val isNotesMode: Boolean = false,
    val notes: Notes = Notes(),
    /** Row just played a valid number (triggers scale animation) */
    val lastPlacedCell: Pair<Int, Int>? = null,
    /** Row just got a mistake (triggers shake animation) */
    val lastMistakeCell: Pair<Int, Int>? = null,
)