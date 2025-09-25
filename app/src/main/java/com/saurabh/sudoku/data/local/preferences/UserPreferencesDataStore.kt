package com.saurabh.sudoku.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.saurabh.sudoku.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.PREFERENCES_NAME
)

class UserPreferencesDataStore(private val context: Context) {

    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val AUTO_NOTES = booleanPreferencesKey("auto_notes")
        val SHOW_HINTS = booleanPreferencesKey("show_hints")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val HIGHLIGHT_ERRORS = booleanPreferencesKey("highlight_errors")
        val AUTO_SAVE = booleanPreferencesKey("auto_save")
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "system"
    }

    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SOUND_ENABLED] ?: true
    }

    val autoNotes: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_NOTES] ?: false
    }

    val showHints: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_HINTS] ?: true
    }

    val vibrationEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[VIBRATION_ENABLED] ?: true
    }

    val highlightErrors: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HIGHLIGHT_ERRORS] ?: true
    }

    val autoSave: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_SAVE] ?: true
    }

    suspend fun updateThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun updateSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SOUND_ENABLED] = enabled
        }
    }

    suspend fun updateAutoNotes(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_NOTES] = enabled
        }
    }

    suspend fun updateShowHints(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_HINTS] = enabled
        }
    }

    suspend fun updateVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun updateHighlightErrors(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HIGHLIGHT_ERRORS] = enabled
        }
    }

    suspend fun updateAutoSave(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_SAVE] = enabled
        }
    }
}