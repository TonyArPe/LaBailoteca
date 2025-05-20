package com.example.bailotecaapp.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.bailotecaapp.ui.components.SesionGuard
import com.example.bailotecaapp.ui.screens.*
import com.example.bailotecaapp.viewmodel.SesionViewModel

/**
 * Define la navegación principal de la aplicación, incluyendo rutas públicas
 * (como Login o Registro) y rutas protegidas que requieren sesión activa y cargada.
 *
 * @param navController Controlador de navegación.
 * @param modifier Modificador opcional para la vista de navegación.
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val sessionViewModel: SesionViewModel = hiltViewModel()
    val sesionCerrada by sessionViewModel.sesionCerrada.collectAsState()

    // Redirige al login automáticamente si se ha cerrado la sesión
    LaunchedEffect(sesionCerrada) {
        if (sesionCerrada) {
            navController.navigate(Screens.Login.route) {
                popUpTo(0) { inclusive = true }
            }
            sessionViewModel.reiniciarEstadoSesion()
        }
    }

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

        // Rutas protegidas con SesionGuard
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
            arguments = listOf(navArgument("claseId") { type = NavType.LongType })
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

        composable(
            route = "editar_usuario/{usuarioId}",
            arguments = listOf(navArgument("usuarioId") { type = NavType.LongType })
        ) { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getLong("usuarioId") ?: 0L
            SesionGuard(navController = navController) { usuario ->
                EditUserScreen(usuarioId = usuarioId, navController = navController)
            }
        }
    }
}