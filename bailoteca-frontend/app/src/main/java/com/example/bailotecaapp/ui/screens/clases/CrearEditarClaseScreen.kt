package com.example.bailotecaapp.ui.screens.clases

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import android.util.Log
import com.example.bailotecaapp.model.dto.HorarioClaseRequest

/**
 * Pantalla para crear o editar una clase por parte de un profesor.
 * Incluye soporte para modificar horarios de forma visual.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
        var dificultad by remember { mutableStateOf<Dificultad?>(Dificultad.INICIAL) }
        var video by remember { mutableStateOf(TextFieldValue()) }
        var publica by remember { mutableStateOf(true) }

        val horarios = remember { mutableStateListOf<HorarioClaseRequest>() }
        var showHorarioDialog by remember { mutableStateOf(false) }
        var horarioEditable by remember { mutableStateOf<HorarioClaseRequest?>(null) }

        LaunchedEffect(claseId) {
            if (isEditing && claseId != null) {
                claseViewModel.cargarClase(claseId)
            }
        }

        LaunchedEffect(clase) {
            clase?.let {
                nombre = TextFieldValue(it.nombre)
                descripcion = TextFieldValue(it.descripcion)
                ubicacion = TextFieldValue(it.ubicacion)
                dificultad = it.dificultad
                video = TextFieldValue(it.videoPresentacion)
                publica = it.publica
                horarios.clear()
                horarios.addAll(it.horarioClases.map { h ->
                    HorarioClaseRequest(
                        diaSemana = h.diaSemana,
                        horaInicio = h.horaInicio.toString(),
                        horaFin = h.horaFin.toString()
                    )
                })
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isEditing) "Editar clase" else "Nueva clase",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") })
                OutlinedTextField(value = ubicacion, onValueChange = { ubicacion = it }, label = { Text("Ubicación") })
                OutlinedTextField(value = video, onValueChange = { video = it }, label = { Text("Video presentación") })

                DropdownMenuBox(selected = dificultad, onSelected = { dificultad = it })

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clase pública", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = publica,
                        onCheckedChange = { publica = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                }

                Text("Horarios", style = MaterialTheme.typography.titleMedium)
                horarios.forEachIndexed { index, horario ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("- ${horario.diaSemana}: ${horario.horaInicio} - ${horario.horaFin}")
                        TextButton(onClick = {
                            horarioEditable = horario
                            showHorarioDialog = true
                        }) {
                            Text("Editar")
                        }
                    }
                }

                Button(
                    onClick = {
                        horarioEditable = null
                        showHorarioDialog = true
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Añadir horario")
                }

                Button(
                    onClick = {
                        val request = ClaseRequest(
                            nombre = nombre.text,
                            descripcion = descripcion.text,
                            ubicacion = ubicacion.text,
                            dificultad = dificultad,
                            videoPresentacion = video.text,
                            horarioClases = horarios.toList(),
                            publica = publica
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

            if (showHorarioDialog) {
                HorarioEditor(
                    horario = horarioEditable,
                    onConfirm = { nuevo ->
                        if (horarioEditable != null) {
                            horarios.remove(horarioEditable)
                        }
                        horarios.add(nuevo)
                        showHorarioDialog = false
                        Log.d("CrearEditarClaseScreen", "✅ Horario confirmado: $nuevo")
                    },
                    onDismiss = {
                        Log.d("CrearEditarClaseScreen", "❌ Diálogo de horario cancelado")
                        showHorarioDialog = false
                    },
                    onChange = {
                        horarioEditable = it
                    },
                    onDelete = {
                        horarioEditable?.let { horarios.remove(it) }
                        showHorarioDialog = false
                        Log.d("CrearEditarClaseScreen", "🗑 Horario eliminado")
                    }
                )
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
            Dificultad.entries.forEach {
                DropdownMenuItem(text = { Text(it.name) }, onClick = {
                    onSelected(it)
                    expanded = false
                })
            }
        }
    }
}