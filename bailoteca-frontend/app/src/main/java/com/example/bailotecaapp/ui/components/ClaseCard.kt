package com.example.bailotecaapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bailotecaapp.model.Clase
import com.example.bailotecaapp.model.Usuario

@Composable
fun ClaseCard(
    clase: Clase,
    usuarioActual: Usuario?,
    onInscribirse: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(clase.nombre, style = MaterialTheme.typography.titleMedium)
            Text(clase.descripcion, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))

            Text("Profesor: ${clase.profesor.nombre}", style = MaterialTheme.typography.labelSmall)

            Spacer(modifier = Modifier.height(8.dp))

            if (!clase.horarioClases.isNullOrEmpty()) {
                (clase.horarioClases).forEach { horario ->
                    Text(text = "${horario.diaSemana} ${horario.horaInicio}")
                }
            } else {
                Text(text = "Horarios no disponibles", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Protección segura para inscritos
            val yaInscrito = clase.inscritos?.any { it.id == usuarioActual?.id } == true

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