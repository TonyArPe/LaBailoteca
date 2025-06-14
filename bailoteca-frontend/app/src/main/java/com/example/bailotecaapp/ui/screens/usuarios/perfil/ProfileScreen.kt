package com.example.bailotecaapp.ui.screens.usuarios.perfil

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.example.bailotecaapp.ui.theme.Lima
import com.example.bailotecaapp.ui.theme.Magenta
import com.example.bailotecaapp.utils.construirUrlMedia
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.example.bailotecaapp.viewmodel.ApiInitViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import com.example.bailotecaapp.utils.construirUrlMedia


/**
 * Pantalla que muestra el perfil del usuario autenticado.
 *
 * Esta pantalla proporciona una vista visualmente agradable de los datos del usuario,
 * permitiendo además la navegación a la pantalla de edición del perfil para usuarios no invitados.
 *
 * @param navController controlador de navegación que permite moverse entre pantallas.
 * @param sesionViewModel ViewModel que gestiona la sesión y expone el usuario actual.
 * @param modifier modificador para aplicar ajustes externos (padding, scroll, etc.)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    sesionViewModel: SesionViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val usuario by sesionViewModel.usuario.collectAsState()
    val apiInitViewModel: ApiInitViewModel = hiltViewModel()
    val baseUrl by apiInitViewModel.baseUrl.collectAsState()

    if (usuario == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val usuarioActual = usuario!!
    val imagenUrl = construirUrlMedia(usuarioActual.fotoPerfil, baseUrl)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mi Perfil") })
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 📷 Imagen de perfil del usuario
            if (!imagenUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imagenUrl + "?t=" + System.currentTimeMillis())
                        .crossfade(true)
                        .build(),
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                )
            }

            // 🧑 Nombre y correo
            Text(
                text = usuarioActual.nombre,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Magenta
            )

            Text(
                text = usuarioActual.correo,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // ℹ️ Resto de campos del usuario
            InfoRow(label = "Teléfono", value = usuarioActual.telefono)
            InfoRow(label = "Dirección", value = usuarioActual.direccion)
            InfoRow(label = "Fecha de nacimiento", value = usuarioActual.fechaNacimiento)
            InfoRow(label = "Género", value = usuarioActual.genero)
            InfoRow(label = "DNI", value = usuarioActual.dni)
            InfoRow(label = "Registrado desde", value = usuarioActual.fechaRegistro)
            InfoRow(label = "Pagado", value = if (usuarioActual.pagado) "Sí" else "No")
            InfoRow(label = "Activo", value = if (usuarioActual.activo) "Sí" else "No")

            Spacer(modifier = Modifier.height(20.dp))

            // ✏️ Botón de editar perfil si no es invitado
            if (usuarioActual.rol != Rol.INVITADO) {
                Button(
                    onClick = {
                        navController.navigate("editar_perfil")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Lima),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text("Editar Perfil", color = Color.White)
                }
            }
        }
    }
}

/**
 * Componente reutilizable para mostrar una fila de información en el perfil del usuario.
 *
 * @param label Etiqueta visible (ej. "Teléfono").
 * @param value Valor asociado (ej. "633 123 456").
 */
@Composable
fun InfoRow(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "$label:", fontWeight = FontWeight.Medium)
            Text(text = value)
        }
    }
}