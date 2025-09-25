package com.saurabh.sudoku.data.local.database.dao
import androidx.room.*
import com.saurabh.sudoku.data.local.database.entities.PuzzleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PuzzleDao {

    @Query("SELECT * FROM puzzles WHERE difficulty = :difficulty AND isUsed = 0 ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomUnusedPuzzle(difficulty: String): PuzzleEntity?

    @Query("SELECT * FROM puzzles WHERE difficulty = :difficulty ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getPuzzlesByDifficulty(difficulty: String, limit: Int): List<PuzzleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPuzzle(puzzle: PuzzleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPuzzles(puzzles: List<PuzzleEntity>)

    @Query("UPDATE puzzles SET isUsed = 1 WHERE id = :puzzleId")
    suspend fun markPuzzleAsUsed(puzzleId: String)

    @Query("SELECT COUNT(*) FROM puzzles WHERE difficulty = :difficulty AND isUsed = 0")
    suspend fun getUnusedPuzzleCount(difficulty: String): Int

    @Query("DELETE FROM puzzles WHERE isUsed = 1 AND createdAt < :timestamp")
    suspend fun deleteOldUsedPuzzles(timestamp: Long)
}