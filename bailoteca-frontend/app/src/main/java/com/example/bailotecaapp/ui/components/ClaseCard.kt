package com.example.bailotecaapp.ui.components

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
 * El contenido mostrado depende del rol y del estado de inscripción del usuario:
 *
 * @param clase Clase a mostrar.
 * @param usuarioActual Usuario autenticado que visualiza la tarjeta.
 * @param inscripciones Lista de inscripciones activas del usuario.
 * @param yaInscrito Indica si el usuario ya está inscrito en esta clase.
 * @param onInscribirse Acción que se ejecuta al pulsar el botón "Inscribirme".
 * @param onVerDetalle Acción al pulsar en la tarjeta para ver más detalles.
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
    val yaInscrito by remember(inscripciones) {
        derivedStateOf {
            inscripciones.any { it.clase.id == clase.id }
        }
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onVerDetalle(clase.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(clase.nombre, style = MaterialTheme.typography.titleMedium)
            Text(clase.descripcion, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))

            Text("Profesor: ${clase.profesor.nombre}", style = MaterialTheme.typography.labelSmall)

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

            when (usuarioActual?.rol) {
                Rol.USUARIO -> {
                    if (yaInscrito) {
                        OutlinedButton(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ya inscrito")
                        }
                    } else {
                        Button(
                            onClick = { onInscribirse(clase.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Inscribirme")
                        }
                    }
                }

                Rol.INVITADO -> {
                    val correoProfesor = clase.profesor.correo
                    if (!correoProfesor.isNullOrBlank()) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = "mailto:$correoProfesor".toUri()
                                    putExtra(Intent.EXTRA_SUBJECT, "Interesado en ${clase.nombre}")
                                    putExtra(Intent.EXTRA_TEXT, "Hola, estoy interesado en la clase '${clase.nombre}'. ¿Podrías darme más información?")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Contactar con el profesor")
                        }
                    } else {
                        Text("Correo del profesor no disponible", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Rol.ADMIN, Rol.PROFESOR -> {
                    // No mostrar acciones para roles con privilegios administrativos
                }

                else -> {
                    // Usuario sin rol definido o no autenticado
                }
            }
        }
    }
}