package com.example.bailotecaapp.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.ui.components.SesionGuard
import com.example.bailotecaapp.ui.screens.clases.*
import com.example.bailotecaapp.ui.screens.enumscreens.MainScreen
import com.example.bailotecaapp.ui.screens.invitado.InvitadoHomeScreen
import com.example.bailotecaapp.ui.screens.login.LoginScreen
import com.example.bailotecaapp.ui.screens.login.RegisterScreen
import com.example.bailotecaapp.ui.screens.usuarios.perfil.EditProfileScreen
import com.example.bailotecaapp.ui.screens.usuarios.UsuarioDetalleScreen
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.example.bailotecaapp.viewmodel.ThemeViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    themeViewModel: ThemeViewModel
) {
    val sesionViewModel: SesionViewModel = hiltViewModel()
    val sesionCerrada by sesionViewModel.sesionCerrada.collectAsState()
    val usuario by sesionViewModel.usuario.collectAsState()
    val cargado by sesionViewModel.yaCargado.collectAsState()
    val invitado by sesionViewModel.modoInvitado.collectAsState()

    LaunchedEffect(sesionCerrada) {
        if (sesionCerrada) {
            Log.d("AppNavigation", "🔐 Sesión cerrada, navegando a login")
            navController.navigate(Screens.Login.route) {
                popUpTo(0) { inclusive = true }
            }
            sesionViewModel.reiniciarEstadoSesion()
        }
    }

    val startDestination = when {
        cargado && usuario?.rol != Rol.INVITADO -> Screens.Home.route
        cargado && usuario?.rol == Rol.INVITADO -> Screens.InvitadoHome.route
        else -> Screens.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screens.Login.route) {
            Log.d("AppNavigation", "📍 Login")
            LoginScreen(navController)
        }

        composable(Screens.Register.route) {
            Log.d("AppNavigation", "📍 Registro")
            RegisterScreen(navController)
        }

        composable(Screens.Home.route) {
            Log.d("AppNavigation", "📍 Home")
            SesionGuard(navController = navController, sesionViewModel = sesionViewModel) { usuario ->
                MainScaffold(
                    globalNavController = navController,
                    usuario = usuario,
                    sesionViewModel = sesionViewModel,
                    currentScreen = MainScreen.HOME,
                    onNavigate = {},
                    themeViewModel = themeViewModel
                )
            }
        }

        composable(Screens.Clases.route) {
            Log.d("AppNavigation", "📍 Lista de Clases")
            SesionGuard(navController = navController, sesionViewModel = sesionViewModel) { usuario ->
                MainScaffold(
                    globalNavController = navController,
                    usuario = usuario,
                    sesionViewModel = sesionViewModel,
                    currentScreen = MainScreen.CLASES,
                    onNavigate = {},
                    themeViewModel = themeViewModel
                )
            }
        }

        composable(
            route = "usuario_detalle/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: return@composable
            UsuarioDetalleScreen(userId = id, navController)
        }

        composable(Screens.Eventos.route) {
            Log.d("AppNavigation", "📍 Lista de Eventos")
            SesionGuard(navController = navController, sesionViewModel = sesionViewModel) { usuario ->
                MainScaffold(
                    globalNavController = navController,
                    usuario = usuario,
                    sesionViewModel = sesionViewModel,
                    currentScreen = MainScreen.EVENTOS,
                    onNavigate = {},
                    themeViewModel = themeViewModel
                )
            }
        }

        composable(Screens.Usuarios.route) {
            Log.d("AppNavigation", "📍 Usuarios")
            SesionGuard(navController = navController, sesionViewModel = sesionViewModel) { usuario ->
                MainScaffold(
                    globalNavController = navController,
                    usuario = usuario,
                    sesionViewModel = sesionViewModel,
                    currentScreen = MainScreen.USUARIOS,
                    onNavigate = {},
                    themeViewModel = themeViewModel
                )
            }
        }

        composable(Screens.Perfil.route) {
            Log.d("AppNavigation", "📍 Perfil")
            SesionGuard(navController = navController, sesionViewModel = sesionViewModel) { usuario ->
                MainScaffold(
                    globalNavController = navController,
                    usuario = usuario,
                    sesionViewModel = sesionViewModel,
                    currentScreen = MainScreen.PERFIL,
                    onNavigate = {},
                    themeViewModel = themeViewModel
                )
            }
        }

        composable(Screens.EditProfile.route) {
            SesionGuard(navController = navController, sesionViewModel = sesionViewModel) { usuario ->
                EditProfileScreen(
                    navController = navController,
                    sesionViewModel = sesionViewModel
                )
            }
        }

        composable(
            route = Screens.ClaseDetail.route,
            arguments = listOf(navArgument("claseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val claseId = backStackEntry.arguments?.getLong("claseId") ?: return@composable
            Log.d("AppNavigation", "📍 Detalle clase ID: $claseId")

            SesionGuard(navController = navController, sesionViewModel = sesionViewModel) { usuario ->
                if (usuario.rol == Rol.PROFESOR) {
                    ClaseDetailProfesorScreen(navController, claseId)
                } else {
                    ClaseDetailScreen(navController, claseId)
                }
            }
        }

        composable("crearEditarClase") {
            Log.d("AppNavigation", "📍 Crear nueva clase")
            CrearEditarClaseScreen(navController = navController)
        }

        composable(
            "crearEditarClase/{claseId}",
            arguments = listOf(navArgument("claseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val claseId = backStackEntry.arguments?.getLong("claseId") ?: return@composable
            Log.d("AppNavigation", "📍 Editar clase ID: $claseId")
            CrearEditarClaseScreen(navController = navController, claseId = claseId)
        }

        composable(Screens.InvitadoHome.route) {
            Log.d("AppNavigation", "📍 Home invitado")
            InvitadoHomeScreen(
                navController = navController,
                sesionViewModel = hiltViewModel(),
                claseViewModel = hiltViewModel(),
                eventoViewModel = hiltViewModel()
            )
        }
    }
}