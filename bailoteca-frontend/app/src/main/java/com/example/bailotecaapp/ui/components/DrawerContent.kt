package com.example.bailotecaapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.viewmodel.SesionViewModel

/**
 * Componente DrawerContent que muestra el menú lateral de navegación personalizado según el rol.
 *
 * @param onItemSelected Callback para cambiar la pantalla según el destino seleccionado.
 * @param sesionViewModel ViewModel que gestiona la sesión del usuario autenticado.
 */
@Composable
fun DrawerContent(
    onItemSelected: (String) -> Unit,
    navController: NavHostController,
    usuario: Usuario,
    onCloseDrawer: () -> Unit,
    sesionViewModel: SesionViewModel = viewModel()
) {
    val idUsuario = usuario.id
    val usuario by sesionViewModel.usuario.collectAsState()

    if (usuario == null) {
        // Mientras no haya usuario, muestra un placeholder
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Cargando menú...")
        }
        return
    }

    val rol = usuario?.rol ?: "INVITADO"
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
        else -> listOf(DrawerDestination.Home)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        opciones.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.label) },
                selected = false,
                onClick = { onItemSelected(item.route) },
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}