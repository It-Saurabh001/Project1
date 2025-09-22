package com.saurabh.sudoku.di

import com.saurabh.sudoku.data.repository.GameRepositoryImpl
import com.saurabh.sudoku.data.repository.PreferencesRepositoryImpl
import com.saurabh.sudoku.data.repository.StatisticsRepositoryImpl
import com.saurabh.sudoku.domain.repository.GameRepository
import com.saurabh.sudoku.domain.repository.PreferencesRepository
import com.saurabh.sudoku.domain.repository.StatisticsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindGameRepository(gameRepositoryImpl: GameRepositoryImpl): GameRepository

    @Binds
    @Singleton
    abstract fun bindStatisticsRepository(statisticsRepositoryImpl: StatisticsRepositoryImpl): StatisticsRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(preferencesRepositoryImpl: PreferencesRepositoryImpl): PreferencesRepository
}