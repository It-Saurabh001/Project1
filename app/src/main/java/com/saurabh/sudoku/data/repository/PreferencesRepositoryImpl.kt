package com.saurabh.sudoku.data.repository

import android.util.Log
import com.saurabh.sudoku.data.local.preferences.UserPreferencesDataStore
import com.saurabh.sudoku.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val preferencesDataStore: UserPreferencesDataStore
): PreferencesRepository {
    private val TAG = "PreferencesRepo"
    override val themeMode: Flow<String> = preferencesDataStore.themeMode
    override val soundEnabled: Flow<Boolean> = preferencesDataStore.soundEnabled
    override val autoNotes: Flow<Boolean> = preferencesDataStore.autoNotes
    override val showHints: Flow<Boolean> = preferencesDataStore.showHints
    override val vibrationEnabled: Flow<Boolean> = preferencesDataStore.vibrationEnabled
    override val highlightErrors: Flow<Boolean> = preferencesDataStore.highlightErrors
    override val autoSave: Flow<Boolean> = preferencesDataStore.autoSave
    override val notesModeEnabled: Flow<Boolean> = preferencesDataStore.notesModeEnabled

    override suspend fun updateThemeMode(mode: String) {
        Log.d(TAG, "updateThemeMode: $mode")
        preferencesDataStore.updateThemeMode(mode)
    }

    override suspend fun updateSoundEnabled(enabled: Boolean) {
        Log.d(TAG, "updateSoundEnabled: $enabled")
        preferencesDataStore.updateSoundEnabled(enabled)
    }

    override suspend fun updateAutoNotes(enabled: Boolean) {
        Log.d(TAG, "updateAutoNotes: $enabled")
        preferencesDataStore.updateAutoNotes(enabled)
    }

    override suspend fun updateShowHints(enabled: Boolean) {
        Log.d(TAG, "updateShowHints: $enabled")
        preferencesDataStore.updateShowHints(enabled)
    }

    override suspend fun updateVibrationEnabled(enabled: Boolean) {
        Log.d(TAG, "updateVibrationEnabled: $enabled")
        preferencesDataStore.updateVibrationEnabled(enabled)
    }

    override suspend fun updateHighlightErrors(enabled: Boolean) {
        Log.d(TAG, "updateHighlightErrors: $enabled")
        preferencesDataStore.updateHighlightErrors(enabled)
    }

    override suspend fun updateAutoSave(enabled: Boolean) {
        Log.d(TAG, "updateAutoSave: $enabled")
        preferencesDataStore.updateAutoSave(enabled)
    }
    override suspend fun updateNotesModeEnabled(enabled: Boolean) {
        Log.d(TAG, "updateNotesModeEnabled: $enabled")
        preferencesDataStore.updateNotesModeEnabled(enabled)
    }
}