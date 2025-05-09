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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.bailotecaapp.R
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

/**
 * Componente DrawerContent que muestra el menú lateral de navegación personalizado según el rol.
 *
 * @param onItemSelected Callback para cambiar la pantalla según el destino seleccionado.
 * @param navController Controlador de navegación.
 * @param usuario Usuario actualmente autenticado.
 * @param onCloseDrawer Función para cerrar el drawer.
 * @param sesionViewModel ViewModel de sesión (por defecto se obtiene con viewModel()).
 */
@Composable
fun DrawerContent(
    onItemSelected: (String) -> Unit,
    navController: NavHostController,
    onCloseDrawer: () -> Unit,
    sesionViewModel: SesionViewModel = hiltViewModel()
) {
    val usuarioState by sesionViewModel.usuario.collectAsState()

    // Para que se cargue el usuario solo la primera vez que se abre el drawer
    LaunchedEffect(usuarioState) {
        if (usuarioState == null && Firebase.auth.currentUser != null) {
            sesionViewModel.obtenerUsuarioActual()
        }
    }

    // Mostrar cargando si aún no hay usuario
    if (usuarioState == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val usuario = usuarioState!!
    val rol = usuario.rol ?: Rol.INVITADO
    Log.d("DrawerContent", "ROL: $rol")

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
        else -> listOf(DrawerDestination.Home)
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Header con datos del usuario
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(top = 48.dp, bottom = 16.dp)
                .clickable {
                    onItemSelected(DrawerDestination.Perfil.route)
                    onCloseDrawer()
                }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                val painter = if (usuario.fotoPerfil.isNullOrEmpty()) {
                    painterResource(id = R.drawable.default_profile)
                } else {
                    rememberAsyncImagePainter(usuario.fotoPerfil)
                }

                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(4.dp)
                ) {
                    Image(
                        painter = painter,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = usuario.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = rol.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

        Column(modifier = Modifier.padding(16.dp)) {
            opciones.forEach { item ->
                NavigationDrawerItem(
                    label = { Text(item.label) },
                    selected = false,
                    onClick = {
                        if (item == DrawerDestination.Logout) {
                            sesionViewModel.cerrarSesion()
                            Firebase.auth.signOut()
                            navController.navigate(Screens.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            onItemSelected(item.route)
                        }
                        onCloseDrawer()
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}