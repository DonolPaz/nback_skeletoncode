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
 * MainActivity serves as the entry point of the application.
 * It sets up the navigation between different screens and manages the primary GameViewModel.
 *
 * This activity handles navigation across the app’s three main screens:
 * - HomeScreen: Displays the high score and allows users to start a game or access settings.
 * - GameScreen: Hosts the main game logic, displaying either visual, audio, or audio-visual game types.
 * - SettingsScreen: Allows users to configure game settings like grid size, match percentage, etc.
 *
 * The app follows MVVM architecture, where this Activity instantiates and shares a single GameViewModel instance across screens.
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
                        // Home Screen composable with game stopping logic in MainActivity
                        composable(Screen.Home.route) {
                            // Stop any ongoing game before showing the HomeScreen
                            gameViewModel.stopGame()

                            HomeScreen(
                                vm = gameViewModel,
                                onStartGameClick = {
                                    navController.navigate(Screen.Game.createRoute(gameViewModel.getSelectedGameType().name))
                                },
                                onSettingsClick = {
                                    navController.navigate(Screen.Settings.route)
                                }
                            )
                        }

                        // Settings Screen composable
                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                vm = gameViewModel,
                                onSaveClick = { navController.navigate(Screen.Home.route) }
                            )
                        }

                        // Game Screen composable with gameType argument
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
                                onAudioMatchClick = { gameViewModel.checkAudioMatch() },
                                onHomeClick = {
                                    // Navigate back to Home Screen and clear back stack
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
