package com.saurabh.sudoku.domain.usecase

import com.saurabh.sudoku.domain.model.SudokuBoard
import com.saurabh.sudoku.presentation.utils.GameUtils
import javax.inject.Inject

/**
 * Validates a number placement against two criteria:
 *
 * 1. **Solution correctness** – is this the right answer for this cell?
 *    → If wrong: increment mistakes, still place the number (visible mistake).
 *
 * 2. **Erase (number = 0)** – always valid, never a mistake.
 *
 * Note: we deliberately DO NOT check Sudoku rules (isValidMove) here —
 * duplicate detection is handled separately in GameUtils.computeAllConflicts(),
 * which highlights conflicts visually. Mistake counting is purely
 * "did you place the correct answer?".
 */
class ValidateMoveUseCase @Inject constructor() {

    operator fun invoke(
        board: SudokuBoard,
        row: Int,
        col: Int,
        number: Int,
        currentMistakes: Int,
        maxMistakes: Int
    ): ValidationResult {
        // Erasing is always valid — never a mistake
        if (number == 0) {
            return ValidationResult(
                isValid = true,
                canPlaceNumber = true,
                message = "Erased",
                newMistakes = currentMistakes,
                isGameOver = false
            )
        }

        // Cannot change initial (clue) cells
        if (board.isInitialCell(row, col)) {
            return ValidationResult(
                isValid = false,
                canPlaceNumber = false,
                message = "Cannot change initial cell",
                newMistakes = currentMistakes,
                isGameOver = false
            )
        }

        // Check against the solution — this is the correct mistake definition
        val correctAnswer = board.solution[row][col]
        val isCorrect = (number == correctAnswer)

        if (!isCorrect) {
            val newMistakes = currentMistakes + 1
            val isGameOver = newMistakes >= maxMistakes
            val remaining = maxMistakes - newMistakes

            return ValidationResult(
                isValid = false,
                canPlaceNumber = true,  // still show the number so the user sees their mistake
                message = if (isGameOver)
                    "Game Over! Too many mistakes!"
                else
                    "Wrong number! $remaining ${if (remaining == 1) "mistake" else "mistakes"} remaining",
                newMistakes = newMistakes,
                isGameOver = isGameOver
            )
        }

        // Correct placement
        return ValidationResult(
            isValid = true,
            canPlaceNumber = true,
            message = "Correct!",
            newMistakes = currentMistakes,
            isGameOver = false
        )
    }

    data class ValidationResult(
        val isValid: Boolean,           // Is the number correct per solution?
        val canPlaceNumber: Boolean,    // Should we visually place the number?
        val message: String,            // Toast/error message
        val newMistakes: Int,           // Updated mistake count
        val isGameOver: Boolean         // Has mistake limit been reached?
    )
}