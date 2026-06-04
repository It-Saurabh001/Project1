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
import com.saurabh.sudoku.presentation.utils.Constants
import com.saurabh.sudoku.presentation.utils.DateUtils
import com.saurabh.sudoku.presentation.utils.deepCopy
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
        .onEach { game ->
            android.util.Log.d("HomeViewModel",
                if (game == null) "currentGame → null (no Continue button will show)"
                else "currentGame → id=${game.id.take(8)}, state=${game.state}, difficulty=${game.difficulty}"
            )
        }
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
        android.util.Log.d("HomeViewModel", "onDifficultySelected() difficulty: $difficulty")
        _uiState.update { it.copy(selectedDifficulty = difficulty) }
    }

    fun startNewGame() {
        val difficulty = _uiState.value.selectedDifficulty
        android.util.Log.d("HomeViewModel", "startNewGame() START difficulty: $difficulty")

        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingPuzzle = true) }

            try {
                android.util.Log.d("HomeViewModel", "startNewGame() generating puzzle...")
                val board = sudokuGenerator.generatePuzzle(difficulty)
                android.util.Log.d("HomeViewModel", "startNewGame() puzzle generated")
                val currentTime = DateUtils.getCurrentTimestamp()
                val maxMistakes = Constants.getMaxMistakes(difficulty.name)
                val game = Game(
                    id = UUID.randomUUID().toString(),
                    board = board,
                    difficulty = difficulty,
                    startTime = currentTime,
                    currentTime = currentTime,
                    state = GameState.PLAYING,
                    hintsUsed = 0,
                    createdAt = currentTime,
                    initialBoard = board.deepCopy(),
                    mistakes = 0,
                    maxMistakes = maxMistakes
                )
                android.util.Log.d("HomeViewModel", "startNewGame() saving new game: ${game.id.take(8)}")
                gameRepository.saveGame(game)
                gameRepository.deleteAbandonedGames(game.id)

                _uiState.update {
                    it.copy(
                        isGeneratingPuzzle = false,
                        navigateToGame = true,
                        generatedGameId = game.id
                    )
                }
                android.util.Log.d("HomeViewModel", "startNewGame() SUCCESS")
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "startNewGame() FAILED: ${e.message}", e)
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
        android.util.Log.d("HomeViewModel", "continueCurrentGame() gameId: ${game?.id?.take(8)}")
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
        android.util.Log.d("HomeViewModel", "onNavigatedToGame() resetting navigation state")
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
