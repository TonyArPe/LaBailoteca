package com.example.bailotecaapp.navigation

import ThemeToggleButton
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.ui.components.DrawerContent
import com.example.bailotecaapp.ui.screens.HomeScreen
import com.example.bailotecaapp.ui.screens.UserListScreen
import com.example.bailotecaapp.ui.screens.clases.ClaseListScreen
import com.example.bailotecaapp.ui.screens.perfil.ProfileScreen
import com.example.bailotecaapp.ui.screens.enumscreens.MainScreen
import com.example.bailotecaapp.ui.screens.eventos.EventoListScreen
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.example.bailotecaapp.viewmodel.ThemeViewModel
import kotlinx.coroutines.launch

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

    val backStackEntry by globalNavController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentRoute = currentDestination?.route ?: ""
    val currentBaseRoute = currentRoute.split("/").firstOrNull() ?: ""

    // Rutas principales que muestran Drawer
    val mainRoutes = setOf(
        Screens.Home.route,
        Screens.Clases.route,
        Screens.Usuarios.route,
        Screens.Perfil.route,
        Screens.InvitadoHome.route
    )

    val showDrawer = currentBaseRoute in mainRoutes
    val showBack = !showDrawer

    Log.d("MainScaffold", "🔍 Ruta actual completa: $currentRoute")
    Log.d("MainScaffold", "🧩 BaseRoute: $currentBaseRoute")
    Log.d("MainScaffold", "📦 showDrawer: $showDrawer, showBack: $showBack")

    val topBarTitle = when {
        currentRoute.contains("clase/") -> "Detalle de clase"
        else -> "Bailoteca"
    }

    val topBarNavigationIcon: @Composable (() -> Unit) = {
        if (showDrawer) {
            Log.d("MainScaffold", "👈 Mostrando botón menú (Drawer)")
            IconButton(onClick = {
                coroutineScope.launch { drawerState.open() }
            }) {
                Icon(Icons.Default.Menu, contentDescription = "Menú")
            }
        } else if (showBack) {
            Log.d("MainScaffold", "🔙 Mostrando botón atrás")
            IconButton(onClick = {
                globalNavController.popBackStack()
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            if (showDrawer) {
                Log.d("MainScaffold", "📂 Renderizando DrawerContent")
                DrawerContent(
                    usuario = usuario,
                    onItemSelected = { route ->
                        coroutineScope.launch { drawerState.close() }
                        onNavigate(
                            when (route) {
                                Screens.Home.route -> MainScreen.HOME
                                Screens.Clases.route -> MainScreen.CLASES
                                Screens.Usuarios.route -> MainScreen.USUARIOS
                                Screens.Perfil.route -> MainScreen.PERFIL
                                Screens.Eventos.route -> MainScreen.EVENTOS
                                else -> MainScreen.HOME
                            }
                        )
                    },
                    navController = globalNavController,
                    onCloseDrawer = {
                        coroutineScope.launch { drawerState.close() }
                    },
                    sesionViewModel = sesionViewModel
                )
            } else {
                Log.d("MainScaffold", "📂 Drawer oculto por ruta")
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(topBarTitle) },
                    navigationIcon = topBarNavigationIcon,
                    actions = {
                        ThemeToggleButton(themeViewModel)
                    }
                )
            },
            content = { padding ->
                Log.d("MainScaffold", "📦 Renderizando contenido para: ${currentScreen.name}")
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

                    MainScreen.EVENTOS -> EventoListScreen(
                        navController = globalNavController,
                        usuario = usuario,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        )
    }
}