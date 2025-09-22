package com.saurabh.sudoku.domain.usecase

import com.saurabh.sudoku.domain.model.Game
import com.saurabh.sudoku.domain.model.GameState
import com.saurabh.sudoku.domain.repository.StatisticsRepository
import com.saurabh.sudoku.utils.DateUtils
import com.saurabh.sudoku.utils.GameUtils
import javax.inject.Inject

class CheckGameCompletionUseCase @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) {
    suspend operator fun invoke(game: Game): CompletionResult {
        val isComplete = GameUtils.isBoardComplete(game.board.board)

        if (isComplete && game.state != GameState.COMPLETED) {
            val completedTime = DateUtils.getCurrentTimestamp()
            val gameTime = completedTime - game.startTime

            // Update statistics
            statisticsRepository.recordGameCompleted(
                difficulty = game.difficulty,
                gameTime = gameTime,
                hintsUsed = game.hintsUsed
            )

            val completedGame = game.copy(
                state = GameState.COMPLETED,
                completedAt = completedTime,
                currentTime = completedTime
            )

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