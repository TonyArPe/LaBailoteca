package com.example.bailotecaapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.viewmodel.ClaseViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.example.bailotecaapp.network.RetrofitInstance
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Pantalla que muestra el detalle de una clase.
 */
@Composable
fun ClaseDetailScreen(
    navController: NavHostController,
    claseId: Long,
    viewModel: ClaseViewModel = viewModel(),
    sesionViewModel: SesionViewModel = viewModel()
) {
    val clase by viewModel.claseSeleccionada.collectAsState()
    val usuario by sesionViewModel.usuario.collectAsState()
    val inscripciones by sesionViewModel.inscripciones.collectAsState()
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Estado: buscamos si está inscrito en esta clase
    val inscripcionActual = inscripciones.find { it.claseId == claseId }

    /**
     * Cargar la clase y los datos del usuario al entrar
     */
    LaunchedEffect(claseId) {
        delay(150)
        viewModel.obtenerClasePorId(claseId)
    }

    LaunchedEffect(Unit) {
        sesionViewModel.cargarUsuarioActual()
    }

    LaunchedEffect(usuario?.id) {
        if (usuario != null) {
            sesionViewModel.cargarMisInscripciones()
        }
    }

    /**
     * Función para cancelar la inscripción en esta clase
     */
    fun cancelarInscripcion(inscripcionId: Long) {
        coroutineScope.launch {
            try {
                val token = Firebase.auth.currentUser?.getIdToken(false)?.await()?.token ?: return@launch
                val response = RetrofitInstance.api.eliminarInscripcion("Bearer $token", inscripcionId)

                if (response.isSuccessful) {
                    Toast.makeText(context, "Inscripción cancelada", Toast.LENGTH_SHORT).show()
                    sesionViewModel.cargarMisInscripciones()
                } else {
                    Toast.makeText(context, "Error al cancelar inscripción: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Interfaz visual
     */
    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            clase?.let { c ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = c.nombre,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(c.descripcion, style = MaterialTheme.typography.bodyMedium)

                    Divider()

                    Text("📍 Ubicación: ${c.ubicacion}")
                    Text("🎯 Dificultad: ${c.dificultad.name.lowercase().replaceFirstChar { it.uppercaseChar() }}")
                    Text("👨‍🏫 Profesor: ${c.profesor.nombre}")

                    if (c.videoPresentacion.isNotBlank()) {
                        ClickableText(
                            text = AnnotatedString("🎥 Ver video de presentación"),
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.secondary),
                            onClick = { uriHandler.openUri(c.videoPresentacion) }
                        )
                    }

                    Divider()
                    Text("🕒 Horarios:", style = MaterialTheme.typography.titleSmall)

                    (c.horarioClases ?: emptyList()).forEach { horario ->
                        Text("• ${horario.diaSemana}: ${horario.horaInicio} - ${horario.horaFin}")
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ✅ Mostrar botón para cancelar si el usuario está inscrito
                    if (inscripcionActual != null) {
                        Button(
                            onClick = { cancelarInscripcion(inscripcionActual.id) },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Cancelar inscripción")
                        }
                    }

                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Volver")
                    }
                }
            } ?: run {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}