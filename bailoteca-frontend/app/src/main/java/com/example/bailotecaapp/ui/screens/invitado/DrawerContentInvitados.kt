package com.example.bailotecaapp.ui.screens.invitado

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DrawerContentInvitado(
    onInicioClick: () -> Unit,
    onPerfilClick: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("👤 Invitado", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onInicioClick) {
            Text("Inicio")
        }
        TextButton(onClick = onPerfilClick) {
            Text("Ver Perfil")
        }
        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onCerrarSesion) {
            Text("Cerrar sesión")
        }
    }
}