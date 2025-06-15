package com.example.bailotecaapp.ui.screens.eventos

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bailotecaapp.viewmodel.EventoViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.network.session.SesionManager
import com.example.bailotecaapp.ui.theme.Lima
import com.example.bailotecaapp.ui.theme.Magenta
import com.example.bailotecaapp.utils.construirUrlMedia
import com.example.bailotecaapp.viewmodel.ApiInitViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla que muestra los detalles de un evento.
 *
 * Carga el evento desde el ViewModel en base al ID recibido por navegación.
 * Solo si el usuario tiene permisos, muestra botones de editar y eliminar.
 */
@Composable
fun EventoDetailScreen(
    navController: NavHostController,
    eventoId: Long,
    eventoViewModel: EventoViewModel = hiltViewModel(),
    apiInitViewModel: ApiInitViewModel = hiltViewModel(),
    sesionViewModel: SesionViewModel = hiltViewModel(),
    sesionManager: SesionManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val evento by eventoViewModel.eventoSeleccionado.collectAsState()
    val baseUrl by apiInitViewModel.baseUrl.collectAsState()
    var usuario by remember { mutableStateOf<Usuario?>(null) }
    val usuarioSesion by sesionViewModel.usuario.collectAsState()

    LaunchedEffect(Unit) {
        val restaurado = sesionManager.obtenerUsuario()
        usuario = restaurado
        Log.d("EventoDetailScreen", "👤 Usuario restaurado directamente desde SesionManager: ${usuario?.correo}, rol=${usuario?.rol}")
    }

    var showDialog by remember { mutableStateOf(false) }

    // Cargar evento por ID usando el token de sesión
    LaunchedEffect(eventoId) {
        val token = sesionViewModel.getTokenActual()
        if (token != null) {
            Log.d("EventoDetailScreen", "🔑 Token obtenido: $token")
            eventoViewModel.cargarEventoPorId(token, eventoId)
        } else {
            Log.e("EventoDetailScreen", "❌ Token no disponible. No se puede cargar evento.")
        }
    }

    if (evento == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Lima)
        }
        return
    }

    val imagenUrl = construirUrlMedia(evento!!.urlImagen, baseUrl)

    val puedeEditar = usuario?.rol == Rol.ADMIN ||
            usuario?.correo == evento?.nombreOrganizador

    Log.d("EventoDetailScreen", "🔐 Validación de permisos:")
    Log.d("EventoDetailScreen", "👤 Usuario correo: ${usuario?.correo}, rol: ${usuario?.rol}")
    Log.d("EventoDetailScreen", "👨‍🏫 Organizador: ${evento?.nombreOrganizador}")
    Log.d("EventoDetailScreen", "✅ Puede editar: $puedeEditar")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Imagen del evento
        if (!imagenUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imagenUrl)
                    .crossfade(true)
                    .listener(
                        onError = { _, result ->
                            Log.e("EventoDetailScreen", "❌ Error cargando imagen: ${result.throwable}")
                        },
                        onSuccess = { _, _ ->
                            Log.d("EventoDetailScreen", "✅ Imagen cargada correctamente")
                        }
                    )
                    .build(),
                contentDescription = "Imagen del evento",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.DarkGray)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = evento!!.nombre,
            style = MaterialTheme.typography.headlineSmall.copy(
                color = Magenta,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = evento!!.descripcion,
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.DarkGray)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "📍 Lugar: ${evento!!.lugar}",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Black)
        )
        Text(
            text = "📅 Fecha: ${evento!!.fecha}",
            style = MaterialTheme.typography.bodySmall.copy(color = Lima)
        )
        Text(
            text = "🌐 Público: ${if (evento!!.publico) "Sí" else "No"}",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (puedeEditar) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Button(
                    onClick = {
                        navController.navigate(Screens.EditarEvento.create(evento!!.id))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Magenta),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar evento")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Editar")
                }

                Button(
                    onClick = { showDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar evento")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar")
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("¿Eliminar evento?", color = Color.Black) },
            text = { Text("Esta acción no se puede deshacer.", color = Color.DarkGray) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        scope.launch {
                            val token = sesionViewModel.getTokenActual()
                            val ok = token?.let {
                                eventoViewModel.eliminarEvento(it, evento!!.id)
                            } ?: false

                            if (ok) {
                                Toast.makeText(context, "✅ Evento eliminado", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } else {
                                Toast.makeText(context, "❌ No se pudo eliminar", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar", color = Color.DarkGray)
                }
            }
        )
    }
}