package com.example.bailotecaapp.ui.screens.invitado

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bailotecaapp.model.Evento
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.ui.theme.Lima
import com.example.bailotecaapp.ui.theme.Magenta

/**
 * Muestra una tarjeta de evento público en modo invitado y permite navegar al detalle.
 *
 * @param evento Evento a representar visualmente.
 * @param navController Controlador de navegación para ir al detalle.
 */
@Composable
fun EventoCardInvitado(evento: Evento, navController: NavController) {
    val context = LocalContext.current
    val imagenUrl = evento.urlImagen?.let {
        "https://tu-backend/media/$it?t=${System.currentTimeMillis()}"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable {
                navController.navigate(Screens.EventoDetail.createRoute(evento.id))
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (!imagenUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imagenUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Imagen del evento",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
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
                        color = Magenta
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = evento.descripcion,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "📅 ${evento.fecha}",
                    style = MaterialTheme.typography.labelMedium.copy(color = Lima)
                )

                Text(
                    text = "📍 ${evento.lugar}",
                    style = MaterialTheme.typography.labelMedium.copy(color = Color.Black)
                )
            }
        }
    }
}