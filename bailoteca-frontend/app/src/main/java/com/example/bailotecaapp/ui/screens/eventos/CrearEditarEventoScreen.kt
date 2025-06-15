package com.example.bailotecaapp.ui.screens.eventos

import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.bailotecaapp.ui.theme.Lima
import com.example.bailotecaapp.ui.theme.Magenta
import com.example.bailotecaapp.utils.construirUrlMedia
import com.example.bailotecaapp.utils.crearMultipartDesdeUri
import com.example.bailotecaapp.viewmodel.ApiInitViewModel
import com.example.bailotecaapp.viewmodel.EventoViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Pantalla que permite crear o editar un evento.
 *
 * Si eventoSeleccionado está presente, se muestra en modo edición.
 * Si no hay evento cargado, se crea uno nuevo desde cero.
 *
 * @param navController controlador de navegación para volver atrás
 * @param eventoId id del evento (solo para editar). Si es null, es modo creación.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CrearEditarEventoScreen(
    navController: NavController,
    sesionViewModel: SesionViewModel,
    eventoId: Long? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val eventoViewModel: EventoViewModel = hiltViewModel()
    val apiInitViewModel: ApiInitViewModel = hiltViewModel()

    val token by sesionViewModel.token.collectAsState()
    val usuario by sesionViewModel.usuario.collectAsState()
    val usuarioCargado by sesionViewModel.usuarioCargado.collectAsState()
    val eventoSeleccionado by eventoViewModel.eventoSeleccionado.collectAsState()
    val baseUrl by apiInitViewModel.baseUrl.collectAsState()

    LaunchedEffect(eventoId) {
        if (eventoId != null) {
            eventoViewModel.cargarEventoPorId(token ?: "", eventoId)
        }
    }

    if (!usuarioCargado || usuario == null) {
        sesionViewModel.sincronizarDesdeSesionManager()
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Lima)
        }
        return
    }

    // ------------------ CAMPOS -------------------
    var nombre by remember { mutableStateOf(eventoSeleccionado?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(eventoSeleccionado?.descripcion ?: "") }
    var fecha by remember {
        mutableStateOf(
            eventoSeleccionado?.fecha?.substringBefore("T")?.let { LocalDate.parse(it) }
                ?: LocalDate.now()
        )
    }
    var lugar by remember { mutableStateOf(eventoSeleccionado?.lugar ?: "") }
    var publico by remember { mutableStateOf(eventoSeleccionado?.publico ?: true) }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    val imagenSubidaNombre = remember { mutableStateOf(eventoSeleccionado?.urlImagen) }

    val imagenFinal = imagenUri ?: eventoSeleccionado?.urlImagen?.let {
        construirUrlMedia(it, baseUrl)?.let { Uri.parse(it) }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imagenUri = uri
        uri?.let {
            val archivoPart = crearMultipartDesdeUri(context, it)
            eventoViewModel.subirImagenEvento(archivoPart) { nombreArchivo ->
                imagenSubidaNombre.value = nombreArchivo
            }
        }
    }

    // ------------------ UI -------------------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            if (eventoSeleccionado != null) "Editar evento" else "Crear evento",
            style = MaterialTheme.typography.headlineSmall,
            color = Magenta
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del evento") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = Color.Black),
            colors = campoColores()
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = Color.Black),
            colors = campoColores()
        )

        OutlinedTextField(
            value = lugar,
            onValueChange = { lugar = it },
            label = { Text("Lugar") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = Color.Black),
            colors = campoColores()
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Magenta)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Fecha: ${fecha.format(DateTimeFormatter.ISO_DATE)}", color = Color.DarkGray)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Evento público", color = Color.DarkGray)
            Switch(
                checked = publico,
                onCheckedChange = { publico = it },
                colors = SwitchDefaults.colors(checkedThumbColor = Lima)
            )
        }

        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            colors = ButtonDefaults.buttonColors(containerColor = Lima)
        ) {
            Icon(Icons.Default.Image, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Seleccionar imagen")
        }

        imagenFinal?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
        }

        Button(
            onClick = {
                val evento = com.example.bailotecaapp.model.Evento(
                    id = eventoSeleccionado?.id ?: 0L,
                    nombre = nombre,
                    descripcion = descripcion,
                    fecha = "${fecha}T00:00:00",
                    lugar = lugar,
                    publico = publico,
                    nombreOrganizador = usuario?.nombre ?: "",
                    urlImagen = imagenSubidaNombre.value
                )

                scope.launch {
                    val ok = if (eventoSeleccionado != null) {
                        eventoViewModel.actualizarEvento(token!!, evento.id, evento)
                    } else {
                        eventoViewModel.crearEvento(token!!, evento)
                    }

                    if (ok) {
                        Toast.makeText(context, "✅ Evento guardado", Toast.LENGTH_SHORT).show()
                        eventoViewModel.limpiarEventoSeleccionado()
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, "❌ Error al guardar evento", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Magenta),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Guardar")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun campoColores() = TextFieldDefaults.outlinedTextFieldColors(
    containerColor = Color.White,
    cursorColor = Magenta,
    focusedBorderColor = Magenta,
    unfocusedBorderColor = Color.Gray,
    focusedLabelColor = Magenta,
    unfocusedLabelColor = Color.DarkGray
)