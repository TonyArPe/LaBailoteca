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
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.bailotecaapp.R
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

/**
 * Componente que representa el contenido del menú lateral (Drawer) adaptado al rol del usuario.
 *
 * Se actualiza dinámicamente en función del rol (ADMIN, PROFESOR, USUARIO, INVITADO).
 * También permite cerrar sesión o volver al login.
 *
 * @param onItemSelected Callback ejecutado cuando se selecciona una opción.
 * @param navController Controlador de navegación para redirigir pantallas.
 * @param onCloseDrawer Función para cerrar el drawer tras la selección.
 * @param sesionViewModel ViewModel que maneja la sesión (inyectado por Hilt).
 */

@Composable
fun DrawerContent(
    onItemSelected: (String) -> Unit,
    navController: NavHostController,
    onCloseDrawer: () -> Unit,
    sesionViewModel: SesionViewModel = hiltViewModel()
) {
    val usuarioState by sesionViewModel.usuario.collectAsState()
    val scope = rememberCoroutineScope()

    // Intenta obtener el usuario actual si no se ha cargado aún
    LaunchedEffect(usuarioState) {
        if (usuarioState == null && FirebaseAuth.getInstance().currentUser != null) {
            sesionViewModel.obtenerUsuarioActual()
        }
    }

    // Muestra un spinner mientras se carga el usuario
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
    val rol = usuario.rol
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
        // Encabezado del drawer con datos del usuario
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
                val painter = if (usuario.fotoPerfil.isEmpty()) {
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
                        scope.launch {
                            if (item == DrawerDestination.Logout) {
                                scope.launch {
                                    sesionViewModel.cerrarSesion()
                                    navController.navigate(Screens.Login.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            } else {
                                onItemSelected(item.route)
                            }
                            onCloseDrawer()
                        }
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Botón para INVITADOS que quieran volver al login
            if (rol == Rol.INVITADO) {
                NavigationDrawerItem(
                    label = { Text("Volver al login") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            sesionViewModel.cerrarSesion()
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate(Screens.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                            onCloseDrawer()
                        }
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}