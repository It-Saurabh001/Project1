package com.saurabh.sudoku.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.saurabh.sudoku.data.local.database.dao.GameDao
import com.saurabh.sudoku.data.local.database.dao.PuzzleDao
import com.saurabh.sudoku.data.local.database.dao.StatisticsDao
import com.saurabh.sudoku.data.local.database.entities.GameEntity
import com.saurabh.sudoku.data.local.database.entities.PuzzleEntity
import com.saurabh.sudoku.data.local.database.entities.StatisticsEntity
import com.saurabh.sudoku.presentation.utils.Constants
import android.util.Log

@Database(
    entities = [
        GameEntity::class,
        StatisticsEntity::class,
        PuzzleEntity::class
    ],
    version = 4,  // Updated from 3 to 4
    exportSchema = false
)
abstract class SudokuDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun statisticsDao(): StatisticsDao
    abstract fun puzzleDao(): PuzzleDao

    companion object {
        private const val TAG = "SudokuDatabase"

        @Volatile
        private var INSTANCE: SudokuDatabase? = null

        // Migration from version 1 to 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                Log.d(TAG, "Migrating from version 1 to 2")
                db.execSQL("""
                    ALTER TABLE statistics 
                    ADD COLUMN lastPlayedDate TEXT DEFAULT NULL
                """)
            }
        }

        // Migration from version 2 to 3
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                Log.d(TAG, "Migrating from version 2 to 3")
                try {
                    // Add mistakes column
                    db.execSQL("ALTER TABLE games ADD COLUMN mistakes INTEGER NOT NULL DEFAULT 0")
                    Log.d(TAG, "Added mistakes column")

                    // Add maxMistakes column
                    db.execSQL("ALTER TABLE games ADD COLUMN maxMistakes INTEGER NOT NULL DEFAULT 3")
                    Log.d(TAG, "Added maxMistakes column")

                } catch (e: Exception) {
                    Log.e(TAG, "Migration error: ${e.message}")
                    throw e
                }
            }
        }

        // Migration from version 3 to 4 (NEW - for notes)
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                Log.d(TAG, "Migrating from version 3 to 4")
                try {
                    // Add notes column to games table
                    db.execSQL("ALTER TABLE games ADD COLUMN notes TEXT NOT NULL DEFAULT '{}'")
                    Log.d(TAG, "Added notes column")
                } catch (e: Exception) {
                    Log.e(TAG, "Migration error: ${e.message}")
                    throw e
                }
            }
        }

        fun getDatabase(context: Context): SudokuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SudokuDatabase::class.java,
                    Constants.DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}