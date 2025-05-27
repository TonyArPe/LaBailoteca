package com.example.bailotecaapp.navigation

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.ui.components.DrawerContent
import com.example.bailotecaapp.viewmodel.SesionViewModel
import kotlinx.coroutines.launch

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

    LaunchedEffect(usuario.id) {
        Log.d("MainScaffold", "🧭 Renderizando Scaffold para: ${usuario.correo}")
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                usuario = usuario,
                onItemSelected = { route ->
                    Log.d("MainScaffold", "📌 Item seleccionado en Drawer: $route")
                    scope.launch {
                        drawerState.close()
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                navController = navController,
                onCloseDrawer = { scope.launch { drawerState.close() } },
                sesionViewModel = sesionViewModel
            )
        }
    ) {
        Scaffold(
            topBar = {
                // Mostrar TopBar solo si el usuario NO es invitado
                if (usuario.rol != Rol.INVITADO) {
                    TopAppBar(
                        title = { Text("La Bailoteca") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menú")
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                content()
            }
        }
    }
}