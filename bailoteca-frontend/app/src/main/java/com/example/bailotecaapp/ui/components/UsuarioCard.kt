package com.example.bailotecaapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bailotecaapp.model.Usuario

@Composable
fun UsuarioCard(usuario: Usuario) {
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
        }
    }
}
