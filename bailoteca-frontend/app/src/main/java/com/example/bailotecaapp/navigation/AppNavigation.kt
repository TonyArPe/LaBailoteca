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
import com.example.bailotecaapp.network.FirebaseUrlProvider
import com.example.bailotecaapp.ui.components.SesionGuard
import com.example.bailotecaapp.ui.screens.*
import com.example.bailotecaapp.ui.screens.clases.*
import com.example.bailotecaapp.ui.screens.enumscreens.MainScreen
import com.example.bailotecaapp.ui.screens.eventos.*
import com.example.bailotecaapp.ui.screens.invitado.InvitadoHomeScreen
import com.example.bailotecaapp.ui.screens.login.LoginScreen
import com.example.bailotecaapp.ui.screens.login.RegisterScreen
import com.example.bailotecaapp.ui.screens.usuarios.ClasesUsuarioScreen
import com.example.bailotecaapp.ui.screens.usuarios.UsuarioDetalleScreen
import com.example.bailotecaapp.ui.screens.usuarios.perfil.EditProfileScreen
import com.example.bailotecaapp.viewmodel.EventoViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.example.bailotecaapp.viewmodel.ThemeViewModel

/**
 * Encargado de definir y controlar toda la navegación principal de la app.
 *
 * Incluye control de sesión y redirección inicial según el rol del usuario
 * y permite usar una pantalla de carga (`SplashScreen`) para sincronizar Firebase.
 *
 * @param navController controlador de navegación de Compose
 * @param modifier modificador externo
 * @param themeViewModel viewmodel que gestiona el tema actual
 * @param urlProvider proveedor de la URL base remota
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    themeViewModel: ThemeViewModel,
    urlProvider: FirebaseUrlProvider
) {
    val sesionViewModel: SesionViewModel = hiltViewModel()
    val sesionCerrada by sesionViewModel.sesionCerrada.collectAsState()
    val usuario by sesionViewModel.usuario.collectAsState()
    val yaCargado by sesionViewModel.usuarioYaCargado.collectAsState()
    val invitado by sesionViewModel.modoInvitado.collectAsState()

    LaunchedEffect(sesionCerrada) {
        if (sesionCerrada) {
            Log.d("AppNavigation", "🔐 Sesión cerrada, navegando a login")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
            sesionViewModel.reiniciarEstadoSesion()
        }
    }

    // Primera pantalla: siempre iniciamos en SplashScreen
    val startDestination = "splash"

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // 🌊 SplashScreen inicial
        composable("splash") {
            SplashScreen(navController = navController, urlProvider = urlProvider)
        }

        composable("login") {
            Log.d("AppNavigation", "📍 Login")
            LoginScreen(navController)
        }

        composable("register") {
            Log.d("AppNavigation", "📍 Registro")
            RegisterScreen(navController)
        }

        composable("home") {
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

        composable("clases") {
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

        composable("usuario_detalle/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) {
            val id = it.arguments?.getLong("id") ?: return@composable
            UsuarioDetalleScreen(userId = id, navController)
        }

        composable("eventos") {
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

        composable("usuarios") {
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

        composable("perfil") {
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

        composable("editar_perfil") {
            SesionGuard(navController = navController, sesionViewModel = sesionViewModel) {
                EditProfileScreen(navController, sesionViewModel)
            }
        }

        composable("clasesUsuario/{id}") {
            val id = it.arguments?.getString("id")?.toLongOrNull() ?: return@composable
            ClasesUsuarioScreen(userId = id, navController = navController)
        }

        composable("clase/{claseId}", arguments = listOf(navArgument("claseId") { type = NavType.LongType })) {
            val claseId = it.arguments?.getLong("claseId") ?: return@composable
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
            CrearEditarClaseScreen(navController)
        }

        composable("crearEditarClase/{claseId}", arguments = listOf(navArgument("claseId") { type = NavType.LongType })) {
            val claseId = it.arguments?.getLong("claseId") ?: return@composable
            CrearEditarClaseScreen(navController, claseId)
        }

        composable("evento_list") {
            val usuario = sesionViewModel.usuario.collectAsState().value
            val eventoViewModel = hiltViewModel<EventoViewModel>()
            if (usuario != null) {
                EventoListScreen(navController, usuario, eventoViewModel)
            }
        }

        composable("evento/{id}") {
            EventoDetailScreen(navController)
        }

        composable("crear_evento") {
            CrearEditarEventoScreen(navController)
        }

        composable("invitado_home") {
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