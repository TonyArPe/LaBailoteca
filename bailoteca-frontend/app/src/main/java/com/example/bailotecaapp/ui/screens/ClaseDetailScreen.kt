package com.example.bailotecaapp.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
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
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.model.enums.Dificultad
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.ui.components.SesionGuard
import com.example.bailotecaapp.viewmodel.ClaseViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current
        val coroutineScope = rememberCoroutineScope()

        val esInvitado = usuario.rol == Rol.INVITADO

        val claseYaCargada = remember { mutableStateOf(false) }
        val inscripcionesYaCargadas = remember { mutableStateOf(false) }

        LaunchedEffect(claseId) {
            if (!claseYaCargada.value) {
                Log.d("ClaseDetailScreen", "Cargando clase con ID: $claseId")
                claseYaCargada.value = true
                claseViewModel.cargarClase(claseId)
            }
        }

        LaunchedEffect(usuario.id) {
            if (!esInvitado && !inscripcionesYaCargadas.value) {
                Log.d("ClaseDetailScreen", "Cargando inscripciones del usuario ID: ${usuario.id}")
                inscripcionesYaCargadas.value = true
                sesionViewModel.cargarMisInscripciones()
            }
        }

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
                    Log.e("ClaseDetailScreen", "Error al desinscribirse", e)
                }
            }
        }

        Scaffold { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                clase?.let { c ->
                    Log.d("ClaseDetailScreen", "Clase cargada: ${c.nombre}, dificultad: ${c.dificultad}")

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(c.nombre, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                        Text(c.descripcion, style = MaterialTheme.typography.bodyMedium)

                        Divider()

                        Text("📍 Ubicación: ${c.ubicacion}")
                        val dificultadTexto = when (c.dificultad) {
                            null -> "Sin especificar"
                            Dificultad.INICIAL -> "Inicial"
                            Dificultad.INTERMEDIO -> "Intermedio"
                            Dificultad.AVANZADO -> "Avanzado"
                        }
                        Text("🎯 Dificultad: $dificultadTexto")

                        Text("👨‍🏫 Profesor: ${c.profesor.nombre}")

                        if (!esInvitado && c.videoPresentacion.isNotBlank()) {
                            ClickableText(
                                text = AnnotatedString("🎥 Ver video de presentación"),
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.secondary),
                                onClick = {
                                    Log.d("ClaseDetailScreen", "Abriendo video presentación: ${c.videoPresentacion}")
                                    uriHandler.openUri(c.videoPresentacion)
                                }
                            )
                        }

                        Divider()
                        Text("🕒 Horarios:", style = MaterialTheme.typography.titleSmall)
                        c.horarioClases.forEach { horario ->
                            Text("• ${horario.diaSemana}: ${horario.horaInicio} - ${horario.horaFin}")
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (usuario.rol == Rol.USUARIO && estaInscrito && inscripcionActual != null) {
                            Button(
                                onClick = { desinscribirse(inscripcionActual.id) },
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Cancelar inscripción")
                            }
                        }

                        if (esInvitado) {
                            Button(
                                onClick = {
                                    val asunto = Uri.encode("Consulta sobre la clase: ${c.nombre}")
                                    val mensaje = Uri.encode("Hola ${c.profesor.nombre},\n\nEstoy interesado en tu clase '${c.nombre}'. ¿Podrías darme más información?\n\nGracias.")
                                    val correo = "mailto:${c.profesor.correo}?subject=$asunto&body=$mensaje".toUri()
                                    val intent = Intent(Intent.ACTION_SENDTO).apply { data = correo }
                                    Log.d("ClaseDetailScreen", "Intentando contactar al profesor: ${c.profesor.correo}")
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("Contactar al profesor")
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