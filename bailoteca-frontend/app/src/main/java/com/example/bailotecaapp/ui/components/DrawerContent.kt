package com.example.bailotecaapp.ui.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.bailotecaapp.R
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.google.firebase.auth.FirebaseAuth

/**
 * Menú lateral personalizado que se adapta al rol del usuario autenticado.
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
    Log.d("DrawerContent", "Renderizando menú para rol: $rol")

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
        // Encabezado con imagen y nombre
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(top = 48.dp, bottom = 16.dp)
                .clickable {
                    if (rol != Rol.INVITADO) {
                        onItemSelected(DrawerDestination.Perfil.route)
                        onCloseDrawer()
                    }
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
                            FirebaseAuth.getInstance().signOut()
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