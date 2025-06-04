package com.example.bailotecaapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bailotecaapp.model.Inscripcion
import com.example.bailotecaapp.ui.theme.dificultadColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Componente que muestra los datos de una inscripción del usuario.
 * Incluye información de la clase, estado, checkbox de pago y botón de eliminar.
 *
 * @param inscripcion Datos de la inscripción.
 * @param esPagado Estado de pago actual del alumno.
 * @param onTogglePagado Acción para alternar el pago del alumno.
 * @param onEliminarInscripcion Acción para eliminar la inscripción (solo profesor dueño).
 * @param scope CoroutineScope para lanzar acciones suspendidas.
 */
@Composable
fun InscripcionCard(
    inscripcion: Inscripcion,
    esPagado: Boolean,
    onTogglePagado: (Boolean) -> Unit,
    onEliminarInscripcion: () -> Unit,
    scope: CoroutineScope
) {
    val clase = inscripcion.clase
    var pagado by remember { mutableStateOf(esPagado) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = clase.nombre, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Dificultad: ${clase.dificultad?.name ?: "Desconocida"}",
                color = dificultadColor(clase.dificultad)
            )
            Text(text = "Fecha inscripción: ${inscripcion.fechaInscripcion}")
            Spacer(Modifier.height(8.dp))
            AssistChip(
                onClick = {},
                label = { Text("Estado: ${inscripcion.estado.name}") },
                colors = AssistChipDefaults.assistChipColors()
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = pagado,
                        onCheckedChange = {
                            pagado = it
                            onTogglePagado(it)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Pagado", style = MaterialTheme.typography.bodyMedium)
                }

                IconButton(
                    onClick = {
                        scope.launch { onEliminarInscripcion() }
                    }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar inscripción")
                }
            }
        }
    }
}