package com.example.bailotecaapp.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bailotecaapp.model.Usuario

/**
 * Componente que representa visualmente a un usuario de la aplicación.
 * Muestra su nombre, correo, rol, y según el rol del usuario logueado,
 * permite eliminarlo o modificar su estado de actividad y pago.
 *
 * @param navController controlador de navegación para usar con acciones contextuales
 * @param usuario usuario a representar en la tarjeta
 * @param rolActual rol del usuario logueado que visualiza la tarjeta (ADMIN o PROFESOR)
 * @param estadoActivo estado actual de actividad (booleano real del backend)
 * @param estadoPagado estado actual de pago (booleano real del backend)
 * @param onEliminar callback ejecutado al pulsar eliminar
 * @param onModificarPagado callback ejecutado al pulsar el checkbox "Pagado"
 * @param onModificarActivo callback ejecutado al pulsar el checkbox "Activo"
 */
@Composable
fun UsuarioCard(
    navController: NavController,
    usuario: Usuario,
    rolActual: String,
    estadoActivo: Boolean,
    estadoPagado: Boolean,
    onEliminar: (Usuario) -> Unit,
    onModificarPagado: (Usuario) -> Unit,
    onModificarActivo: (Usuario) -> Unit
) {
    Log.d("UsuarioCard", "🧾 Renderizando tarjeta para: ${usuario.correo}, rol=$rolActual, activo=$estadoActivo, pagado=$estadoPagado")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("${usuario.nombre} ${usuario.apellido ?: ""}", style = MaterialTheme.typography.titleMedium)
            Text(usuario.correo, style = MaterialTheme.typography.bodyMedium)
            Text("Rol: ${usuario.rol}", style = MaterialTheme.typography.labelSmall)

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (rolActual == "ADMIN") {
                    IconButton(onClick = {
                        Log.d("UsuarioCard", "🗑️ ADMIN pulsa eliminar para: ${usuario.correo}")
                        onEliminar(usuario)
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = estadoActivo,
                            onCheckedChange = {
                                Log.d("UsuarioCard", "✅ ADMIN cambia ACTIVO=${!estadoActivo} para ${usuario.correo}")
                                onModificarActivo(usuario)
                            }
                        )
                        Text("Activo")
                    }
                }

                if (rolActual == "PROFESOR") {
                    IconButton(onClick = {
                        Log.d("UsuarioCard", "📅 PROFESOR navega a clasesUsuario/${usuario.id}")
                        navController.navigate("clasesUsuario/${usuario.id}")
                    }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Clases inscritas")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = estadoPagado,
                            onCheckedChange = {
                                Log.d("UsuarioCard", "💰 PROFESOR cambia PAGADO=${!estadoPagado} para ${usuario.correo}")
                                onModificarPagado(usuario)
                            }
                        )
                        Text("Pagado")
                    }
                }
            }
        }
    }
}