package com.example.bailotecaapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.bailotecaapp.ui.components.SesionGuard
import com.example.bailotecaapp.ui.screens.*

/**
 * Define las rutas y pantallas de la app.
 * Las rutas públicas son accesibles sin sesión activa.
 * Las rutas privadas están protegidas por el componente [SesionGuard].
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = Screens.Login.route
    ) {
        // Rutas públicas
        composable(Screens.Login.route) {
            LoginScreen(navController)
        }
        composable(Screens.Register.route) {
            RegisterScreen(navController)
        }

        // Rutas protegidas
        composable(Screens.Home.route) {
            SesionGuard(navController = navController) { usuario ->
                HomeScreen(navController)
            }
        }

        composable(Screens.Clases.route) {
            SesionGuard(navController = navController) { usuario ->
                ClaseListScreen(navController)
            }
        }

        composable(Screens.Usuarios.route) {
            SesionGuard(navController = navController) { usuario ->
                UserListScreen(navController)
            }
        }

        composable(
            route = Screens.ClaseDetail.route,
            arguments = listOf(navArgument("claseId") { defaultValue = -1L })
        ) { backStackEntry ->
            val claseId = backStackEntry.arguments?.getLong("claseId") ?: -1L
            SesionGuard(navController = navController) { usuario ->
                ClaseDetailScreen(navController, claseId)
            }
        }

        composable(Screens.Perfil.route) {
            SesionGuard(navController = navController) { usuario ->
                ProfileScreen(navController)
            }
        }

        composable(Screens.EditProfile.route) {
            SesionGuard(navController = navController) { usuario ->
                EditProfileScreen(navController)
            }
        }
    }
}