package com.example.bailotecaapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bailotecaapp.model.Clase
import com.example.bailotecaapp.model.Inscripcion
import com.example.bailotecaapp.model.Usuario
import android.util.Log

/**
 * Muestra una tarjeta con la información de una clase, permitiendo inscribirse si el usuario no está ya inscrito.
 */
@Composable
fun ClaseCard(
    clase: Clase,
    usuarioActual: Usuario?,
    inscripciones: List<Inscripcion>,
    onInscribirse: (Long) -> Unit,
    onVerDetalle: (Long) -> Unit
) {
    Log.d("ClaseCard", "usuarioActual: ${usuarioActual?.id}, clase.id: ${clase.id}")
    Log.d("ClaseCard", "Inscripciones del usuario: ${inscripciones.map { it.claseId }}")

    val yaInscrito = inscripciones.any { it.claseId == clase.id }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onVerDetalle(clase.id) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(clase.nombre, style = MaterialTheme.typography.titleMedium)
            Text(clase.descripcion, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))

            Text("Profesor: ${clase.profesor.nombre}", style = MaterialTheme.typography.labelSmall)

            Spacer(modifier = Modifier.height(8.dp))

            if (clase.horarioClases.isNotEmpty()) {
                clase.horarioClases.forEach { horario ->
                    Text(text = "${horario.diaSemana} ${horario.horaInicio}")
                }
            } else {
                Text("Horarios no disponibles", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (usuarioActual != null && !yaInscrito) {
                Button(onClick = { onInscribirse(clase.id) }) {
                    Text("Inscribirme")
                }
            } else if (yaInscrito) {
                Text("Ya estás inscrito", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}