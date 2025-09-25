package com.saurabh.sudoku.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.sudoku.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel(){

    val themeMode = preferencesRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "system"
        )

    val soundEnabled = preferencesRepository.soundEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val autoNotes = preferencesRepository.autoNotes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val showHints = preferencesRepository.showHints
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val vibrationEnabled = preferencesRepository.vibrationEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val highlightErrors = preferencesRepository.highlightErrors
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val autoSave = preferencesRepository.autoSave
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun setThemeMode(mode: String) = viewModelScope.launch {
        preferencesRepository.updateThemeMode(mode)
    }
    fun setSoundEnabled(enabled : Boolean) = viewModelScope.launch { preferencesRepository.updateSoundEnabled(enabled) }

    fun setAutoNotes(enabled: Boolean) = viewModelScope.launch { preferencesRepository.updateAutoNotes(enabled) }

    fun setShowHints(enabled: Boolean) = viewModelScope.launch { preferencesRepository.updateShowHints(enabled) }

    fun setVibrationEnabled(enabled: Boolean) = viewModelScope.launch { preferencesRepository.updateVibrationEnabled(enabled) }

    fun setHighlightErrors(enabled: Boolean) =  viewModelScope.launch { preferencesRepository.updateHighlightErrors(enabled) }

    fun setAutoSave(enabled: Boolean)= viewModelScope.launch { preferencesRepository.updateAutoSave(enabled) }

}