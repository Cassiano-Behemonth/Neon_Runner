package com.example.geometric_neon_runner.ui.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.geometric_neon_runner.data.repository.AuthRepository
import com.example.geometric_neon_runner.data.repository.ScoreRepository
import com.example.geometric_neon_runner.ui.screens.*
import com.example.geometric_neon_runner.ui.viewmodels.*

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Menu : Screen("menu")
    object ModeSelection : Screen("mode_selection")
    object Profile : Screen("profile")

    object Game : Screen("game/{mode}") {
        fun createRoute(mode: String) = "game/$mode"
    }

    object GameOver : Screen("gameover/{score}/{time}/{mode}") {
        fun createRoute(score: Int, time: Int, mode: String) = "gameover/$score/$time/$mode"
    }

    object Ranking : Screen("ranking/{mode}") {
        fun createRoute(mode: String = "NORMAL") = "ranking/$mode"
    }
}

class AppDependencies(
    val authRepository: AuthRepository,
    val scoreRepository: ScoreRepository
)

class ViewModelFactory<T>(private val creator: () -> T) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return creator() as T
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current.applicationContext

    // Criar dependências uma única vez
    val dependencies = AppDependencies(
        authRepository = AuthRepository(context),
        scoreRepository = ScoreRepository(context)
    )

    val startDestination = if (dependencies.authRepository.isUserLoggedIn()) {
        Screen.Menu.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            val viewModel: LoginViewModel = viewModel(
                factory = ViewModelFactory {
                    LoginViewModel(dependencies.authRepository)
                }
            )

            LoginScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(Screen.Register.route) {
            val viewModel: RegisterViewModel = viewModel(
                factory = ViewModelFactory {
                    RegisterViewModel(dependencies.authRepository)
                }
            )

            RegisterScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(Screen.Menu.route) {
            val viewModel: MenuViewModel = viewModel(
                factory = ViewModelFactory {
                    MenuViewModel(
                        dependencies.authRepository,
                        dependencies.scoreRepository
                    )
                }
            )

            MenuScreen(
                viewModel = viewModel,
                navController = navController
            )
        }

        composable(Screen.ModeSelection.route) {
            val viewModel: MenuViewModel = viewModel(
                factory = ViewModelFactory {
                    MenuViewModel(
                        dependencies.authRepository,
                        dependencies.scoreRepository
                    )
                }
            )

            ModeSelectionScreen(
                onModeSelected = { mode ->
                    navController.navigate(Screen.Game.createRoute(mode)) {
                        popUpTo(Screen.ModeSelection.route) { inclusive = true }
                    }
                },
                onBackPressed = {
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        }

        composable(Screen.Profile.route) {
            val viewModel: ProfileViewModel = viewModel(
                factory = ViewModelFactory {
                    ProfileViewModel(
                        dependencies.authRepository,
                        dependencies.scoreRepository
                    )
                }
            )

            ProfileScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(
            route = Screen.Game.route,
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "NORMAL"
            val viewModel: GameViewModel = viewModel(
                factory = ViewModelFactory {
                    GameViewModel(
                        dependencies.scoreRepository,
                        dependencies.authRepository
                    )
                }
            )

            GameScreen(
                navController = navController,
                viewModel = viewModel,
                mode = mode
            )
        }

        composable(
            route = Screen.GameOver.route,
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("time") { type = NavType.IntType },
                navArgument("mode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            val time = backStackEntry.arguments?.getInt("time") ?: 0
            val mode = backStackEntry.arguments?.getString("mode") ?: "NORMAL"

            GameOverScreen(
                navController = navController,
                score = score,
                timeSeconds = time,
                mode = mode
            )
        }

        composable(
            route = Screen.Ranking.route,
            arguments = listOf(
                navArgument("mode") {
                    type = NavType.StringType
                    defaultValue = "NORMAL"
                }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "NORMAL"
            val viewModel: RankingViewModel = viewModel(
                factory = ViewModelFactory {
                    RankingViewModel(
                        dependencies.scoreRepository,
                        dependencies.authRepository
                    )
                }
            )

            RankingScreen(
                navController = navController,
                vm = viewModel,
                initialMode = mode
            )
        }
    }
}