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
 * Componente que gestiona la navegación entre pantallas.
 * Escucha cambios de sesión y redirige cuando corresponde.
 * Define rutas públicas y protegidas, además de una ruta exclusiva para invitados.
 *
 * @param navController Controlador de navegación global.
 * @param modifier Modificador opcional para aplicar estilos al NavHost.
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val sessionViewModel: SesionViewModel = hiltViewModel()
    val sesionCerrada by sessionViewModel.sesionCerrada.collectAsState()

    // Redirige al login si la sesión ha sido cerrada
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
        // Públicas
        composable(Screens.Login.route) {
            LoginScreen(navController)
        }

        composable(Screens.Register.route) {
            RegisterScreen(navController)
        }

        // Protegidas con SesionGuard y MainScaffold
        composable(Screens.Home.route) {
            SesionGuard(navController = navController) { usuario ->
                MainScaffold(
                    navController = navController,
                    usuario = usuario,
                    sesionViewModel = sessionViewModel
                ) {
                    HomeScreen(navController)
                }
            }
        }

        composable(Screens.Clases.route) {
            SesionGuard(navController = navController) { usuario ->
                MainScaffold(
                    navController = navController,
                    usuario = usuario,
                    sesionViewModel = sessionViewModel
                ) {
                    ClaseListScreen(navController)
                }
            }
        }

        composable(Screens.Usuarios.route) {
            SesionGuard(navController = navController) { usuario ->
                MainScaffold(
                    navController = navController,
                    usuario = usuario,
                    sesionViewModel = sessionViewModel
                ) {
                    UserListScreen(navController)
                }
            }
        }

        composable(
            route = Screens.ClaseDetail.route,
            arguments = listOf(navArgument("claseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val claseId = backStackEntry.arguments?.getLong("claseId") ?: -1L
            SesionGuard(navController = navController) { usuario ->
                MainScaffold(
                    navController = navController,
                    usuario = usuario,
                    sesionViewModel = sessionViewModel
                ) {
                    ClaseDetailScreen(navController, claseId)
                }
            }
        }

        composable(Screens.Perfil.route) {
            SesionGuard(navController = navController) { usuario ->
                MainScaffold(
                    navController = navController,
                    usuario = usuario,
                    sesionViewModel = sessionViewModel
                ) {
                    ProfileScreen(navController)
                }
            }
        }

        composable(Screens.EditProfile.route) {
            SesionGuard(navController = navController) { usuario ->
                MainScaffold(
                    navController = navController,
                    usuario = usuario,
                    sesionViewModel = sessionViewModel
                ) {
                    EditProfileScreen(navController)
                }
            }
        }

        composable(
            route = "editar_usuario/{usuarioId}",
            arguments = listOf(navArgument("usuarioId") { type = NavType.LongType })
        ) { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getLong("usuarioId") ?: 0L
            SesionGuard(navController = navController) { usuario ->
                MainScaffold(
                    navController = navController,
                    usuario = usuario,
                    sesionViewModel = sessionViewModel
                ) {
                    EditUserScreen(usuarioId = usuarioId, navController = navController)
                }
            }
        }

        // Invitado
        composable("invitado_home") {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                com.example.bailotecaapp.ui.screens.invitado.InvitadoHomeScreen()
            } else {
                androidx.compose.material3.Text("Esta pantalla no es compatible con versiones anteriores a Android 8 (API 26).")
            }
        }
    }
}