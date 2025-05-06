package com.example.bailotecaapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.bailotecaapp.ui.components.DrawerContent
import com.example.bailotecaapp.viewmodel.SesionViewModel
import kotlinx.coroutines.launch

/**
 * Componente principal que configura el Scaffold general de la app.
 * Muestra un TopAppBar y un NavigationDrawer dependiendo de la pantalla actual.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavHostController,
    sessionViewModel: SesionViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val usuario by sessionViewModel.usuario.collectAsState()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    // Se muestra si estamos en pantalla principal, si no flecha para volver
    val esPantallaPrincipal = currentDestination?.route in listOf(
        Screens.Home.route,
        Screens.Clases.route,
        Screens.Usuarios.route
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            usuario?.let {
                DrawerContent(
                    onItemSelected = { ruta ->
                        navController.navigate(ruta) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                        scope.launch { drawerState.close() }
                    },
                    navController = navController,
                    usuario = it,
                    onCloseDrawer = { scope.launch { drawerState.close() } },
                    sesionViewModel = sessionViewModel
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("La Bailoteca")
                    },
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