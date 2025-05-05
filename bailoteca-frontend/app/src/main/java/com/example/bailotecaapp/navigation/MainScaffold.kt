package com.example.bailotecaapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.ModalNavigationDrawer
import com.example.bailotecaapp.ui.components.DrawerContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import com.example.bailotecaapp.viewmodel.SesionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(navController: NavHostController) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val sesionViewModel: SesionViewModel = viewModel()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                onItemSelected = { destination ->
                    scope.launch {
                        drawerState.close()
                        if (destination == "logout") {
                            sesionViewModel.cerrarSesion()
                            navController.navigate("login") {
                                popUpTo(0) // Limpia el backstack para evitar volver con el botón atrás
                            }
                        } else {
                            navController.navigate(destination)
                        }
                    }
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
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { padding ->
            AppNavigation(navController, modifier = Modifier.padding(padding)
            )
        }
    }
}