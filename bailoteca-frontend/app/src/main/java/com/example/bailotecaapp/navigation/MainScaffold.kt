package com.example.bailotecaapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.ui.components.DrawerContent
import com.example.bailotecaapp.viewmodel.SesionViewModel
import kotlinx.coroutines.launch

/**
 * Estructura principal del layout de la aplicación, con AppBar, Drawer y navegación integrada.
 *
 * @param navController Controlador de navegación de Jetpack Compose.
 * @param sessionViewModel ViewModel que gestiona la sesión del usuario autenticado.
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

    // Carga del usuario actual desde backend al iniciar
    LaunchedEffect(Unit) {
        sessionViewModel.cargarUsuarioActual()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            if (usuario != null) {
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
                    usuario = usuario!!,
                    onCloseDrawer = { scope.launch { drawerState.close() } },
                    sesionViewModel = sessionViewModel
                )
            } else {
                // Drawer vacío o con "Cargando..."
                Text(
                    text = "Cargando menú...",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("La Bailoteca") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    }
                )
            }
        ) { padding ->
            AppNavigation(
                navController = navController,
                modifier = Modifier.padding(padding)
            )
        }
    }
}