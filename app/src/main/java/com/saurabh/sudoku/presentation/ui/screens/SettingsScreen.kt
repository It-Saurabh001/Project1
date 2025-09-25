package com.saurabh.sudoku.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saurabh.sudoku.presentation.ui.components.AboutItem
import com.saurabh.sudoku.presentation.ui.components.SettingsSection
import com.saurabh.sudoku.presentation.ui.components.SettingsSwitch
import com.saurabh.sudoku.presentation.ui.components.ThemeSelector
import com.saurabh.sudoku.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
    val autoNotes by viewModel.autoNotes.collectAsStateWithLifecycle()
    val showHints by viewModel.showHints.collectAsStateWithLifecycle()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsStateWithLifecycle()
    val highlightErrors by viewModel.highlightErrors.collectAsStateWithLifecycle()
    val autoSave by viewModel.autoSave.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .testTag("settings_screen")
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance Settings
            SettingsSection(title = "Appearance") {
                ThemeSelector(
                    selectedTheme = themeMode,
                    onThemeSelected = viewModel::setThemeMode
                )
            }

            // Gameplay Settings
            SettingsSection(title = "Gameplay") {
                SettingsSwitch(
                    title = "Auto Notes",
                    subtitle = "Automatically add/remove notes when placing numbers",
                    checked = autoNotes,
                    onCheckedChange = viewModel::setAutoNotes
                )

                SettingsSwitch(
                    title = "Show Hints",
                    subtitle = "Enable hint button during gameplay",
                    checked = showHints,
                    onCheckedChange = viewModel::setShowHints
                )

                SettingsSwitch(
                    title = "Highlight Errors",
                    subtitle = "Highlight conflicting numbers in red",
                    checked = highlightErrors,
                    onCheckedChange = viewModel::setHighlightErrors
                )

                SettingsSwitch(
                    title = "Auto Save",
                    subtitle = "Automatically save game progress",
                    checked = autoSave,
                    onCheckedChange = viewModel::setAutoSave
                )
            }

            // Audio & Haptics
            SettingsSection(title = "Audio & Haptics") {
                SettingsSwitch(
                    title = "Sound Effects",
                    subtitle = "Play sounds for moves and completion",
                    checked = soundEnabled,
                    onCheckedChange = viewModel::setSoundEnabled
                )

                SettingsSwitch(
                    title = "Vibration",
                    subtitle = "Vibrate on invalid moves and completion",
                    checked = vibrationEnabled,
                    onCheckedChange = viewModel::setVibrationEnabled
                )
            }

            // About Section
            SettingsSection(title = "About") {
                AboutItem(
                    title = "Version",
                    subtitle = "1.0.0"
                )

                AboutItem(
                    title = "Developer",
                    subtitle = "Sudoku Team"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "A modern, offline Sudoku game built with Jetpack Compose.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

