package com.saurabh.sudoku.domain.usecase

import com.saurabh.sudoku.domain.model.Game
import com.saurabh.sudoku.domain.repository.GameRepository
import javax.inject.Inject

class SaveGameUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(game: Game){
        gameRepository.updateGame(game)
    }
}