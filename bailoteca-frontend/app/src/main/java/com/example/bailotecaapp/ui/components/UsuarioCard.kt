package com.example.bailotecaapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Money
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bailotecaapp.model.Usuario

@Composable
fun UsuarioCard(
    navController: NavController,
    usuario: Usuario,
    rolActual: String,
    onEditar: (Usuario) -> Unit,
    onEliminar: (Usuario) -> Unit,
    onModificarPagado: (Usuario) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${usuario.nombre} ${usuario.apellido}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = usuario.correo,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Rol: ${usuario.rol}",
                style = MaterialTheme.typography.labelSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { navController.navigate("editar_usuario/${usuario.id}") }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar usuario")
                }

                IconButton(onClick = {
                    // Confirmación antes de eliminar
                    onEliminar(usuario)
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }

                // Solo visible si el rol actual es PROFESOR
                if (rolActual == "PROFESOR") {
                    IconButton(onClick = { onModificarPagado(usuario) }) {
                        Icon(Icons.Default.Money, contentDescription = "Marcar como pagado/no pagado")
                    }
                }
            }
        }
    }
}
