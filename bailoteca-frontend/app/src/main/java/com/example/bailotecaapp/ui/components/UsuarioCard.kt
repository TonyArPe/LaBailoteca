package com.example.bailotecaapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.navigation.Screens

/**
 * Tarjeta visual que representa un usuario.
 * Contiene botones para editar, eliminar y modificar estados según el rol actual.
 * - ADMIN puede modificar el estado 'activo'.
 * - PROFESOR puede navegar a las clases del usuario.
 *
 * @param navController Controlador de navegación.
 * @param usuario Usuario que se muestra.
 * @param rolActual Rol del usuario autenticado.
 * @param onEditar Acción al pulsar editar.
 * @param onEliminar Acción al confirmar eliminación.
 * @param onModificarPagado Acción al modificar el campo pagado (profesor).
 * @param onModificarActivo Acción al modificar el campo activo (admin).
 */
@Composable
fun UsuarioCard(
    navController: NavController,
    usuario: Usuario,
    rolActual: String,
    onEditar: (Usuario) -> Unit,
    onEliminar: (Usuario) -> Unit,
    onModificarPagado: (Usuario) -> Unit,
    onModificarActivo: (Usuario) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${usuario.nombre} ${usuario.apellido}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = usuario.correo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Rol: ${usuario.rol}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(onClick = {
                    navController.navigate("editar_usuario/${usuario.id}")
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }

                IconButton(onClick = { onEliminar(usuario) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }

                if (rolActual == "PROFESOR") {
                    IconButton(onClick = {
                        navController.navigate("clasesUsuario/${usuario.id}")
                    }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Clases inscritas")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = usuario.pagado,
                            onCheckedChange = { onModificarPagado(usuario) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text("Pagado")
                    }
                }

                if (rolActual == "ADMIN") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = usuario.activo,
                            onCheckedChange = { onModificarActivo(usuario) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                        Text("Activo")
                    }
                }
            }
        }
    }
}