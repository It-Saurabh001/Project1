package com.saurabh.sudoku.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.sudoku.data.generator.SudokuGenerator
import com.saurabh.sudoku.domain.model.Difficulty
import com.saurabh.sudoku.domain.model.Game
import com.saurabh.sudoku.domain.model.GameState
import com.saurabh.sudoku.domain.model.Statistics
import com.saurabh.sudoku.domain.repository.GameRepository
import com.saurabh.sudoku.domain.repository.StatisticsRepository
import com.saurabh.sudoku.presentation.uistate.HomeUiState
import com.saurabh.sudoku.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val statisticsRepository: StatisticsRepository,
    private val sudokuGenerator: SudokuGenerator
): ViewModel(){
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val currentGame: StateFlow<Game?> = gameRepository.getCurrentGameFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val statistics: StateFlow<Statistics> = statisticsRepository.getStatisticsFlow()
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
                // Logic from GeneratePuzzleUseCase
                val board = sudokuGenerator.generatePuzzle(difficulty)
                val currentTime = DateUtils.getCurrentTimestamp()
                val game = Game(
                    id = UUID.randomUUID().toString(),
                    board = board,
                    difficulty = difficulty,
                    startTime = currentTime,
                    currentTime = currentTime,
                    state = GameState.PLAYING,
                    hintsUsed = 0,
                    createdAt = currentTime
                )
                gameRepository.saveGame(game)
                // ---

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
