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
 * Define la navegación principal de la aplicación, incluyendo rutas públicas
 * (como Login o Registro) y rutas protegidas que requieren sesión activa y cargada.
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
        // Rutas públicas (sin necesidad de sesión cargada)
        composable(Screens.Login.route) {
            LoginScreen(navController)
        }
        composable(Screens.Register.route) {
            RegisterScreen(navController)
        }

        // Rutas protegidas con SesionGuard
        composable(Screens.Home.route) {
            SesionGuard { usuario ->
                HomeScreen(navController)
            }
        }

        composable(Screens.Clases.route) {
            SesionGuard { usuario ->
                ClaseListScreen(navController)
            }
        }

        composable(Screens.Usuarios.route) {
            SesionGuard { usuario ->
                UserListScreen(navController)
            }
        }

        composable(
            route = Screens.ClaseDetail.route,
            arguments = listOf(navArgument("claseId") { defaultValue = -1L })
        ) { backStackEntry ->
            val claseId = backStackEntry.arguments?.getLong("claseId") ?: -1L
            SesionGuard { usuario ->
                ClaseDetailScreen(navController, claseId)
            }
        }

        composable(Screens.Perfil.route) {
            SesionGuard { usuario ->
                ProfileScreen(navController)
            }
        }

        composable(Screens.EditProfile.route) {
            SesionGuard { usuario ->
                EditProfileScreen(navController)
            }
        }
    }
}