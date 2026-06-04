package com.saurabh.sudoku.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.saurabh.sudoku.data.local.database.dao.GameDao
import com.saurabh.sudoku.data.local.database.entities.GameEntity
import com.saurabh.sudoku.domain.model.Difficulty
import com.saurabh.sudoku.domain.model.Game
import com.saurabh.sudoku.domain.model.GameState
import com.saurabh.sudoku.domain.model.Notes
import com.saurabh.sudoku.domain.model.SudokuBoard
import com.saurabh.sudoku.domain.repository.GameRepository
import com.saurabh.sudoku.presentation.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val gameDao : GameDao,
    private val gson: Gson
): GameRepository{
    override suspend fun getCurrentGame(): Game? {
        val game = gameDao.getCurrentGame()?.toDomainModel()
        android.util.Log.d("GameRepository", "getCurrentGame() → ${game?.id?.take(8) ?: "null"}")
        return game
    }

    override fun getCurrentGameFlow(): Flow<Game?> {
        return gameDao.getCurrentGameFlow().map { entity ->
            val game = entity?.toDomainModel()
            android.util.Log.d("GameRepository",
                if (game == null) "getCurrentGameFlow → null (no active game)"
                else "getCurrentGameFlow → id=${game.id.take(8)}, state=${game.state}, difficulty=${game.difficulty}, createdAt=${game.createdAt}"
            )
            game
        }
    }

    override suspend fun getGameById(gameId: String): Game? {
        val game = gameDao.getGameById(gameId)?.toDomainModel()
        android.util.Log.d("GameRepository", "getGameById($gameId) → ${game?.id?.take(8) ?: "null"}")
        return game
    }

    override suspend fun saveGame(game: Game) {
        android.util.Log.d("GameRepository", "saveGame() id=${game.id.take(8)}")
        gameDao.insertGame(game.toEntity())
    }

    override suspend fun updateGame(game: Game) {
        android.util.Log.v("GameRepository", "updateGame() id=${game.id.take(8)}, state=${game.state}, mistakes=${game.mistakes}")
        gameDao.updateGame(game.toEntity())
    }

    override suspend fun deleteGame(gameId: String) {
        android.util.Log.d("GameRepository", "deleteGame($gameId)")
        val game = gameDao.getGameById(gameId)
        if (game != null) {
            gameDao.deleteGame(game)
        }
    }

    override suspend fun getRecentCompletedGames(limit: Int): List<Game> {
        return gameDao.getRecentCompletedGames(limit).map { it.toDomainModel() }
    }

    override fun getAllGames(): Flow<List<Game>> {
        return gameDao.getAllGames().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getCompletedGamesCount(): Int {
        return gameDao.getCompletedGamesCount()
    }
    override suspend fun deleteOldCompletedGames(daysBefore: Int) {
        val timestamp = DateUtils.getCurrentTimestamp() - (daysBefore * 24 * 60 * 60 * 1000L)
        gameDao.deleteOldCompletedGames(timestamp)
    }

    private fun GameEntity.toDomainModel(): Game {
        val boardType = object : TypeToken<Array<IntArray>>() {}.type
        val notesType = object : TypeToken<Notes>() {}.type

        return Game(
            id = id,
            board = SudokuBoard(
                board = gson.fromJson(board, boardType),
                solution = gson.fromJson(solution, boardType),
                initialBoard = gson.fromJson(initialBoard, boardType)
            ),
            initialBoard = SudokuBoard(
                board = gson.fromJson(initialBoard, boardType),
                solution = gson.fromJson(solution, boardType),
                initialBoard = gson.fromJson(initialBoard, boardType)
            ),
            difficulty = Difficulty.fromString(difficulty),
            startTime = startTime,
            currentTime = currentTime,
            state = GameState.valueOf(state),
            hintsUsed = hintsUsed,
            createdAt = createdAt,
            completedAt = completedAt,
            mistakes = mistakes,
            maxMistakes = maxMistakes,
            notes = gson.fromJson(notes, notesType) ?: Notes()

        )
    }

    private fun Game.toEntity(): GameEntity {
        return GameEntity(
            id = id,
            board = gson.toJson(board.board),
            solution = gson.toJson(board.solution),
            initialBoard = gson.toJson(board.initialBoard),
            difficulty = difficulty.name,
            startTime = startTime,
            currentTime = currentTime,
            state = state.name,
            hintsUsed = hintsUsed,
            createdAt = createdAt,
            completedAt = completedAt,
            mistakes = mistakes,
            maxMistakes = maxMistakes,
            notes = gson.toJson(notes)
        )
    }
}