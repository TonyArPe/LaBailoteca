package com.example.bailotecaapp.ui.screens.eventos

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bailotecaapp.model.Evento
import com.example.bailotecaapp.model.enums.EstadoEvento
import com.example.bailotecaapp.viewmodel.EventoViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import com.example.bailotecaapp.utils.DropdownMenuBox


/**
 * Pantalla para crear o editar un evento.
 *
 * - Si se proporciona un evento existente desde el ViewModel, se entra en modo edición.
 * - Si no hay evento seleccionado, se entra en modo creación.
 *
 * @param navController NavController global
 * @param eventoViewModel ViewModel encargado de gestionar los eventos
 * @param sesionViewModel ViewModel de sesión (para obtener el token)
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearEditarEventoScreen(
    navController: NavController,
    eventoViewModel: EventoViewModel,
    sesionViewModel: SesionViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val token by sesionViewModel.token.collectAsState()
    val evento = eventoViewModel.eventoSeleccionado.collectAsState().value

    var nombre by remember { mutableStateOf(evento?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(evento?.descripcion ?: "") }
    var fecha by remember { mutableStateOf(evento?.fecha ?: LocalDateTime.now().toString()) }
    var lugar by remember { mutableStateOf(evento?.lugar ?: "") }
    var publico by remember { mutableStateOf(evento?.publico ?: true) }
    var estado by remember { mutableStateOf(evento?.estado ?: EstadoEvento.ACTIVO) }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher para galería
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imagenUri = uri }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (evento != null) "Editar evento" else "Nuevo evento") }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del evento") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = fecha,
                onValueChange = { fecha = it },
                label = { Text("Fecha (formato ISO)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = lugar,
                onValueChange = { lugar = it },
                label = { Text("Lugar") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Evento público")
                Spacer(modifier = Modifier.width(8.dp))
                Switch(checked = publico, onCheckedChange = { publico = it })
            }

            if (evento != null) {
                Text("Estado del evento")
                DropdownMenuBox(
                    selected = estado,
                    onSelected = { estado = it },
                    opciones = EstadoEvento.entries
                )
            }

            // Selector de imagen
            OutlinedButton(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Seleccionar imagen")
            }

            // Previsualización
            imagenUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Imagen del evento",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
            }

            Button(
                onClick = {
                    if (nombre.isBlank() || descripcion.isBlank() || lugar.isBlank() || fecha.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Por favor, completa todos los campos")
                        }
                        return@Button
                    }

                    scope.launch {
                        val exito = if (evento == null) {
                            val nuevo = Evento(
                                id = 0L,
                                nombre = nombre,
                                descripcion = descripcion,
                                fecha = fecha,
                                lugar = lugar,
                                estado = EstadoEvento.ACTIVO,
                                organizador = sesionViewModel.usuario.value!!,
                                publico = publico
                            )
                            eventoViewModel.crearEvento(token ?: "", nuevo)
                        } else {
                            val actualizado = evento.copy(
                                nombre = nombre,
                                descripcion = descripcion,
                                fecha = fecha,
                                lugar = lugar,
                                estado = estado,
                                publico = publico
                            )
                            eventoViewModel.actualizarEvento(token ?: "", actualizado)
                        }

                        if (exito) {
                            Toast.makeText(context, "Guardado", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } else {
                            snackbarHostState.showSnackbar("Error al guardar el evento")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }
        }
    }
}