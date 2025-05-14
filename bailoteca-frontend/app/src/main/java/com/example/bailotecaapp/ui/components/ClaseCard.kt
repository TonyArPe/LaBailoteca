package com.example.bailotecaapp.ui.components

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.bailotecaapp.model.Clase
import com.example.bailotecaapp.model.Inscripcion
import com.example.bailotecaapp.model.Usuario
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
    onInscribirse: ((Long) -> Unit)? = null,
    onVerDetalle: (Long) -> Unit
) {
    val context = LocalContext.current

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

            Spacer(modifier = Modifier.height(8.dp))

            if (clase.horarioClases.isNotEmpty()) {
                clase.horarioClases.forEach { horario ->
                    Text("• ${horario.diaSemana} ${horario.horaInicio} - ${horario.horaFin}")
                }
            } else {
                Text("Horarios no disponibles", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (usuarioActual != null) {
                    when (usuarioActual.rol) {
                        Rol.INVITADO -> {
                            IconButton(onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = "mailto:${clase.profesor.correo}".toUri()
                                    putExtra(Intent.EXTRA_SUBJECT, "Interesado en ${clase.nombre}")
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Hola ${clase.profesor.nombre},\n\nEstoy interesado en la clase '${clase.nombre}'. ¿Podrías darme más información?\n\nGracias."
                                    )
                                }
                                context.startActivity(intent)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Enviar correo",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        else -> {
                            if (onInscribirse != null && !yaInscrito) {
                                Button(onClick = { onInscribirse(clase.id) }) {
                                    Text("Inscribirse")
                                }
                            } else {
                                Text("Ya estás inscrito", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}