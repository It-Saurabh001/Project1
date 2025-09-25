package com.saurabh.sudoku.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.saurabh.sudoku.presentation.ui.screens.GameScreen
import com.saurabh.sudoku.presentation.ui.screens.HomeScreen
import com.saurabh.sudoku.presentation.ui.screens.SettingsScreen
import com.saurabh.sudoku.presentation.ui.screens.StatisticsScreen


@Composable
fun SudokuNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = SudokuScreen.Home.route,
        modifier = modifier
    ) {
        composable(route = SudokuScreen.Home.route) {
            HomeScreen(
                onNavigateToGame = { gameId ->
                    navController.navigate(SudokuScreen.Game.createRoute(gameId))
                },
                onNavigateToStatistics = {
                    navController.navigate(SudokuScreen.Statistics.route)
                },
                onNavigateToSettings = {
                    navController.navigate(SudokuScreen.Settings.route)
                }
            )
        }

        composable(
            route = SudokuScreen.Game.route,
            arguments = SudokuScreen.Game.arguments
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: return@composable
            GameScreen(
                gameId = gameId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = SudokuScreen.Statistics.route) {
            StatisticsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = SudokuScreen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}