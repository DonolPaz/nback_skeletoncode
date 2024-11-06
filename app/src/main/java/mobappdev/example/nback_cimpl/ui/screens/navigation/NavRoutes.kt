package mobappdev.example.nback_cimpl.ui.screens.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")

    object Game : Screen("game/{gameType}") {
        const val ROUTE_WITH_ARGUMENT = "game/{gameType}"
        val arguments = listOf(navArgument("gameType") { type = NavType.StringType })
        fun createRoute(gameType: String): String = "game/$gameType"
    }
}