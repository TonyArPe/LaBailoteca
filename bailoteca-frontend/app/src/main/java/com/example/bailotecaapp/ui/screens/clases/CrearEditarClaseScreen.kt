// CRUD para profesores sobre sus propias clases (versión inicial sin UI refinada)

package com.example.bailotecaapp.ui.screens.clases

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.bailotecaapp.model.dto.ClaseRequest
import com.example.bailotecaapp.model.enums.Dificultad
import com.example.bailotecaapp.ui.components.SesionGuard
import com.example.bailotecaapp.viewmodel.ClaseViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla para crear o editar una clase por parte de un profesor.
 * El ViewModel se reutiliza, y se determina si es edición por el ID recibido (si hay).
 */
@Composable
fun CrearEditarClaseScreen(
    navController: NavController,
    claseId: Long? = null,
    claseViewModel: ClaseViewModel = hiltViewModel(),
    sesionViewModel: SesionViewModel = hiltViewModel()
) {
    SesionGuard(navController = navController, sesionViewModel = sesionViewModel) { usuario ->

        if (usuario.id == null) return@SesionGuard

        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        val clase by claseViewModel.claseSeleccionada.collectAsState()
        val isEditing = claseId != null

        var nombre by remember { mutableStateOf(TextFieldValue()) }
        var descripcion by remember { mutableStateOf(TextFieldValue()) }
        var ubicacion by remember { mutableStateOf(TextFieldValue()) }
        var dificultad: Dificultad? by remember { mutableStateOf(Dificultad.INICIAL) }
        var video by remember { mutableStateOf(TextFieldValue()) }

        // Al cargar en edición
        LaunchedEffect(claseId) {
            if (isEditing && claseId != null) {
                claseViewModel.cargarClase(claseId)
            }
        }

        // Rellenar campos en edición
        LaunchedEffect(clase) {
            clase?.let {
                nombre = TextFieldValue(it.nombre)
                descripcion = TextFieldValue(it.descripcion)
                ubicacion = TextFieldValue(it.ubicacion)
                dificultad = it.dificultad
                video = TextFieldValue(it.videoPresentacion)
            }
        }

        Scaffold { padding ->
            Column(
                modifier = Modifier.padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(if (isEditing) "Editar Clase" else "Nueva Clase", style = MaterialTheme.typography.headlineSmall)

                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") })
                OutlinedTextField(value = ubicacion, onValueChange = { ubicacion = it }, label = { Text("Ubicación") })
                OutlinedTextField(value = video, onValueChange = { video = it }, label = { Text("Video presentación") })

                // Selector de dificultad (enum)
                DropdownMenuBox(selected = dificultad, onSelected = { dificultad = it })

                Button(
                    onClick = {
                        val request = ClaseRequest(
                            nombre = nombre.text,
                            descripcion = descripcion.text,
                            ubicacion = ubicacion.text,
                            dificultad = dificultad,
                            videoPresentacion = video.text,
                            horarioClases = emptyList()
                        )

                        scope.launch {
                            val token = sesionViewModel.token.value ?: return@launch
                            val response = if (isEditing && claseId != null) {
                                claseViewModel.actualizarClase(token, claseId, request)
                            } else {
                                claseViewModel.crearClase(token, request)
                            }

                            if (response.isSuccessful) {
                                Toast.makeText(context, "Clase guardada con éxito", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                                sesionViewModel.marcarClasesComoActualizadas()
                            } else {
                                Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}

@Composable
fun DropdownMenuBox(selected: Dificultad?, onSelected: (Dificultad) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text("Dificultad: ${selected?.name?.lowercase()?.replaceFirstChar { it.uppercase() }}")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Dificultad.values().forEach {
                DropdownMenuItem(text = { Text(it.name) }, onClick = {
                    onSelected(it)
                    expanded = false
                })
            }
        }
    }
}