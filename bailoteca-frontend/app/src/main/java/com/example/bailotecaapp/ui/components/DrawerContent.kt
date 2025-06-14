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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.bailotecaapp.R
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.navigation.DrawerDestination
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.network.FirebaseUrlProvider
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.google.firebase.auth.FirebaseAuth

/**
 * Componente visual para el menú lateral (Drawer).
 * Muestra las secciones disponibles según el rol del usuario.
 * También actualiza dinámicamente la imagen de perfil tras su edición.
 */
@Composable
fun DrawerContent(
    sesionViewModel: SesionViewModel,
    onItemSelected: (String) -> Unit,
    navController: NavHostController,
    onCloseDrawer: () -> Unit
) {

    val usuario by sesionViewModel.usuario.collectAsState()
    val rol = usuario?.rol ?: Rol.INVITADO
    Log.d("DrawerContent", "🧑‍🎤 Rol activo: $rol")

    val opciones = when (rol) {
        Rol.ADMIN -> listOf(
            DrawerDestination.Home,
            DrawerDestination.Clases,
            DrawerDestination.Eventos,
            DrawerDestination.Usuarios,
            DrawerDestination.Perfil,
            DrawerDestination.Logout
        )
        Rol.PROFESOR -> listOf(
            DrawerDestination.Home,
            DrawerDestination.Clases,
            DrawerDestination.Eventos,
            DrawerDestination.Perfil,
            DrawerDestination.Logout
        )
        Rol.USUARIO -> listOf(
            DrawerDestination.Home,
            DrawerDestination.Clases,
            DrawerDestination.Eventos,
            DrawerDestination.Perfil,
            DrawerDestination.Logout
        )
        Rol.INVITADO -> listOf(
            DrawerDestination.Home,
            DrawerDestination.Logout
        )
    }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: ""
    Log.d("DrawerContent", "📍 Ruta actual: $currentRoute")

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Cabecera
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(top = 48.dp, bottom = 16.dp)
                .clickable(enabled = rol != Rol.INVITADO) {
                    Log.d("DrawerContent", "👤 Click en cabecera -> Perfil")
                    onItemSelected(DrawerDestination.Perfil.route)
                    navController.navigate(DrawerDestination.Perfil.route)
                    onCloseDrawer()
                }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val firebaseUrlProvider = remember { FirebaseUrlProvider() }
                val baseUrl = firebaseUrlProvider.getBaseUrl()
                val imagenFilename = usuario?.fotoPerfil
                val imagenUrl = if (!imagenFilename.isNullOrBlank()) {
                    baseUrl.trimEnd('/') + "/media/" + imagenFilename
                } else null
                val timestamp = System.currentTimeMillis()
                val context = LocalContext.current

// Añadimos logs para verificar datos
                Log.d("DrawerContent", "🧠 Nombre archivo: $imagenFilename")
                Log.d("DrawerContent", "🌐 Base URL: $baseUrl")
                Log.d("DrawerContent", "🕒 Timestamp: $timestamp")

                val finalImageUrl = if (!imagenUrl.isNullOrBlank()) "$imagenUrl?$timestamp" else null

                val painter = if (!finalImageUrl.isNullOrBlank()) {
                    Log.d("DrawerContent", "🖼️ Usando imagen personalizada: $finalImageUrl")
                    rememberAsyncImagePainter(
                        model = ImageRequest.Builder(context)
                            .data(finalImageUrl)
                            .crossfade(true)
                            .diskCachePolicy(coil.request.CachePolicy.DISABLED) // Evita caché corrupta
                            .listener(
                                onError = { _, result ->
                                    Log.e("DrawerContent", "❌ Error cargando imagen: ${result.throwable.message}")
                                },
                                onSuccess = { request, _ ->
                                    Log.d("DrawerContent", "✅ Imagen cargada correctamente desde URL: ${request.data}")
                                }
                            )
                            .build()
                    )
                } else {
                    Log.d("DrawerContent", "🎨 Usando imagen por defecto")
                    painterResource(id = R.drawable.default_profile)
                }

                Image(
                    painter = painter,
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(usuario?.nombre ?: "Invitado", style = MaterialTheme.typography.titleMedium)
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
                        Log.d("DrawerContent", "🧭 Click en ${item.route}")
                        if (item == DrawerDestination.Logout) {
                            Log.d("DrawerContent", "🚪 Cerrando sesión")
                            sesionViewModel.cerrarSesion()
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate(Screens.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            if (item.route != currentRoute) {
                                navController.navigate(item.route) {
                                    popUpTo(Screens.Home.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                                onItemSelected(item.route)
                            } else {
                                Log.d("DrawerContent", "⏸ Ya estás en esta ruta, ignorando navegación")
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