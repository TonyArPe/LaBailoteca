package com.example.bailotecaapp.ui.components

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.bailotecaapp.model.Clase
import com.example.bailotecaapp.model.Inscripcion
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.enums.Rol
import androidx.core.net.toUri

/**
 * Tarjeta visual de una clase que muestra su información básica.
 * Adapta el contenido mostrado según el rol del usuario:
 * - USUARIO: puede inscribirse o ver "Ya inscrito".
 * - INVITADO: puede contactar con el profesor por correo.
 * - Otros roles (ADMIN, PROFESOR): solo lectura.
 */
@Composable
fun ClaseCard(
    clase: Clase,
    usuarioActual: Usuario?,
    inscripciones: List<Inscripcion>,
    yaInscrito: Boolean,
    onInscribirse: (Long) -> Unit,
    onVerDetalle: (Long) -> Unit,
    onEditarClase: ((Long) -> Unit)? = null,
    onEliminarClase: ((Long) -> Unit)? = null
) {
    val context = LocalContext.current
    val colorFondo = if (yaInscrito)
        MaterialTheme.colorScheme.secondaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onVerDetalle(clase.id) },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = clase.nombre,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = clase.descripcion,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("📍 ${clase.ubicacion}", style = MaterialTheme.typography.labelSmall)
            clase.dificultad?.let {
                Text("🔥 Dificultad: ${it.name.lowercase().replaceFirstChar { c -> c.uppercase() }}",
                    style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("👤 ${clase.profesor.nombre}", style = MaterialTheme.typography.labelSmall)

                when (usuarioActual?.rol) {
                    Rol.USUARIO -> {
                        Button(
                            onClick = { if (!yaInscrito) onInscribirse(clase.id) },
                            enabled = !yaInscrito,
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (yaInscrito)
                                    MaterialTheme.colorScheme.secondary
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(if (yaInscrito) "Ya inscrito" else "Inscribirse")
                        }
                    }

                    Rol.INVITADO -> {
                        IconButton(onClick = {
                            val asunto = Uri.encode("Consulta sobre la clase: ${clase.nombre}")
                            val mensaje = Uri.encode("Hola ${clase.profesor.nombre},\n\nEstoy interesado en tu clase '${clase.nombre}'. ¿Podrías darme más información?\n\nGracias.")
                            val uri = "mailto:${clase.profesor.correo}?subject=$asunto&body=$mensaje".toUri()
                            context.startActivity(Intent(Intent.ACTION_SENDTO).apply { data = uri })
                        }) {
                            Icon(Icons.Default.Email, contentDescription = "Contactar")
                        }
                    }

                    Rol.ADMIN -> {
                        // 🎯 NUEVOS BOTONES
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                onEditarClase?.invoke(clase.id)
                            }) {
                                Text("Editar")
                            }

                            OutlinedButton(
                                onClick = {
                                    onEliminarClase?.invoke(clase.id)
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Eliminar")
                            }
                        }
                    }

                    else -> {}
                }
            }

            LaunchedEffect(yaInscrito) {
                Log.d("ClaseCard", "Recomposición para clase ${clase.id}, yaInscrito=$yaInscrito")
            }
        }
    }
}