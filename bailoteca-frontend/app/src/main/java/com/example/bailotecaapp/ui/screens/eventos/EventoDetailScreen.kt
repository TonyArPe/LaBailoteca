package com.example.bailotecaapp.ui.screens.eventos

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.viewmodel.EventoViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla de detalle de un evento.
 * Muestra la información del evento seleccionado y permite editarlo o eliminarlo si el usuario tiene permisos.
 *
 * @param eventoId ID del evento a mostrar.
 * @param navController Controlador de navegación para redirigir a otras pantallas.
 * @param sesionViewModel ViewModel de sesión del usuario autenticado.
 * @param eventoViewModel ViewModel de eventos que contiene el evento seleccionado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventoDetailScreen(
    eventoId: Long,
    navController: NavController,
    sesionViewModel: SesionViewModel,
    eventoViewModel: EventoViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val token by sesionViewModel.token.collectAsState()
    val usuario by sesionViewModel.usuario.collectAsState()
    val evento by eventoViewModel.eventoSeleccionado.collectAsState()

    // Carga el evento seleccionado al entrar en la pantalla
    LaunchedEffect(eventoId) {
        Log.d("EventoDetailScreen", "🔍 Buscando evento con ID $eventoId")
        eventoViewModel.seleccionarEvento(eventoId)
    }

    if (evento == null) {
        Log.w("EventoDetailScreen", "⚠️ Evento con ID $eventoId no encontrado")
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Evento no encontrado", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val esOrganizador = usuario?.id == evento?.organizador?.id
    val esAdmin = usuario?.rol == Rol.ADMIN
    val puedeEditar = esOrganizador || esAdmin

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(evento!!.nombre) })
        },
        floatingActionButton = {
            if (puedeEditar) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FloatingActionButton(
                        onClick = {
                            // No es necesario volver a seleccionar: ya está seleccionado
                            navController.navigate(Screens.CrearEvento.route)
                        },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar evento")
                    }

                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                val exito = eventoViewModel.eliminarEvento(token ?: "", evento!!.id)
                                if (exito) {
                                    Toast.makeText(context, "Evento eliminado", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                } else {
                                    Toast.makeText(context, "Error al eliminar evento", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar evento")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(evento!!.descripcion, style = MaterialTheme.typography.bodyMedium)
            Text("📍 ${evento!!.lugar}", style = MaterialTheme.typography.labelLarge)
            Text("📅 ${evento!!.fecha.replace('T', ' ')}", style = MaterialTheme.typography.labelLarge)

            Text(
                text = if (evento!!.publico) "Evento público" else "Evento privado",
                color = if (evento!!.publico) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )

            Text("Organizador: ${evento!!.organizador.nombre}", style = MaterialTheme.typography.bodySmall)

            evento!!.organizador.fotoPerfil?.takeIf { it.isNotBlank() }?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "Imagen del evento",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
    }
}