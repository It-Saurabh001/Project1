package com.saurabh.sudoku.presentation.navigation

import android.util.Log
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
    val TAG = "SudokuNavigation"
    Log.d(TAG, "SudokuNavigation composable called")
    NavHost(
        navController = navController,
        startDestination = SudokuScreen.Home.route,
        modifier = modifier
    ) {
        composable(route = SudokuScreen.Home.route) {
            Log.d(TAG, "Navigating to HomeScreen")
            HomeScreen(
                onNavigateToGame = { gameId ->
                    Log.d(TAG, "Navigating to GameScreen with gameId: $gameId")
                    navController.navigate(SudokuScreen.Game.createRoute(gameId))
                },
                onNavigateToStatistics = {
                    Log.d(TAG, "Navigating to StatisticsScreen")
                    navController.navigate(SudokuScreen.Statistics.route)
                },
                onNavigateToSettings = {
                    Log.d(TAG, "Navigating to SettingsScreen")
                    navController.navigate(SudokuScreen.Settings.route)
                }
            )
        }

        composable(
            route = SudokuScreen.Game.route,
            arguments = SudokuScreen.Game.arguments
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: return@composable
            Log.d(TAG, "Navigating to GameScreen with gameId: $gameId")
            GameScreen(
                gameId = gameId,
                onNavigateBack = {
                    Log.d(TAG, "Navigating back from GameScreen")
                    navController.popBackStack()
                }
            )
        }

        composable(route = SudokuScreen.Statistics.route) {
            Log.d(TAG, "Navigating to StatisticsScreen")
            StatisticsScreen(
                onNavigateBack = {
                    Log.d(TAG, "Navigating back from StatisticsScreen")
                    navController.popBackStack()
                }
            )
        }

        composable(route = SudokuScreen.Settings.route) {
            Log.d(TAG, "Navigating to SettingsScreen")
            SettingsScreen(
                onNavigateBack = {
                    Log.d(TAG, "Navigating back from SettingsScreen")
                    navController.popBackStack()
                }
            )
        }
    }
}