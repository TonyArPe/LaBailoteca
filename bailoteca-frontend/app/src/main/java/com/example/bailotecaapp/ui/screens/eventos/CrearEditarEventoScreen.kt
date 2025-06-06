package com.example.bailotecaapp.ui.screens.eventos

import android.net.Uri
import android.os.Build
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.bailotecaapp.model.Evento
import com.example.bailotecaapp.model.enums.EstadoEvento
import com.example.bailotecaapp.ui.theme.Lima
import com.example.bailotecaapp.ui.theme.Magenta
import com.example.bailotecaapp.viewmodel.EventoViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Pantalla para crear o editar un evento.
 */
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CrearEditarEventoScreen(
    navController: NavController,
    sesionViewModel: SesionViewModel = hiltViewModel(),
    eventoViewModel: EventoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val token = sesionViewModel.token.collectAsState().value
    val usuario = sesionViewModel.usuario.collectAsState().value
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(LocalDate.now()) }
    var lugar by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf(EstadoEvento.ACTIVO) }
    var publico by remember { mutableStateOf(true) }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imagenUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.Black),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Crear o editar evento", style = MaterialTheme.typography.headlineSmall, color = Lima)

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del evento") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Magenta,
                focusedLabelColor = Magenta
            )
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Magenta,
                focusedLabelColor = Magenta
            )
        )

        OutlinedTextField(
            value = lugar,
            onValueChange = { lugar = it },
            label = { Text("Lugar") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Magenta,
                focusedLabelColor = Magenta
            )
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Lima)
            Text(
                "Fecha: ${fecha.format(DateTimeFormatter.ISO_DATE)}",
                modifier = Modifier.padding(start = 8.dp),
                color = Color.White
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Evento público", color = Color.White)
            Switch(
                checked = publico,
                onCheckedChange = { publico = it },
                colors = SwitchDefaults.colors(checkedThumbColor = Lima)
            )
        }

        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            colors = ButtonDefaults.buttonColors(containerColor = Magenta),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Image, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Seleccionar imagen")
        }

        imagenUri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.Gray, RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (token.isNullOrBlank() || usuario == null) {
                    Toast.makeText(context, "Sesión no disponible", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val evento = Evento(
                    id = 0L,
                    nombre = nombre,
                    descripcion = descripcion,
                    fecha = fecha.toString(),
                    lugar = lugar,
                    estado = estado,
                    organizador = usuario,
                    publico = publico
                )

                scope.launch {
                    val ok = eventoViewModel.crearEvento(token, evento)
                    if (ok) {
                        Toast.makeText(context, "Evento guardado", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, "Error al guardar evento", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.align(Alignment.End),
            colors = ButtonDefaults.buttonColors(containerColor = Lima)
        ) {
            Text("Guardar")
        }
    }
}