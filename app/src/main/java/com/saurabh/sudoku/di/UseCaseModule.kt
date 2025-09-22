package com.saurabh.sudoku.di

import SudokuGenerator
import com.saurabh.sudoku.data.generator.SudokuSolver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    @Singleton
    fun provideSudokuGenerator(): SudokuGenerator {
        return SudokuGenerator()
    }

    @Provides
    @Singleton
    fun provideSudokuSolver(): SudokuSolver {
        return SudokuSolver()
    }
}
