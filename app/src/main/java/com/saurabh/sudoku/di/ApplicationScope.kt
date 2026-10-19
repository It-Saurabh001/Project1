package com.saurabh.sudoku.di

import javax.inject.Qualifier

/**
 * Qualifier that distinguishes the application-level [kotlinx.coroutines.CoroutineScope]
 * (backed by a [kotlinx.coroutines.SupervisorJob] tied to the process lifetime)
 * from any other CoroutineScope that might be injected.
 *
 * NOTE: This is a @Qualifier, NOT a @Scope. The lifecycle is controlled by
 * @Singleton on the provider in [ApplicationModule].
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope