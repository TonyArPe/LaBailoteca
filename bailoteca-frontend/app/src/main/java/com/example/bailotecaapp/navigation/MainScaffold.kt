package com.example.bailotecaapp.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.ui.components.DrawerContent
import com.example.bailotecaapp.viewmodel.SesionViewModel
import kotlinx.coroutines.launch

/**
 * Scaffold principal que muestra el Drawer lateral y la TopAppBar superior.
 * Esta versión espera un usuario válido ya validado desde SesionGuard.
 *
 * @param navController Controlador de navegación para gestionar rutas.
 * @param usuario Usuario ya autenticado (no se hacen verificaciones internas).
 * @param content Contenido principal a renderizar dentro del Scaffold.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavHostController,
    usuario: Usuario,
    sesionViewModel: SesionViewModel,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                usuario = usuario,
                onItemSelected = { route ->
                    scope.launch { drawerState.close() }
                    navController.navigate(route)
                },
                navController = navController,
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                },
                sesionViewModel = sesionViewModel
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("La Bailoteca") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)) {
                content()
            }
        }
    }
}