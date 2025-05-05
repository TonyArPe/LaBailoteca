package com.example.bailotecaapp.ui.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bailotecaapp.model.Clase
import com.example.bailotecaapp.model.Inscripcion
import com.example.bailotecaapp.model.Usuario

/**
 * Componente visual que muestra una tarjeta con la información básica de una clase.
 * Si el usuario no está inscrito, permite inscribirse.
 * Si ya está inscrito, muestra un mensaje indicando el estado.
 *
 * @param clase Clase a mostrar.
 * @param usuarioActual Usuario autenticado que visualiza la tarjeta.
 * @param inscripciones Lista de inscripciones activas del usuario.
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

            Spacer(modifier = Modifier.height(8.dp))

            Log.d("ClaseCard", "yaInscrito=$yaInscrito, claseId=${clase.id}, inscripciones=${inscripciones.map { it.clase.id }}")

            if (usuarioActual != null) {
                if (!yaInscrito) {
                    Log.d("ClaseCard", "yaInscrito=${yaInscrito}, claseId=${clase.id}, inscripciones=${inscripciones.map { it.clase }}")
                    Button(onClick = { onInscribirse(clase.id) }) {
                        Text("Inscribirme")
                    }
                } else {
                    Text("Ya estás inscrito", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}