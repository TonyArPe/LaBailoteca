package com.example.bailotecaapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bailotecaapp.ui.components.DrawerContent
import com.example.bailotecaapp.viewmodel.SesionViewModel
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(navController: NavHostController, sessionViewModel: SesionViewModel) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    // Obtenemos el usuario desde el ViewModel
    val usuario by sessionViewModel.usuario.collectAsState()

    // Lanzamos la carga del usuario solo una vez
    LaunchedEffect(Unit) {
        sessionViewModel.cargarUsuarioDesdeApi(idUsuario = "id_desde_firebase_o_token")
    }

    Scaffold(
        drawerContent = {
            if (usuario != null) {
                DrawerContent(
                    navController = navController,
                    usuario = usuario!!,
                    onCloseDrawer = { scope.launch { drawerState.close() } }
                )
            }
        },
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