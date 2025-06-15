package com.example.bailotecaapp.ui.screens.eventos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bailotecaapp.model.Evento
import com.example.bailotecaapp.ui.theme.Lima
import com.example.bailotecaapp.ui.theme.Magenta
import com.example.bailotecaapp.viewmodel.ApiInitViewModel
import com.example.bailotecaapp.viewmodel.EventoViewModel
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Tarjeta visual que muestra un evento con la estética de la app Bailoteca.
 * Incluye imagen, nombre, descripción, ubicación y fecha.
 *
 * @param evento Evento que se desea mostrar
 * @param navController Controlador de navegación
 * @param viewModel ViewModel compartido para seleccionar el evento
 */
@Composable
fun EventoCard(
    evento: Evento,
    navController: NavHostController,
    viewModel: EventoViewModel
) {
    val apiInitViewModel: ApiInitViewModel = hiltViewModel()
    val baseUrl by apiInitViewModel.baseUrl.collectAsState()
    val context = LocalContext.current

    val imagenFinal = evento.urlImagen?.let {
        "$baseUrl/media/$it?t=${System.currentTimeMillis()}"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable {
                viewModel.seleccionarEvento(evento)
                navController.navigate("evento/${evento.id}")
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column {
            if (!imagenFinal.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imagenFinal)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Imagen del evento",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text(
                    text = evento.nombre,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Magenta,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = evento.descripcion,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.DarkGray
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "📍 ${evento.lugar}",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Black)
                )
                Text(
                    text = "📅 ${evento.fecha}",
                    style = MaterialTheme.typography.labelSmall.copy(color = Lima)
                )
            }
        }
    }
}