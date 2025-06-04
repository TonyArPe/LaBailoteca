package com.example.bailotecaapp.ui.screens.usuarios

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bailotecaapp.model.Usuario
import androidx.compose.foundation.layout.Row

@Composable
internal fun UsuarioDetalleContent(usuario: Usuario) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "${usuario.nombre} ${usuario.apellido}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = "Correo: ${usuario.correo}")
            Text(text = "Teléfono: ${usuario.telefono ?: "No disponible"}")
            EstadoRow(label = "Pagado", estado = usuario.pagado)
            EstadoRow(label = "Activo", estado = usuario.activo)
            Text(text = "Rol: ${usuario.rol.name}")
        }
    }
}

@Composable
private fun EstadoRow(label: String, estado: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "$label: ", style = MaterialTheme.typography.bodyLarge)
        Text(
            text = if (estado) "Sí ✅" else "No ❌",
            color = if (estado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}