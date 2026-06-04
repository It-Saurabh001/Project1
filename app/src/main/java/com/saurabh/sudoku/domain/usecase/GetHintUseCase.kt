package com.saurabh.sudoku.domain.usecase

import android.util.Log
import com.saurabh.sudoku.domain.model.Game
import com.saurabh.sudoku.presentation.utils.Constants
import javax.inject.Inject
import kotlin.random.Random

class GetHintUseCase @Inject constructor() {
    operator fun invoke(game: Game): HintResult {
        val TAG = "GetHintUseCase"
        Log.d(TAG, "invoke() gameId=${game.id.take(8)}, hintsUsed=${game.hintsUsed}/${Constants.MAX_HINTS}")
        if (game.hintsUsed >= Constants.MAX_HINTS) {
            Log.d(TAG, "No more hints available")
            return HintResult(null, "No more hints available")
        }

        val board = game.board
        val emptyCells = mutableListOf<Pair<Int, Int>>()

        // Find all empty cells
        for (row in 0..8) {
            for (col in 0..8) {
                if (board.getCurrentValue(row, col) == 0) {
                    emptyCells.add(Pair(row, col))
                }
            }
        }
        Log.d(TAG, "Found ${emptyCells.size} empty cells")

        if (emptyCells.isEmpty()) {
            Log.d(TAG, "No empty cells")
            return HintResult(null, "No empty cells")
        }

        // Choose a random empty cell
        val (row, col) = emptyCells[Random.nextInt(emptyCells.size)]
        val hintValue = board.getSolutionValue(row, col)
        Log.d(TAG, "Selected hint: ($row, $col) = $hintValue")

        return HintResult(
            hint = Hint(row, col, hintValue),
            message = "Hint: Place $hintValue in row ${row + 1}, column ${col + 1}"
        )
    }

    data class HintResult(
        val hint: Hint?,
        val message: String
    )

    data class Hint(
        val row: Int,
        val col: Int,
        val value: Int
    )
}