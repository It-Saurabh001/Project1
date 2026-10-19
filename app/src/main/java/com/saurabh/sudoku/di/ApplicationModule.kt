package com.saurabh.sudoku.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    /**
     * Provides a process-lifetime [CoroutineScope] for fire-and-forget work that must
     * outlive a ViewModel (e.g. auto-pausing a game in GameViewModel.onCleared).
     *
     * @Singleton  — one shared instance inside SingletonComponent (required for
     *               bindings inside a @Singleton-scoped component).
     * @ApplicationScope — @Qualifier that distinguishes it from other CoroutineScope bindings.
     * Dispatchers.Default — keeps DB/IO work off the Main thread.
     */
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}