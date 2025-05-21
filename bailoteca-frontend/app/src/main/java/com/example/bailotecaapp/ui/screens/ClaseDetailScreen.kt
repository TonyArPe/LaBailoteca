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
    claseViewModel: ClaseViewModel = hiltViewModel()
) {
    SesionGuard(navController = navController) { usuario ->

        val sessionViewModel: SesionViewModel = hiltViewModel()
        val versionClases by sessionViewModel.versionClases.collectAsState()
        val clase by claseViewModel.claseSeleccionada.collectAsState()
        val inscripciones by sessionViewModel.inscripciones.collectAsState()
        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current
        val coroutineScope = rememberCoroutineScope()

        val esInvitado = usuario.rol == Rol.INVITADO
        val claseYaCargada = remember { mutableStateOf(false) }

        LaunchedEffect(claseId) {
            if (!claseYaCargada.value) {
                Log.d("ClaseDetailScreen", "CARGANDO CLASE CON ID: $claseId")
                claseYaCargada.value = true
                claseViewModel.cargarClase(claseId)
            }
        }

        LaunchedEffect(versionClases) {
            if (!esInvitado) {
                Log.d("ClaseDetailScreen", "DETONADA LA BOMBA DE INSCRIPCIONES")
                sessionViewModel.cargarMisInscripciones()
            }
        }

        val inscripcionActual by remember(inscripciones, claseId) {
            derivedStateOf {
                val encontrada = inscripciones.find { it.clase.id == claseId }
                Log.d("ClaseDetailScreen", "INSCRIPCION ACTUAL: ${encontrada?.id ?: "Ninguna"} para claseId=$claseId")
                encontrada
            }
        }

        val estaInscrito by remember(inscripcionActual) {
            derivedStateOf {
                val resultado = inscripcionActual != null
                Log.d("ClaseDetailScreen", "¿ESTA INSCRITO? -> $resultado")
                resultado
            }
        }

        fun desinscribirse(inscripcionId: Long) {
            coroutineScope.launch {
                try {
                    val token = Firebase.auth.currentUser?.getIdToken(false)?.await()?.token
                    if (token == null) {
                        Log.e("ClaseDetailScreen", "TOKEN JWTES NULL, CANCELANDO ACCION")
                        return@launch
                    }
                    Log.d("ClaseDetailScreen", "ENVIANDO SOLICITUD PARA ELIMINAR INSCRIPCION CONId=$inscripcionId")
                    val response = claseViewModel.eliminarInscripcion(token, inscripcionId)

                    if (response.isSuccessful) {
                        Toast.makeText(context, "Inscripción cancelada", Toast.LENGTH_SHORT).show()
                        Log.d("ClaseDetailScreen", "INSCRIPCION ELIMINADA CON EXITO")
                        sessionViewModel.cargarMisInscripciones()
                        sessionViewModel.marcarClasesComoActualizadas()
                    } else {
                        Log.e("ClaseDetailScreen", "ERROR HTP AL ELIMINAR LA INSCRIPCION: ${response.code()}")
                        Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("ClaseDetailScreen", "EXCEPCION AL DESESCRIBIRSE", e)
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
                    Log.d("ClaseDetailScreen", "CLASE CARGADA: ${c.nombre}, dificultad: ${c.dificultad}")

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
                                    Log.d("ClaseDetailScreen", "ABRIENDO VIDEO DE PRESENTACION: ${c.videoPresentacion}")
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
                            Log.d("ClaseDetailScreen", "MOSTRANDO BOTON PARA CANCELAR SUBSCRIPCION")
                            Button(
                                onClick = { desinscribirse(inscripcionActual!!.id) },
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
                                    Log.d("ClaseDetailScreen", "INTENTANDO CONTACTAR CON EL PROFESOR: ${c.profesor.correo}")
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("Contactar al profesor")
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                Log.d("ClaseDetailScreen", "VOLVIENDO ATRAS")
                                navController.popBackStack()
                            },
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