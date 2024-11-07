package mobappdev.example.nback_cimpl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import mobappdev.example.nback_cimpl.ui.screens.HomeScreen
import mobappdev.example.nback_cimpl.ui.theme.NBack_CImplTheme
import mobappdev.example.nback_cimpl.ui.viewmodels.GameVM
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mobappdev.example.nback_cimpl.ui.screens.GameScreen
import mobappdev.example.nback_cimpl.ui.screens.SettingsScreen
import mobappdev.example.nback_cimpl.ui.screens.navigation.Screen
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType

/**
 * This is the MainActivity of the application
 *
 * Your navigation between the two (or more) screens should be handled here
 * For this application you need at least a homescreen (a start is already made for you)
 * and a gamescreen (you will have to make yourself, but you can use the same viewmodel)
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NBack_CImplTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val gameViewModel: GameVM = viewModel(factory = GameVM.Factory)

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                vm = gameViewModel,
                                onStartGameClick = {
                                    navController.navigate(Screen.Game.createRoute(gameViewModel.getSelectedGameType().name))
                                },
                                onSettingsClick = {
                                    navController.navigate(Screen.Settings.route)
                                }
                            )
                            // Ensure we stop the game when navigating to Home
                            gameViewModel.stopGame()
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                vm = gameViewModel,
                                onSaveClick = { navController.navigate(Screen.Home.route) }
                            )
                        }
                        composable(
                            route = Screen.Game.ROUTE_WITH_ARGUMENT,
                            arguments = Screen.Game.arguments
                        ) { backStackEntry ->
                            val gameTypeString = backStackEntry.arguments?.getString("gameType")
                            val gameType = gameTypeString?.let { GameType.valueOf(it) } ?: GameType.Visual

                            GameScreen(
                                gameType = gameType,
                                vm = gameViewModel,
                                onMatchClick = { gameViewModel.checkMatch() },
                                onAudioMatchClick = { gameViewModel.checkAudioMatch() },  // Add this for audio match
                                onHomeClick = {
                                    // Navigate back to Home Screen
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                },
                                onPlayAgainClick = {
                                    // Restart the game with the same configuration
                                    gameViewModel.startGame(
                                        size = gameViewModel.size.value,
                                        combinations = gameViewModel.combinations.value,
                                        percentMatch = gameViewModel.percentMatch.value
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
