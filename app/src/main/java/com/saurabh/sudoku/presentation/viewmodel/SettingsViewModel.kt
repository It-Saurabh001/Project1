package com.saurabh.sudoku.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.sudoku.domain.repository.PreferencesRepository
import com.saurabh.sudoku.presentation.utils.HapticFeedbackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val hapticFeedbackManager: HapticFeedbackManager
) : ViewModel() {

    val themeMode = preferencesRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val soundEnabled = preferencesRepository.soundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val autoNotes = preferencesRepository.autoNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val showHints = preferencesRepository.showHints
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val vibrationEnabled = preferencesRepository.vibrationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val highlightErrors = preferencesRepository.highlightErrors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val autoSave = preferencesRepository.autoSave
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val notesModeEnabled = preferencesRepository.notesModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setThemeMode(mode: String) = viewModelScope.launch {
        android.util.Log.d("SettingsViewModel", "setThemeMode: $mode")
        preferencesRepository.updateThemeMode(mode)
    }

    fun setSoundEnabled(enabled: Boolean) = viewModelScope.launch {
        android.util.Log.d("SettingsViewModel", "setSoundEnabled: $enabled")
        preferencesRepository.updateSoundEnabled(enabled)
    }

    fun setAutoNotes(enabled: Boolean) = viewModelScope.launch {
        android.util.Log.d("SettingsViewModel", "setAutoNotes: $enabled")
        preferencesRepository.updateAutoNotes(enabled)
    }

    fun setShowHints(enabled: Boolean) = viewModelScope.launch {
        android.util.Log.d("SettingsViewModel", "setShowHints: $enabled")
        preferencesRepository.updateShowHints(enabled)
    }

    fun setVibrationEnabled(enabled: Boolean) = viewModelScope.launch {
        android.util.Log.d("SettingsViewModel", "setVibrationEnabled: $enabled")
        preferencesRepository.updateVibrationEnabled(enabled)
        if (enabled) hapticFeedbackManager.performVibration()
    }

    fun setHighlightErrors(enabled: Boolean) = viewModelScope.launch {
        android.util.Log.d("SettingsViewModel", "setHighlightErrors: $enabled")
        preferencesRepository.updateHighlightErrors(enabled)
    }

    fun setAutoSave(enabled: Boolean) = viewModelScope.launch {
        android.util.Log.d("SettingsViewModel", "setAutoSave: $enabled")
        preferencesRepository.updateAutoSave(enabled)
    }

    fun setNotesModeEnabled(enabled: Boolean) = viewModelScope.launch {
        android.util.Log.d("SettingsViewModel", "setNotesModeEnabled: $enabled")
        preferencesRepository.updateNotesModeEnabled(enabled)
    }
}