package com.saurabh.sudoku.domain.usecase

import android.util.Log
import com.saurabh.sudoku.domain.model.Game
import com.saurabh.sudoku.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoadGameUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    private val TAG = "SudokuDebug_UseCase"
    suspend fun getCurrentGame(): Game? {
        Log.d(TAG, "LoadGameUseCase: Getting current game.")
        return gameRepository.getCurrentGame()
    }

    fun getCurrentGameFlow(): Flow<Game?> {
        Log.d(TAG, "LoadGameUseCase: Getting current game as a Flow.")
        return gameRepository.getCurrentGameFlow()
    }

    suspend fun getGameById(gameId: String): Game? {
        Log.d(TAG, "LoadGameUseCase: Getting game by ID: $gameId")
        return gameRepository.getGameById(gameId)
    }
}