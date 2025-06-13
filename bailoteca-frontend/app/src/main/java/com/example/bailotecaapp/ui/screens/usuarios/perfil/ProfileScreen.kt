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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.example.bailotecaapp.ui.theme.Lima
import com.example.bailotecaapp.ui.theme.Magenta

/**
 * Pantalla que muestra el perfil del usuario autenticado de forma visual y alineada con la estética de Bailoteca.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    sesionViewModel: SesionViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val usuario by sesionViewModel.usuario.collectAsState()

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

    // ⚠️ Construcción segura de la URL de imagen desde IP local (evitando caché)
    val imagenUrl = usuarioActual.fotoPerfil?.let {
        "http://10.0.2.2:8080/api/media/files/$it?cache=${System.currentTimeMillis()}"
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mi Perfil") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Foto de perfil
            if (!imagenUrl.isNullOrBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(imagenUrl),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                )
            }

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

            InfoRow(label = "Teléfono", value = usuarioActual.telefono)
            InfoRow(label = "Dirección", value = usuarioActual.direccion)
            InfoRow(label = "Fecha de nacimiento", value = usuarioActual.fechaNacimiento)
            InfoRow(label = "Género", value = usuarioActual.genero)
            InfoRow(label = "DNI", value = usuarioActual.dni)
            InfoRow(label = "Registrado desde", value = usuarioActual.fechaRegistro)
            InfoRow(label = "Pagado", value = if (usuarioActual.pagado) "Sí" else "No")
            InfoRow(label = "Activo", value = if (usuarioActual.activo) "Sí" else "No")

            Spacer(modifier = Modifier.height(20.dp))

            if (usuarioActual.rol != Rol.INVITADO) {
                Button(
                    onClick = { navController.navigate(Screens.EditProfile.route) },
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
 * Componente reutilizable para mostrar una fila de información del perfil.
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