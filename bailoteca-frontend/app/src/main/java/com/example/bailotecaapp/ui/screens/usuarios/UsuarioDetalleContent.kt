package com.example.bailotecaapp.ui.screens.usuarios

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bailotecaapp.model.Usuario

@Composable
internal fun UsuarioDetalleContent(
    usuario: Usuario,
    onToggleActivo: (() -> Unit)? = null,
    onTogglePagado: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "${usuario.nombre} ${usuario.apellido}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = "Correo: ${usuario.correo}")
            Text(text = "Teléfono: ${usuario.telefono ?: "No disponible"}")

            EstadoRow(
                label = "Pagado",
                estado = usuario.pagado,
                onToggle = onTogglePagado,
                enabled = onTogglePagado != null
            )

            EstadoRow(
                label = "Activo",
                estado = usuario.activo,
                onToggle = onToggleActivo,
                enabled = onToggleActivo != null
            )

            Text(text = "Rol: ${usuario.rol.name}")
        }
    }
}

@Composable
private fun EstadoRow(
    label: String,
    estado: Boolean,
    onToggle: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "$label: ", style = MaterialTheme.typography.bodyLarge)
        if (onToggle != null) {
            Switch(
                checked = estado,
                onCheckedChange = { onToggle() },
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.error
                )
            )
        } else {
            Text(
                text = if (estado) "Sí ✅" else "No ❌",
                color = if (estado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}