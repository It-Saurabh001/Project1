package com.saurabh.sudoku.domain.usecase

import com.saurabh.sudoku.domain.model.SudokuBoard
import com.saurabh.sudoku.utils.GameUtils
import javax.inject.Inject

class ValidateMoveUseCase @Inject constructor() {

    operator fun invoke(board: SudokuBoard, row: Int, col: Int, number: Int): ValidationResult {
        // Check if it's an initial cell (can't be changed)
        if (board.isInitialCell(row, col)) {
            return ValidationResult(false, "Cannot change initial cell")
        }

        // Check if the move is valid
        if (!GameUtils.isValidMove(board.board, row, col, number)) {
            return ValidationResult(false, "Invalid move")
        }

        return ValidationResult(true, "Valid move")
    }

    data class ValidationResult(
        val isValid: Boolean,
        val message: String
    )
}