package com.example.bailotecaapp.ui.components

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.bailotecaapp.model.Clase
import com.example.bailotecaapp.model.Inscripcion
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.enums.Dificultad
import com.example.bailotecaapp.model.enums.Rol
import androidx.core.net.toUri

/**
 * Componente visual que muestra una tarjeta con la información básica de una clase.
 * Si el usuario no está inscrito, permite inscribirse.
 * Si ya está inscrito, muestra un mensaje indicando el estado.
 * Si es invitado, se ofrece botón para contactar con el profesor.
 *
 * @param clase Clase a mostrar.
 * @param usuarioActual Usuario autenticado que visualiza la tarjeta.
 * @param inscripciones Lista de inscripciones activas del usuario.
 * @param yaInscrito Indica si el usuario ya está inscrito en la clase.
 * @param onInscribirse Acción que se ejecuta cuando el usuario pulsa el botón de inscripción.
 * @param onVerDetalle Acción que se ejecuta al pulsar en la tarjeta para ver más detalles.
 */
@Composable
fun ClaseCard(
    clase: Clase,
    usuarioActual: Usuario?,
    inscripciones: List<Inscripcion>,
    yaInscrito: Boolean,
    onInscribirse: (Long) -> Unit,
    onVerDetalle: (Long) -> Unit
) {
    val context = LocalContext.current

    Log.d("ClaseCard", "Renderizando tarjeta para clase: ${clase.nombre} con dificultad: ${clase.dificultad}")
    Log.d("ClaseCard", "Usuario actual: ${usuarioActual?.nombre}, Rol: ${usuarioActual?.rol}, yaInscrito: $yaInscrito")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                Log.d("ClaseCard", "Clic en claseId=${clase.id} para ver detalles")
                onVerDetalle(clase.id)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(clase.nombre, style = MaterialTheme.typography.titleMedium)
            Text(clase.descripcion, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))

            Text("Profesor: ${clase.profesor.nombre}", style = MaterialTheme.typography.labelSmall)

            // Dificultad (con fallback)
            val dificultadTexto = when (clase.dificultad) {
                null -> "Sin especificar"
                Dificultad.INICIAL -> "Inicial"
                Dificultad.INTERMEDIO -> "Intermedio"
                Dificultad.AVANZADO -> "Avanzado"
            }
            Text("Nivel: $dificultadTexto", style = MaterialTheme.typography.labelSmall)

            Spacer(modifier = Modifier.height(8.dp))

            if (clase.horarioClases.isNotEmpty()) {
                clase.horarioClases.forEach { horario ->
                    Text("• ${horario.diaSemana} ${horario.horaInicio} - ${horario.horaFin}")
                }
            } else {
                Text("Horarios no disponibles", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Logs útiles para depuración
            Log.d("ClaseCard", "Inscripciones actuales del usuario: ${inscripciones.map { it.clase.id }}")

            if (usuarioActual?.rol != null) {
                when (usuarioActual.rol) {
                    Rol.USUARIO -> {
                        if (!yaInscrito) {
                            Button(onClick = {
                                Log.d("ClaseCard", "Usuario se quiere inscribir a claseId=${clase.id}")
                                onInscribirse(clase.id)
                            }) {
                                Text("Inscribirme")
                            }
                        } else {
                            Text("Ya estás inscrito", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Rol.INVITADO -> {
                        val correoProfesor = clase.profesor.correo
                        if (!correoProfesor.isNullOrBlank()) {
                            Button(onClick = {
                                Log.d("ClaseCard", "Invitado quiere contactar con ${correoProfesor}")
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = "mailto:$correoProfesor".toUri()
                                    putExtra(Intent.EXTRA_SUBJECT, "Interesado en ${clase.nombre}")
                                    putExtra(Intent.EXTRA_TEXT, "Hola, estoy interesado en la clase '${clase.nombre}'. ¿Podrías darme más información?")
                                }
                                context.startActivity(intent)
                            }) {
                                Text("Contactar por email")
                            }
                        } else {
                            Log.w("ClaseCard", "Correo del profesor no disponible para clase: ${clase.id}")
                            Text("Correo del profesor no disponible")
                        }
                    }

                    Rol.ADMIN, Rol.PROFESOR -> {
                        // No mostramos botón en estos casos
                        Log.d("ClaseCard", "Rol ${usuarioActual.rol} no requiere botón de acción")
                    }

                    else -> {
                        Log.w("ClaseCard", "Rol inesperado: ${usuarioActual.rol}")
                    }
                }
            } else {
                Log.w("ClaseCard", "Usuario actual es null o sin rol")
            }
        }
    }
}