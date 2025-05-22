package com.example.bailotecaapp.navigation

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
import com.example.bailotecaapp.ui.components.DrawerContent
import com.example.bailotecaapp.ui.components.SesionGuard
import com.example.bailotecaapp.viewmodel.SesionViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavHostController
) {
    val sessionViewModel: SesionViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val usuario by sessionViewModel.usuario.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val invitado by sessionViewModel.modoInvitado.collectAsState()

    val currentDestination = navController.currentDestination
    val esPantallaPrincipal = remember(currentDestination) {
        val esPrincipal = currentDestination?.route in listOf(
            Screens.Home.route,
            Screens.Clases.route,
            Screens.Usuarios.route
        )
        Log.d("MainScaffold", "Ruta actual: ${currentDestination?.route}, ¿esPantallaPrincipal? $esPrincipal")
        esPrincipal
    }

    // 🔍 Trackea cambios de navegación
    LaunchedEffect(Unit) {
        snapshotFlow { navController.currentDestination?.route }
            .collectLatest { ruta ->
                Log.d("MainScaffold", "snapshotFlow: Cambio de ruta detectado: $ruta")
            }
    }

    Log.d(
        "MainScaffold",
        "🧩 Composición con usuario=${usuario?.correo}, id=${usuario?.id}, rol=${usuario?.rol}, invitado=$invitado"
    )

    key(usuario?.id) {
        SesionGuard(
            navController = navController,
            sesionViewModel = sessionViewModel
        ) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    DrawerContent(
                        onItemSelected = { ruta ->
                            Log.d("MainScaffold", "Drawer -> Navegar a: $ruta")
                            navController.navigate(ruta) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                }
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
                        Log.d("MainScaffold", "Evaluando TopAppBar, esPantallaPrincipal=$esPantallaPrincipal")
                        TopAppBar(
                            title = { Text("La Bailoteca") },
                            navigationIcon = {
                                if (esPantallaPrincipal) {
                                    Log.d("MainScaffold", "📌 Mostrando botón de Drawer")
                                    IconButton(onClick = {
                                        scope.launch { drawerState.open() }
                                    }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                                    }
                                } else {
                                    Log.d("MainScaffold", "📌 Mostrando botón de retroceso")
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
    }
}