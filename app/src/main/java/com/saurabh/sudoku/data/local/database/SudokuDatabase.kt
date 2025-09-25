package com.saurabh.sudoku.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.saurabh.sudoku.data.local.database.dao.GameDao
import com.saurabh.sudoku.data.local.database.dao.PuzzleDao
import com.saurabh.sudoku.data.local.database.dao.StatisticsDao
import com.saurabh.sudoku.data.local.database.entities.GameEntity
import com.saurabh.sudoku.data.local.database.entities.PuzzleEntity
import com.saurabh.sudoku.data.local.database.entities.StatisticsEntity
import com.saurabh.sudoku.utils.Constants

@Database(
    entities = [
        GameEntity::class,
        StatisticsEntity::class,
        PuzzleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SudokuDatabase : RoomDatabase(){
    abstract fun gameDao(): GameDao
    abstract fun statisticsDao(): StatisticsDao
    abstract fun puzzleDao(): PuzzleDao

    companion object {
        @Volatile
        private var INSTANCE: SudokuDatabase? = null

        fun getDatabase(context: Context): SudokuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SudokuDatabase::class.java,
                    Constants.DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}