package com.saurabh.sudoku.domain.repository

import com.saurabh.sudoku.domain.model.Game
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    suspend fun getCurrentGame(): Game?
    fun getCurrentGameFlow(): Flow<Game?>
    suspend fun getGameById(gameId: String): Game?
    suspend fun saveGame(game: Game)
    suspend fun updateGame(game: Game)
    suspend fun deleteGame(gameId: String)
    suspend fun getRecentCompletedGames(limit: Int): List<Game>
    fun getAllGames(): Flow<List<Game>>
    suspend fun getCompletedGamesCount(): Int
    suspend fun deleteOldCompletedGames(daysBefore: Int)
}