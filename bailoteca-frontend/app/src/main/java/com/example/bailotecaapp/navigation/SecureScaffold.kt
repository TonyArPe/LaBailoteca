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
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.ui.components.DrawerContent
import com.example.bailotecaapp.viewmodel.SesionViewModel
import kotlinx.coroutines.launch

/**
 * Scaffold seguro que renderiza el `TopAppBar` y `Drawer` dinámicamente según el rol.
 * Se asegura de que el `TopAppBar` aparezca correctamente en cualquier momento.
 *
 * @param navController Controlador de navegación.
 * @param sesionViewModel ViewModel de sesión (inyectado por defecto).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecureScaffold(
    navController: NavHostController,
    sesionViewModel: SesionViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val usuario by sesionViewModel.usuario.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isStartDestination = remember(currentRoute) {
        currentRoute == navController.graph.startDestinationRoute
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            usuario?.let {
                if (it.rol != Rol.INVITADO) {
                    DrawerContent(
                        navController = navController,
                        onCloseDrawer = { scope.launch { drawerState.close() } },
                        onItemSelected = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                            scope.launch { drawerState.close() }
                        },
                        sesionViewModel = sesionViewModel
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("La Bailoteca") },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (isStartDestination) {
                                scope.launch { drawerState.open() }
                            } else {
                                navController.popBackStack()
                            }
                        }) {
                            Icon(
                                imageVector = if (isStartDestination) Icons.Default.Menu else Icons.Default.ArrowBack,
                                contentDescription = "Navegación"
                            )
                        }
                    }
                )
                Log.d("SecureScaffold", "🔧 Renderizando TopAppBar. Usuario: ${usuario?.correo} | Rol=${usuario?.rol}")
            }
        ) { innerPadding ->
            AppNavigation(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}