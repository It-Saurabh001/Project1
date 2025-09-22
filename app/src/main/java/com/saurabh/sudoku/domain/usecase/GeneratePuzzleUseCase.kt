package com.saurabh.sudoku.domain.usecase

import SudokuGenerator
import com.saurabh.sudoku.domain.model.Difficulty
import com.saurabh.sudoku.domain.model.Game
import com.saurabh.sudoku.domain.model.GameState
import com.saurabh.sudoku.domain.repository.GameRepository
import com.saurabh.sudoku.utils.DateUtils
import java.util.UUID
import javax.inject.Inject

class GeneratePuzzleUseCase @Inject constructor(
    private val sudokuGenerator: SudokuGenerator,
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(difficulty: Difficulty): Game {
        val board = sudokuGenerator.generatePuzzle(difficulty)
        val currentTime = DateUtils.getCurrentTimestamp()
        val gameId = UUID.randomUUID().toString()

        val game = Game(
            id = gameId,
            board = board,
            difficulty = difficulty,
            startTime = currentTime,
            currentTime = currentTime,
            state = GameState.NEW,
            hintsUsed = 0,
            createdAt = currentTime
        )

        gameRepository.saveGame(game)
        return game
    }

}