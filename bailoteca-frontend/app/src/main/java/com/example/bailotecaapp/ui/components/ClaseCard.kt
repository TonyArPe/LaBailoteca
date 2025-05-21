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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onVerDetalle(clase.id) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = clase.nombre,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = clase.descripcion,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ubicación: ${clase.ubicacion}",
                style = MaterialTheme.typography.labelMedium
            )

            clase.dificultad?.let {
                Text(
                    text = "Dificultad: ${it.name.lowercase().replaceFirstChar { c -> c.uppercase() }}",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profesor: ${clase.profesor.nombre}",
                    style = MaterialTheme.typography.labelMedium
                )

                if (usuarioActual?.rol == Rol.USUARIO) {
                    Button(
                        onClick = {
                            if (!yaInscrito) onInscribirse(clase.id)
                        },
                        enabled = !yaInscrito,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (yaInscrito) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (yaInscrito) "Ya inscrito" else "Inscribirse")
                    }
                }

                if (usuarioActual?.rol == Rol.INVITADO) {
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            val asunto = Uri.encode("Consulta sobre la clase: ${clase.nombre}")
                            val mensaje = Uri.encode("Hola ${clase.profesor.nombre},\n\nEstoy interesado en tu clase '${clase.nombre}'. ¿Podrías darme más información?\n\nGracias.")
                            data =
                                "mailto:${clase.profesor.correo}?subject=$asunto&body=$mensaje".toUri()
                        }
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.Email, contentDescription = "Contactar")
                    }
                }
            }

            LaunchedEffect(yaInscrito) {
                Log.d("ClaseCard", "Recomposición para clase ${clase.id}, yaInscrito=$yaInscrito")
            }
        }
    }
}