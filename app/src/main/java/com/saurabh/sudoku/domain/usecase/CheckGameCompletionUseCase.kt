package com.saurabh.sudoku.domain.usecase

import android.util.Log
import com.saurabh.sudoku.domain.model.Game
import com.saurabh.sudoku.domain.model.GameState
import com.saurabh.sudoku.domain.repository.StatisticsRepository
import com.saurabh.sudoku.presentation.utils.DateUtils
import com.saurabh.sudoku.presentation.utils.GameUtils
import javax.inject.Inject

class CheckGameCompletionUseCase @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) {
    private val TAG = "SudokuDebug_UseCase"
    suspend operator fun invoke(game: Game): CompletionResult {
        Log.d(TAG, "CheckGameCompletionUseCase: Checking game ID ${game.id}")
        val isComplete = GameUtils.isBoardComplete(game.board.board)

        if (isComplete && game.state != GameState.COMPLETED) {
            Log.d(TAG, "CheckGameCompletionUseCase: Game ${game.id} is complete!")
            val completedTime = DateUtils.getCurrentTimestamp()
            val gameTime = completedTime - game.startTime

            // Update statistics
            statisticsRepository.recordGameCompleted(
                difficulty = game.difficulty,
                gameTime = gameTime,
                hintsUsed = game.hintsUsed
            )
            Log.d(TAG, "CheckGameCompletionUseCase: Statistics updated for game ${game.id}.")

            val completedGame = game.copy(
                state = GameState.COMPLETED,
                completedAt = completedTime,
                currentTime = completedTime
            )
            Log.d(TAG, "CheckGameCompletionUseCase: Game ${game.id} is not yet complete.")
            return CompletionResult(
                isCompleted = true,
                updatedGame = completedGame,
                completionTime = gameTime
            )
        }

        return CompletionResult(
            isCompleted = false,
            updatedGame = game,
            completionTime = null
        )
    }

    data class CompletionResult(
        val isCompleted: Boolean,
        val updatedGame: Game,
        val completionTime: Long?
    )


}