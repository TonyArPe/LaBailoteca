package com.example.bailotecaapp.ui.screens.usuarios.perfil

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bailotecaapp.model.Clase

/**
 * Tarjeta visual para mostrar una clase inscrita por un usuario, incluyendo profesor y horarios.
 *
 * @param clase Clase a renderizar.
 */
@Composable
fun ClaseInscritaCard(clase: Clase) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = clase.nombre,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Profesor: ${clase.profesor.nombre} ${clase.profesor.apellido}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Ubicación: ${clase.ubicacion}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (clase.horarioClases.isNotEmpty()) {
                clase.horarioClases.forEach { horario ->
                    Text(
                        text = "${horario.diaSemana}: ${horario.horaInicio} - ${horario.horaFin}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Text(
                    text = "Sin horarios definidos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}