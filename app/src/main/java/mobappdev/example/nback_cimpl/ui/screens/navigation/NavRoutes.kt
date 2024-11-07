package mobappdev.example.nback_cimpl.ui.screens.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * Represents different navigation destinations (screens) within the app.
 * Each object within this sealed class defines a route for a specific screen.
 *
 * @property route The route string used for navigation.
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")

    /**
     * The Game screen route, which includes an argument for the game type.
     *
     * @property ROUTE_WITH_ARGUMENT Defines the route format with a gameType argument placeholder.
     * @property arguments Specifies the list of arguments required by this route, including
     * a "gameType" argument of type String.
     */
    object Game : Screen("game/{gameType}") {
        const val ROUTE_WITH_ARGUMENT = "game/{gameType}"
        val arguments = listOf(navArgument("gameType") { type = NavType.StringType })
        fun createRoute(gameType: String): String = "game/$gameType"
    }
}