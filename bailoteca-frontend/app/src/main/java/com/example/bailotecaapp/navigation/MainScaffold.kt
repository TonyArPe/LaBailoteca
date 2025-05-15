package com.example.bailotecaapp.navigation

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
import com.example.bailotecaapp.ui.components.DrawerContent
import com.example.bailotecaapp.ui.components.SesionGuard
import com.example.bailotecaapp.viewmodel.SesionViewModel
import kotlinx.coroutines.launch

/**
 * Componente principal que configura el Scaffold general de la app.
 * Muestra un TopAppBar y un Drawer lateral que se adapta a la sesión del usuario.
 *
 * Utiliza `SesionGuard` para asegurarse de que el usuario está cargado
 * antes de mostrar el drawer lateral o las pantallas protegidas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavHostController,
    sessionViewModel: SesionViewModel = hiltViewModel()
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val rutasConScaffold = listOf(
        Screens.Home.route,
        Screens.Clases.route,
        Screens.Usuarios.route,
        Screens.Perfil.route,
        Screens.EditProfile.route,
        Screens.ClaseDetail.route
    )

    if (currentRoute in rutasConScaffold) {
        val esPantallaPrincipal = currentRoute in listOf(
            Screens.Home.route,
            Screens.Clases.route,
            Screens.Usuarios.route
        )

        SesionGuard(navController = navController, sesionViewModel = sessionViewModel) { usuario ->
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    DrawerContent(
                        onItemSelected = { ruta ->
                            navController.navigate(ruta) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                launchSingleTop = true
                            }
                            scope.launch { drawerState.close() }
                        },
                        navController = navController,
                        onCloseDrawer = { scope.launch { drawerState.close() } },
                        sesionViewModel = sessionViewModel
                    )
                }
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("La Bailoteca") },
                            navigationIcon = {
                                if (esPantallaPrincipal) {
                                    IconButton(onClick = {
                                        scope.launch { drawerState.open() }
                                    }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                                    }
                                } else {
                                    IconButton(onClick = {
                                        navController.navigate(Screens.Home.route) {
                                            popUpTo(Screens.Home.route) { inclusive = false }
                                            launchSingleTop = true
                                        }
                                    }) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                                    }
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    AppNavigation(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    } else {
        // Rutas públicas: sin drawer ni topbar
        AppNavigation(navController = navController)
    }
}