package com.example.bailotecaapp.ui.screens.clases

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bailotecaapp.model.dto.HorarioClaseRequest

/**
 * Diálogo/componente para editar o crear un nuevo horario de clase.
 *
 * @param horario Horario a editar o null si se crea nuevo.
 * @param onConfirm Acción al confirmar el horario.
 * @param onDismiss Acción al cancelar la edición.
 * @param onChange Acción cuando se modifica algún campo.
 * @param onDelete Acción al pulsar eliminar (visible solo en edición).
 */
@Composable
fun HorarioEditor(
    horario: HorarioClaseRequest?,
    onConfirm: (HorarioClaseRequest) -> Unit,
    onDismiss: () -> Unit,
    onChange: (HorarioClaseRequest) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val horarioEditable = horario ?: HorarioClaseRequest("", "", "")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir horario", style = MaterialTheme.typography.titleMedium) },
        confirmButton = {
            Button(
                onClick = {
                    Log.d("HorarioEditor", "✅ Confirmando horario: $horarioEditable")
                    onConfirm(horarioEditable)
                }
            ) { Text("Aceptar") }
        },
        dismissButton = {
            TextButton(onClick = {
                Log.d("HorarioEditor", "❌ Cancelado")
                onDismiss()
            }) {
                Text("Cancelar")
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = horarioEditable.diaSemana,
                    onValueChange = {
                        onChange(horarioEditable.copy(diaSemana = it))
                    },
                    label = { Text("Día de la semana") }
                )
                OutlinedTextField(
                    value = horarioEditable.horaInicio,
                    onValueChange = {
                        onChange(horarioEditable.copy(horaInicio = it))
                    },
                    label = { Text("Hora inicio (HH:mm)") }
                )
                OutlinedTextField(
                    value = horarioEditable.horaFin,
                    onValueChange = {
                        onChange(horarioEditable.copy(horaFin = it))
                    },
                    label = { Text("Hora fin (HH:mm)") }
                )

                if (onDelete != null) {
                    Button(
                        onClick = {
                            Log.w("HorarioEditor", "🗑 Eliminando horario: $horarioEditable")
                            onDelete()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Eliminar horario")
                    }
                }
            }
        }
    )
}