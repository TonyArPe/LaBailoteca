package com.example.bailotecaapp.navigation

import ThemeToggleButton
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.ui.components.DrawerContent
import com.example.bailotecaapp.ui.screens.clases.ClaseListScreen
import com.example.bailotecaapp.ui.screens.HomeScreen
import com.example.bailotecaapp.ui.screens.perfil.ProfileScreen
import com.example.bailotecaapp.ui.screens.UserListScreen
import com.example.bailotecaapp.ui.screens.enumscreens.MainScreen
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.example.bailotecaapp.viewmodel.ThemeViewModel
import kotlinx.coroutines.launch

/**
 * Contenedor principal que gestiona la estructura visible de la app (drawer, top bar y contenido).
 *
 * Este Scaffold se usa después de que el usuario ha iniciado sesión o accede como invitado.
 *
 * @param globalNavController Controlador de navegación único de la app.
 * @param usuario Usuario actual (autenticado o invitado).
 * @param sesionViewModel ViewModel que gestiona los datos del usuario.
 * @param currentScreen Pantalla actualmente visible (home, clases, perfil, etc).
 * @param onNavigate Función que cambia la pantalla activa.
 * @param themeViewModel ViewModel que controla el modo claro/oscuro.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    globalNavController: NavHostController,
    usuario: Usuario,
    sesionViewModel: SesionViewModel,
    currentScreen: MainScreen,
    onNavigate: (MainScreen) -> Unit,
    themeViewModel: ThemeViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                usuario = usuario,
                onItemSelected = { route ->
                    coroutineScope.launch { drawerState.close() }
                    when (route) {
                        DrawerDestination.Home.route -> onNavigate(MainScreen.HOME)
                        DrawerDestination.Clases.route -> onNavigate(MainScreen.CLASES)
                        DrawerDestination.Perfil.route -> onNavigate(MainScreen.PERFIL)
                        DrawerDestination.Usuarios.route -> onNavigate(MainScreen.USUARIOS)
                        DrawerDestination.Logout.route -> {
                            sesionViewModel.cerrarSesion()
                        }
                    }
                },
                navController = globalNavController,
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                },
                sesionViewModel = sesionViewModel
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Bailoteca") },
                    actions = {
                        ThemeToggleButton(themeViewModel)
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    }
                )
            }
        ) { padding ->
            // Aplicamos padding para respetar la barra superior y otros elementos del Scaffold
            when (currentScreen) {
                MainScreen.HOME -> HomeScreen(
                    navController = globalNavController,
                    sesionViewModel = sesionViewModel,
                    modifier = Modifier.padding(padding)
                )
                MainScreen.CLASES -> ClaseListScreen(
                    navController = globalNavController,
                    viewModel = hiltViewModel(),
                    sesionViewModel = sesionViewModel,
                    modifier = Modifier.padding(padding)
                )
                MainScreen.USUARIOS -> UserListScreen(
                    navController = globalNavController,
                    viewModel = hiltViewModel(),
                    modifier = Modifier.padding(padding)
                )
                MainScreen.PERFIL -> ProfileScreen(
                    navController = globalNavController,
                    sesionViewModel = sesionViewModel,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}