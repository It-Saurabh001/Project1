package com.saurabh.sudoku.domain.usecase

import com.saurabh.sudoku.domain.model.Game
import com.saurabh.sudoku.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoadGameUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {

    suspend fun getCurrentGame(): Game? {
        return gameRepository.getCurrentGame()
    }

    fun getCurrentGameFlow(): Flow<Game?> {
        return gameRepository.getCurrentGameFlow()
    }

    suspend fun getGameById(gameId: String): Game? {
        return gameRepository.getGameById(gameId)
    }
}