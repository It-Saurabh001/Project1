package com.saurabh.sudoku.data.local.database.dao

import androidx.room.Dao
import androidx.room.*
import com.saurabh.sudoku.data.local.database.entities.GameEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface GameDao {

    @Query("SELECT * FROM games WHERE state != 'COMPLETED' ORDER BY currentTime DESC LIMIT 1")
    suspend fun getCurrentGame(): GameEntity?

    @Query("SELECT * FROM games WHERE state != 'COMPLETED' ORDER BY currentTime DESC LIMIT 1")
    fun getCurrentGameFlow(): Flow<GameEntity?>

    @Query("SELECT * FROM games WHERE id = :gameId")
    suspend fun getGameById(gameId: String): GameEntity?

    @Query("SELECT * FROM games ORDER BY createdAt DESC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE state = 'COMPLETED' ORDER BY completedAt DESC LIMIT :limit")
    suspend fun getRecentCompletedGames(limit: Int): List<GameEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity)

    @Update
    suspend fun updateGame(game: GameEntity)

    @Delete
    suspend fun deleteGame(game: GameEntity)

    @Query("DELETE FROM games WHERE state = 'COMPLETED' AND completedAt < :timestamp")
    suspend fun deleteOldCompletedGames(timestamp: Long)

    @Query("SELECT COUNT(*) FROM games WHERE state = 'COMPLETED'")
    suspend fun getCompletedGamesCount(): Int
}