package com.example.bailotecaapp.ui.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberAsyncImagePainter
import com.example.bailotecaapp.R
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.navigation.DrawerDestination
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.google.firebase.auth.FirebaseAuth

/**
 * Composable del menú lateral (Drawer).
 * Muestra opciones distintas según el rol del usuario y permite navegar o cerrar sesión.
 *
 * @param usuario Usuario actualmente autenticado.
 * @param onItemSelected Callback con la ruta seleccionada.
 * @param navController Controlador de navegación global.
 * @param onCloseDrawer Función a ejecutar al cerrar el Drawer.
 * @param sesionViewModel ViewModel de sesión para cerrar sesión correctamente.
 */
@Composable
fun DrawerContent(
    usuario: Usuario,
    onItemSelected: (String) -> Unit,
    navController: NavHostController,
    onCloseDrawer: () -> Unit,
    sesionViewModel: SesionViewModel
) {
    val rol = usuario.rol
    Log.d("DrawerContent", "📦 Renderizando menú para rol: $rol")

    // Opciones por rol
    val opciones = when (rol) {
        Rol.ADMIN -> listOf(
            DrawerDestination.Home,
            DrawerDestination.Clases,
            DrawerDestination.Usuarios,
            DrawerDestination.Perfil,
            DrawerDestination.Logout
        )
        Rol.PROFESOR, Rol.USUARIO -> listOf(
            DrawerDestination.Home,
            DrawerDestination.Clases,
            DrawerDestination.Perfil,
            DrawerDestination.Logout
        )
        Rol.INVITADO -> listOf(
            DrawerDestination.Home,
            DrawerDestination.Clases
        )
    }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    Log.d("DrawerContent", "📍 Ruta actual: $currentRoute")

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Cabecera con datos de usuario
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(top = 48.dp, bottom = 16.dp)
                .clickable(enabled = rol != Rol.INVITADO) {
                    onItemSelected(DrawerDestination.Perfil.route)
                    onCloseDrawer()
                }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val painter = if (usuario.fotoPerfil.isNullOrEmpty())
                    painterResource(id = R.drawable.default_profile)
                else rememberAsyncImagePainter(usuario.fotoPerfil)

                Image(
                    painter = painter,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(usuario.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    rol.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Divider(thickness = 1.dp)

        // Ítems del menú
        Column(modifier = Modifier.padding(16.dp)) {
            opciones.forEach { item ->
                NavigationDrawerItem(
                    label = { Text(item.label) },
                    selected = currentRoute == item.route,
                    onClick = {
                        Log.d("DrawerContent", "🧭 Selección de ítem: ${item.route}")
                        if (item == DrawerDestination.Logout) {
                            Log.d("DrawerContent", "🔒 Logout solicitado")
                            sesionViewModel.cerrarSesion()
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate(Screens.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            if (item.route != currentRoute) {
                                onItemSelected(item.route)
                            }
                        }
                        onCloseDrawer()
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}