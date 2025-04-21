package com.example.bailotecaapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bailotecaapp.model.Clase

@Composable
fun ClaseCard(clase: Clase) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(clase.nombre, style = MaterialTheme.typography.titleMedium)
            Text(clase.descripcion, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Profesor: ${clase.profesor.nombre}", style = MaterialTheme.typography.labelSmall)
            if (clase.horarioClases.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Horarios:", style = MaterialTheme.typography.labelSmall)
                clase.horarioClases.forEach {
                    Text("- ${it.diaSemana} ${it.horaInicio} - ${it.horaFin}",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
