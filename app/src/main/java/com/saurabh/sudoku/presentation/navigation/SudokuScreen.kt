package com.saurabh.sudoku.presentation.navigation

// Navigation Sealed Class for type safety
sealed class SudokuScreen(val route: String) {
    object Home : SudokuScreen("home")
    object Statistics : SudokuScreen("statistics")
    object Settings : SudokuScreen("settings")
    object Game : SudokuScreen("game/{gameId}") {
        fun createRoute(gameId: String) = "game/$gameId"
        val arguments = listOf(
            androidx.navigation.navArgument("gameId") {
                type = androidx.navigation.NavType.StringType
            }
        )
    }
}