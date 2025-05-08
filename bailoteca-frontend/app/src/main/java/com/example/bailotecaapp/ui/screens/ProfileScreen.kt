package com.example.bailotecaapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.example.bailotecaapp.navigation.Screens
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Pantalla que muestra el perfil del usuario autenticado de forma visual y atractiva.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    sesionViewModel: SesionViewModel = hiltViewModel()
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

    //Para guardar el usuario en una variable una vez hecha la comprobacion
    val usuarioActual = usuario!!

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
                .verticalScroll(rememberScrollState()), // Añade scroll
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Foto de perfil
            Image(
                painter = rememberAsyncImagePainter(usuarioActual.fotoPerfil),
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            )

            Text(
                text = usuarioActual.nombre,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = usuarioActual.correo,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Sección de detalles del usuario
            InfoRow(label = "Teléfono", value = usuarioActual.telefono)
            InfoRow(label = "Dirección", value = usuarioActual.direccion)
            InfoRow(label = "Fecha de nacimiento", value = usuarioActual.fechaNacimiento)
            InfoRow(label = "Género", value = usuarioActual.genero)
            InfoRow(label = "DNI", value = usuarioActual.dni)
            InfoRow(label = "Registrado desde", value = usuarioActual.fechaRegistro)
            InfoRow(label = "Pagado", value = if (usuarioActual.pagado) "Sí" else "No")
            InfoRow(label = "Activo", value = if (usuarioActual.activo) "Sí" else "No")

            Spacer(modifier = Modifier.height(20.dp))

            // Botón para editar perfil
            Button(
                onClick = {
                    navController.navigate(Screens.EditProfile.route)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Editar Perfil")
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
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "$label:", fontWeight = FontWeight.Medium)
            Text(text = value)
        }
    }
}