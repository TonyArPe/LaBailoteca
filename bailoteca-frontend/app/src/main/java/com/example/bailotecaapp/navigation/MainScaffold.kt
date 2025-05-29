package com.example.bailotecaapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

/**
 * Contenedor principal de la aplicación una vez iniciada la sesión.
 * Muestra la estructura con Drawer + TopBar y renderiza la pantalla actual del NavHost.
 *
 * @param globalNavController Controlador global de navegación (único en toda la app).
 * @param usuario Usuario autenticado.
 * @param sesionViewModel ViewModel que gestiona la sesión y datos del usuario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    globalNavController: NavHostController,
    usuario: Usuario,
    sesionViewModel: SesionViewModel,
    currentScreen: MainScreen,
    onNavigate: (MainScreen) -> Unit
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
            when (currentScreen) {
                MainScreen.HOME -> HomeScreen(globalNavController, sesionViewModel)
                MainScreen.CLASES -> ClaseListScreen(globalNavController, hiltViewModel(), sesionViewModel)
                MainScreen.USUARIOS -> UserListScreen(globalNavController, hiltViewModel())
                MainScreen.PERFIL -> ProfileScreen(globalNavController, sesionViewModel)
            }
        }
    }
}