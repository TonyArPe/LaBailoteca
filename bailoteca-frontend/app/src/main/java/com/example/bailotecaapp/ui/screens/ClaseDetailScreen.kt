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
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.ui.components.SesionGuard
import com.example.bailotecaapp.viewmodel.ClaseViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Pantalla de detalle de clase, con renderizado adaptado al rol del usuario.
 * Los invitados pueden ver el contenido, pero no pueden inscribirse ni cancelar.
 */
@Composable
fun ClaseDetailScreen(
    navController: NavHostController,
    claseId: Long,
    claseViewModel: ClaseViewModel = hiltViewModel(),
    sesionViewModel: SesionViewModel = hiltViewModel()
) {
    SesionGuard(sesionViewModel) { usuario ->

        val clase by claseViewModel.claseSeleccionada.collectAsState()
        val inscripciones by sesionViewModel.inscripciones.collectAsState()
        val uriHandler = LocalUriHandler.current
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        val esInvitado = usuario.rol == Rol.INVITADO

        // Cargar clase al entrar
        LaunchedEffect(claseId) {
            delay(150)
            claseViewModel.cargarClase(claseId)
        }

        // Solo si no es invitado, cargar sus inscripciones
        LaunchedEffect(usuario.id) {
            if (!esInvitado) {
                sesionViewModel.cargarMisInscripciones()
            }
        }

        // Inscripción actual si existe
        val inscripcionActual = inscripciones.find { it.clase.id == claseId }
        val estaInscrito = inscripcionActual != null

        fun desinscribirse(inscripcionId: Long) {
            coroutineScope.launch {
                try {
                    val token = Firebase.auth.currentUser?.getIdToken(false)?.await()?.token ?: return@launch
                    val response = claseViewModel.eliminarInscripcion(token, inscripcionId)

                    if (response.isSuccessful) {
                        Toast.makeText(context, "Inscripción cancelada", Toast.LENGTH_SHORT).show()
                        sesionViewModel.cargarMisInscripciones()
                    } else {
                        Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // UI de clase
        Scaffold { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                clase?.let { c ->
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(c.nombre, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
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
                        c.horarioClases.forEach { horario ->
                            Text("• ${horario.diaSemana}: ${horario.horaInicio} - ${horario.horaFin}")
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Solo si es usuario normal o profesor, no invitado
                        if (!esInvitado && estaInscrito && inscripcionActual != null) {
                            Button(
                                onClick = { desinscribirse(inscripcionActual.id) },
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
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
                } ?: CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}