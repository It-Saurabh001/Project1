package com.saurabh.sudoku.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.sudoku.domain.model.Difficulty
import com.saurabh.sudoku.domain.model.Game
import com.saurabh.sudoku.domain.model.Statistics
import com.saurabh.sudoku.domain.usecase.GeneratePuzzleUseCase
import com.saurabh.sudoku.domain.usecase.GetStatisticsUseCase
import com.saurabh.sudoku.domain.usecase.LoadGameUseCase
import com.saurabh.sudoku.presentation.uistate.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val generatePuzzleUseCase: GeneratePuzzleUseCase,
    private val loadGameUseCase: LoadGameUseCase,
    private val getStatisticsUseCase: GetStatisticsUseCase
): ViewModel(){
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val currentGame: StateFlow<Game?> = loadGameUseCase.getCurrentGameFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val statistics: StateFlow<Statistics> = getStatisticsUseCase.getStatisticsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Statistics()
        )

    fun onDifficultySelected(difficulty: Difficulty) {
        _uiState.update { it.copy(selectedDifficulty = difficulty) }
    }

    fun startNewGame() {
        val difficulty = _uiState.value.selectedDifficulty

        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingPuzzle = true) }

            try {
                val game = generatePuzzleUseCase(difficulty)
                _uiState.update {
                    it.copy(
                        isGeneratingPuzzle = false,
                        navigateToGame = true,
                        generatedGameId = game.id
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGeneratingPuzzle = false,
                        error = "Failed to generate puzzle: ${e.message}"
                    )
                }
            }
        }
    }

    fun continueCurrentGame() {
        val game = currentGame.value
        if (game != null) {
            _uiState.update {
                it.copy(
                    navigateToGame = true,
                    generatedGameId = game.id
                )
            }
        }
    }

    fun onNavigatedToGame() {
        _uiState.update {
            it.copy(
                navigateToGame = false,
                generatedGameId = null
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}