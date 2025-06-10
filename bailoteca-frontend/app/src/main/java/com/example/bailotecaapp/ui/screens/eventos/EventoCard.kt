package com.example.bailotecaapp.ui.screens.eventos;

import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.shape.RoundedCornerShape;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.Lock;
import androidx.compose.material.icons.filled.Public;
import androidx.compose.material.icons.filled.Visibility;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.*;
import androidx.compose.ui.draw.clip;
import androidx.compose.ui.graphics.Color;
import androidx.compose.ui.platform.LocalContext;
import androidx.compose.ui.unit.dp;
import coil.compose.AsyncImage;
import coil.request.ImageRequest;
import com.example.bailotecaapp.model.Evento;
import com.example.bailotecaapp.model.Usuario;
import com.example.bailotecaapp.model.enums.Rol;
import com.example.bailotecaapp.viewmodel.EventoViewModel;
import android.util.Log;
import androidx.compose.foundation.clickable
import androidx.navigation.NavController
import com.example.bailotecaapp.navigation.Screens
import kotlinx.coroutines.launch

/**
 * Componente visual que representa una tarjeta con información de un evento.
 * Muestra el nombre, descripción, fecha, lugar, visibilidad (público o privado) y
 * permite al usuario autenticado marcar o cancelar su asistencia al evento.
 *
 * También se muestra la imagen del organizador si está disponible.
 *
 * @param evento Evento a mostrar
 * @param usuario Usuario autenticado (puede ser ADMIN, PROFESOR, USUARIO, etc.)
 * @param token Token JWT válido para las llamadas protegidas
 * @param eventoViewModel ViewModel encargado de la lógica de asistencia
 */
@Composable
fun EventoCard(
    evento: Evento,
    usuario: Usuario?,
    token: String?,
    eventoViewModel: EventoViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val yaAsiste by remember { derivedStateOf { eventoViewModel.yaAsiste(evento.id) } }
    val puedeAsistir = usuario?.rol?.name == Rol.USUARIO.name

    Log.d("EventoCard", "Renderizando evento: ${evento.nombre} (${evento.id})")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                Log.d("EventoCard", "🖱️ Click en evento ${evento.id}")
                navController.navigate(Screens.EventoDetalle.createRoute(evento.id))
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = evento.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Icon(
                    imageVector = if (evento.publico) Icons.Default.Public else Icons.Default.Lock,
                    contentDescription = if (evento.publico) "Evento público" else "Evento privado",
                    tint = if (evento.publico) Color(0xFF8CD400) else Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = evento.descripcion,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "📍 ${evento.lugar} — 📅 ${evento.fecha.take(16).replace('T', ' ')}",
                style = MaterialTheme.typography.labelSmall
            )

            // Verificar si el organizador existe antes de intentar acceder a su imagen
            evento.organizador?.fotoPerfil?.takeIf { it.isNotBlank() }?.let { url ->
                Log.d("EventoCard", "Cargando imagen desde URL: $url")
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = ImageRequest.Builder(context).data(url).crossfade(true).build(),
                    contentDescription = "Imagen del evento u organizador",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            } ?: Log.d("EventoCard", "No hay imagen de perfil del organizador")

            Spacer(modifier = Modifier.height(12.dp))

            // Botón de asistir o cancelar asistencia
            if (puedeAsistir && token != null) {
                Button(
                    onClick = {
                        if (yaAsiste) {
                            eventoViewModel.cancelarAsistencia(evento.id, token)
                            eventoViewModel.mostrarToast(context, "Asistencia cancelada a ${evento.nombre}")
                        } else {
                            eventoViewModel.asistirEvento(evento.id, token)
                            eventoViewModel.mostrarToast(context, "Asistencia registrada a ${evento.nombre}")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (yaAsiste) Color.Gray else Color(0xFF8CD400),
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(if (yaAsiste) "Cancelar asistencia" else "Asistir al evento")
                }
            }
        }
    }
}