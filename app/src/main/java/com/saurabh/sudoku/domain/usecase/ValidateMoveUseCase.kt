package com.saurabh.sudoku.domain.usecase

import android.util.Log
import com.saurabh.sudoku.domain.model.SudokuBoard
import com.saurabh.sudoku.utils.GameUtils
import javax.inject.Inject

class ValidateMoveUseCase @Inject constructor() {
    private val TAG = "SudokuDebug_UseCase"
    operator fun invoke(board: SudokuBoard, row: Int, col: Int, number: Int): ValidationResult {
        Log.d(TAG, "ValidateMoveUseCase: Validating number '$number' at ($row, $col)")
        // Check if it's an initial cell (can't be changed)
        if (board.isInitialCell(row, col)) {
            Log.d(TAG, "ValidateMoveUseCase: Invalid - Cannot change initial cell at ($row, $col).")
            return ValidationResult(false, "Cannot change initial cell")
        }

        // Check if the move is valid
        if (!GameUtils.isValidMove(board.board, row, col, number)) {
            Log.d(TAG, "ValidateMoveUseCase: Invalid - Move is not valid according to Sudoku rules.")
            return ValidationResult(false, "Invalid move")

        }
        Log.d(TAG, "ValidateMoveUseCase: Valid - Move is valid.")
        return ValidationResult(true, "Valid move")
    }

    data class ValidationResult(
        val isValid: Boolean,
        val message: String
    )
}