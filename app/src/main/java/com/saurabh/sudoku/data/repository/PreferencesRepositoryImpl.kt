package com.saurabh.sudoku.data.repository

import com.saurabh.sudoku.data.local.preferences.UserPreferencesDataStore
import com.saurabh.sudoku.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val preferencesDataStore: UserPreferencesDataStore
): PreferencesRepository {
    override val themeMode: Flow<String> = preferencesDataStore.themeMode
    override val soundEnabled: Flow<Boolean> = preferencesDataStore.soundEnabled
    override val autoNotes: Flow<Boolean> = preferencesDataStore.autoNotes
    override val showHints: Flow<Boolean> = preferencesDataStore.showHints
    override val vibrationEnabled: Flow<Boolean> = preferencesDataStore.vibrationEnabled
    override val highlightErrors: Flow<Boolean> = preferencesDataStore.highlightErrors
    override val autoSave: Flow<Boolean> = preferencesDataStore.autoSave

    override suspend fun updateThemeMode(mode: String) {
        preferencesDataStore.updateThemeMode(mode)
    }

    override suspend fun updateSoundEnabled(enabled: Boolean) {
        preferencesDataStore.updateSoundEnabled(enabled)
    }

    override suspend fun updateAutoNotes(enabled: Boolean) {
        preferencesDataStore.updateAutoNotes(enabled)
    }

    override suspend fun updateShowHints(enabled: Boolean) {
        preferencesDataStore.updateShowHints(enabled)
    }

    override suspend fun updateVibrationEnabled(enabled: Boolean) {
        preferencesDataStore.updateVibrationEnabled(enabled)
    }

    override suspend fun updateHighlightErrors(enabled: Boolean) {
        preferencesDataStore.updateHighlightErrors(enabled)
    }

    override suspend fun updateAutoSave(enabled: Boolean) {
        preferencesDataStore.updateAutoSave(enabled)
    }
}