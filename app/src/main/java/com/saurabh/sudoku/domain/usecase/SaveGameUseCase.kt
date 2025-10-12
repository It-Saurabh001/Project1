package com.saurabh.sudoku.domain.usecase

import android.util.Log
import com.saurabh.sudoku.domain.model.Game
import com.saurabh.sudoku.domain.repository.GameRepository
import javax.inject.Inject

class SaveGameUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    private val TAG = "SudokuDebug_UseCase"
    suspend operator fun invoke(game: Game){
        Log.d(TAG, "SaveGameUseCase: Saving game ID ${game.id} with state ${game.state}")
        gameRepository.updateGame(game)
    }
}