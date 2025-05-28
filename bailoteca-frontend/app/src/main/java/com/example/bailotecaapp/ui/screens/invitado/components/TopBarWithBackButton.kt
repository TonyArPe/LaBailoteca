package com.example.bailotecaapp.ui.screens.invitado.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

/**
 * Barra superior con botón para volver atrás.
 *
 * @param navController Controlador de navegación.
 * @param titulo Texto mostrado como título.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarWithBackButton(navController: NavHostController, titulo: String) {
    TopAppBar(
        title = { Text(titulo) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
        }
    )
}
