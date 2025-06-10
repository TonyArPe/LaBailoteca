package com.example.bailotecaapp.ui.screens.eventos

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bailotecaapp.model.Evento
import com.example.bailotecaapp.model.enums.EstadoEvento
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.viewmodel.EventoViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Pantalla para crear o editar un evento.
 * Si hay un evento seleccionado en el ViewModel, se mostrará para editar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CrearEditarEventoScreen(
    navController: NavController,
    sesionViewModel: SesionViewModel,
    eventoViewModel: EventoViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val token by sesionViewModel.token.collectAsState()
    val usuario by sesionViewModel.usuario.collectAsState()

    if (usuario?.rol != Rol.ADMIN && usuario?.rol != Rol.PROFESOR) {
        // Si el usuario no es ADMIN o PROFESOR, lo redirigimos.
        LaunchedEffect(true) {
            navController.navigate(Screens.Eventos.route)
        }
        return
    }

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(LocalDateTime.now().toString()) }
    var lugar by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf(EstadoEvento.ACTIVO) }
    var publico by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (nombre.isNotEmpty()) "Editar Evento" else "Crear Evento") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (usuario == null || token == null) return@FloatingActionButton

                val nuevoEvento = Evento(
                    id = 0L, // Deberás manejar el ID si estás editando un evento existente
                    nombre = nombre,
                    descripcion = descripcion,
                    fecha = fecha,
                    lugar = lugar,
                    estado = estado,
                    organizador = usuario!!,
                    publico = publico
                )

                scope.launch {
                    val exito = if (eventoSeleccionado != null) {
                        eventoViewModel.actualizarEvento(token!!, nuevoEvento)
                    } else {
                        eventoViewModel.crearEvento(token!!, nuevoEvento)
                    }

                    if (exito) {
                        Toast.makeText(context, "Evento guardado correctamente", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, "Error al guardar el evento", Toast.LENGTH_LONG).show()
                    }
                }
            }) {
                Text("Guardar")
            }
        }
    ) { padding ->
        // Resto del formulario
    }
}