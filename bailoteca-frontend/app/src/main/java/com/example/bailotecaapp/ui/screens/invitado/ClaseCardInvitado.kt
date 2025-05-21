package com.example.bailotecaapp.ui.screens.invitado

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Duration
import java.time.LocalTime
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.bailotecaapp.model.Clase
import androidx.core.net.toUri

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ClaseCardInvitado(clase: Clase) {
    val context = LocalContext.current

    // Calcular duración de la primera franja horaria (si existe)
    val duracionMinutos = clase.horarioClases.firstOrNull()?.let { horario ->
        try {
            val horaInicio = LocalTime.parse(horario.horaInicio)
            val horaFin = LocalTime.parse(horario.horaFin)
            Duration.between(horaInicio, horaFin).toMinutes().toInt()
        } catch (e: Exception) {
            null
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(clase.nombre, style = MaterialTheme.typography.titleLarge)
            Text("Profesor: ${clase.profesor.nombre}")
            Text("Dificultad: ${clase.dificultad ?: "No especificada"}")
            Text("Duración: ${duracionMinutos ?: "?"} min")
            Text("Descripción: ${clase.descripcion}")

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    setData("mailto:${clase.profesor.correo}".toUri())
                    putExtra(Intent.EXTRA_SUBJECT, "Interesado en clase '${clase.nombre}'")
                    putExtra(Intent.EXTRA_TEXT, "Hola ${clase.profesor.nombre}, estoy interesado en tu clase. ¿Podrías darme más información?")
                }
                context.startActivity(intent)
            }) {
                Text("Contactar por Gmail")
            }
        }
    }
}