package com.example.bailotecaapp.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.bailotecaapp.ui.screens.*
import com.example.bailotecaapp.ui.screens.clases.ClaseDetailProfesorScreen
import com.example.bailotecaapp.ui.screens.clases.ClaseDetailScreen
import com.example.bailotecaapp.ui.screens.clases.ClaseListScreen
import com.example.bailotecaapp.ui.screens.clases.CrearEditarClaseScreen
import com.example.bailotecaapp.ui.screens.enumscreens.MainScreen
import com.example.bailotecaapp.ui.screens.invitado.InvitadoHomeScreen
import com.example.bailotecaapp.ui.screens.login.LoginScreen
import com.example.bailotecaapp.ui.screens.login.RegisterScreen
import com.example.bailotecaapp.ui.screens.perfil.ProfileScreen
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
    val yaCargado by sesionViewModel.yaCargado.collectAsState(initial = false)
    var currentScreen by remember { mutableStateOf(MainScreen.HOME) }

    // 🚫 Si aún no se ha cargado la sesión, no inicies navegación
    if (!yaCargado) {
        Box(modifier = Modifier.fillMaxSize()) { /* Carga inicial silenciosa */ }
        return
    }

    val startDestination = remember(usuario) {
        if (usuario != null) "main" else Screens.Login.route
    }

    LaunchedEffect(sesionCerrada) {
        if (sesionCerrada) {
            Log.d("AppNavigation", "🔐 Sesión cerrada, redirigiendo al login")
            navController.navigate(Screens.Login.route) {
                popUpTo(0) { inclusive = true }
            }
            sesionViewModel.reiniciarEstadoSesion()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screens.Login.route) {
            LoginScreen(navController)
        }

        composable(Screens.Register.route) {
            RegisterScreen(navController)
        }

        // Pantalla con scaffold y navegación protegida
        composable("main") {
            SesionGuard(navController = navController, sesionViewModel = sesionViewModel) { usuario ->
                MainScaffold(
                    globalNavController = navController,
                    usuario = usuario,
                    sesionViewModel = sesionViewModel,
                    currentScreen = currentScreen,
                    onNavigate = { screen -> currentScreen = screen }
                )
            }
        }

        // Rutas del drawer
        composable(Screens.Home.route) {
            HomeScreen(navController, sesionViewModel)
        }
        composable(Screens.Clases.route) {
            ClaseListScreen(navController, hiltViewModel(), sesionViewModel)
        }
        composable(Screens.Usuarios.route) {
            UserListScreen(navController, hiltViewModel())
        }
        composable(Screens.Perfil.route) {
            ProfileScreen(navController, sesionViewModel)
        }

        composable(
            route = Screens.ClaseDetail.route,
            arguments = listOf(navArgument("claseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val claseId = backStackEntry.arguments?.getLong("claseId") ?: return@composable
            SesionGuard(navController = navController, sesionViewModel = sesionViewModel) { usuario ->
                if (usuario.rol == Rol.PROFESOR) {
                    ClaseDetailProfesorScreen(navController, claseId)
                } else {
                    ClaseDetailScreen(navController, claseId)
                }
            }
        }

        composable("crearEditarClase") {
            CrearEditarClaseScreen(navController = navController)
        }

        composable(
            "crearEditarClase/{claseId}",
            arguments = listOf(navArgument("claseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val claseId = backStackEntry.arguments?.getLong("claseId") ?: return@composable
            CrearEditarClaseScreen(navController = navController, claseId = claseId)
        }

        // Modo invitado
        composable(Screens.InvitadoHome.route) {
            InvitadoHomeScreen(
                navController = navController,
                sesionViewModel = hiltViewModel(),
                claseViewModel = hiltViewModel(),
                eventoViewModel = hiltViewModel()
            )
        }
    }
}