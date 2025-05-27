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

/**
 * Pantalla que muestra el detalle completo de una clase.
 * Incluye horarios, datos del profesor y posibilidad de inscripción/cancelación.
 * Modo invitado permite contactar con el profesor por Gmail.
 */
@Composable
fun ClaseDetailScreen(
    navController: NavHostController,
    claseId: Long,
    claseViewModel: ClaseViewModel = hiltViewModel()
) {
    SesionGuard(navController = navController) { usuario ->

        val sesionViewModel: SesionViewModel = hiltViewModel()
        val clase by claseViewModel.claseSeleccionada.collectAsState()
        val inscripciones by sesionViewModel.inscripciones.collectAsState()
        val versionClases by sesionViewModel.versionClases.collectAsState()

        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current
        val coroutineScope = rememberCoroutineScope()

        val esInvitado = usuario.rol == Rol.INVITADO
        val claseYaCargada = remember { mutableStateOf(false) }

        LaunchedEffect(claseId) {
            if (!claseYaCargada.value) {
                Log.d("ClaseDetailScreen", "🔄 Cargando clase ID: $claseId")
                claseViewModel.cargarClase(claseId)
                claseYaCargada.value = true
            }
        }

        LaunchedEffect(versionClases) {
            if (!esInvitado) {
                Log.d("ClaseDetailScreen", "🔁 Recargando inscripciones")
                sesionViewModel.cargarMisInscripciones()
            }
        }

        val inscripcionActual = remember(inscripciones) {
            inscripciones.find { it.clase.id == claseId }
        }

        val estaInscrito = inscripcionActual != null

        fun cancelarInscripcion(inscripcionId: Long) {
            coroutineScope.launch {
                try {
                    val token = Firebase.auth.currentUser?.getIdToken(false)?.await()?.token
                    if (token.isNullOrEmpty()) {
                        Toast.makeText(context, "Token inválido", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val response = claseViewModel.eliminarInscripcion(token, inscripcionId)

                    if (response.isSuccessful) {
                        Toast.makeText(context, "Inscripción cancelada", Toast.LENGTH_SHORT).show()
                        sesionViewModel.cargarMisInscripciones()
                        sesionViewModel.marcarClasesComoActualizadas()
                    } else {
                        Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(c.nombre, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                        Text(c.descripcion)

                        Divider()

                        Text("📍 Ubicación: ${c.ubicacion}")
                        val dificultadTexto = when (c.dificultad) {
                            Dificultad.INICIAL -> "Inicial"
                            Dificultad.INTERMEDIO -> "Intermedio"
                            Dificultad.AVANZADO -> "Avanzado"
                            else -> "Sin especificar"
                        }
                        Text("🎯 Dificultad: $dificultadTexto")

                        Text("👨‍🏫 Profesor: ${c.profesor.nombre}")

                        if (!esInvitado && c.videoPresentacion.isNotBlank()) {
                            ClickableText(
                                text = AnnotatedString("🎥 Ver video de presentación"),
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.secondary),
                                onClick = {
                                    uriHandler.openUri(c.videoPresentacion)
                                }
                            )
                        }

                        Divider()

                        Text("🕒 Horarios:", style = MaterialTheme.typography.titleSmall)
                        c.horarioClases.forEach {
                            Text("• ${it.diaSemana}: ${it.horaInicio} - ${it.horaFin}")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botones según rol
                        when (usuario.rol) {
                            Rol.USUARIO -> {
                                if (estaInscrito && inscripcionActual != null) {
                                    Button(
                                        onClick = { cancelarInscripcion(inscripcionActual.id) },
                                        modifier = Modifier.align(Alignment.CenterHorizontally),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Cancelar inscripción")
                                    }
                                }
                            }

                            Rol.INVITADO -> {
                                Button(
                                    onClick = {
                                        val asunto = Uri.encode("Consulta sobre la clase: ${c.nombre}")
                                        val mensaje = Uri.encode("Hola ${c.profesor.nombre},\n\nEstoy interesado en tu clase '${c.nombre}'. ¿Podrías darme más información?\n\nGracias.")
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = "mailto:${c.profesor.correo}?subject=$asunto&body=$mensaje".toUri()
                                        }
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Contactar al profesor")
                                }
                            }

                            else -> {}
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