package com.example.bailotecaapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel

/**
 * Componente DrawerContent que muestra un menú lateral de navegación.
 * Las opciones que se muestran dependen del rol del usuario.
 *
 * @param onItemSelected Función callback que se ejecuta al seleccionar un destino del menú.
 * @param sesionViewModel ViewModel que contiene la sesión actual del usuario.
 */
@Composable
fun DrawerContent(
    onItemSelected: (String) -> Unit,
    sesionViewModel: SesionViewModel = viewModel()
) {
    val usuario by sesionViewModel.usuario.collectAsState()
    if(usuario != null) {
        val rol = usuario?.rol ?: "INVITADO"

        // Lista de opciones de navegación según el rol del usuario
        val opciones = when (rol) {
            "ADMIN" -> listOf(
                DrawerDestination.Home,
                DrawerDestination.Clases,
                DrawerDestination.Usuarios,
                DrawerDestination.Perfil,
                DrawerDestination.Logout
            )

            "PROFESOR" -> listOf(
                DrawerDestination.Home,
                DrawerDestination.Clases,
                DrawerDestination.Perfil,
                DrawerDestination.Logout
            )

            "USUARIO" -> listOf(
                DrawerDestination.Home,
                DrawerDestination.Clases,
                DrawerDestination.Perfil,
                DrawerDestination.Logout
            )

            else -> listOf(
                DrawerDestination.Home
            )
        }

        // Composición visual del menú lateral
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface) // Color de fondo visible
                .padding(16.dp)
        ) {
            opciones.forEach { item ->
                NavigationDrawerItem(
                    label = { Text(item.label) },
                    selected = false,
                    onClick = { onItemSelected(item.route) },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}