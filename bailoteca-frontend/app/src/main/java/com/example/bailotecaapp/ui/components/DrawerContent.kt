package com.example.bailotecaapp.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp

@Composable
fun DrawerContent(
    onItemSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Aquí para añadir mas opciones
        NavigationDrawerItem(
            label = { Text("Inicio") },
            selected = false,
            onClick = { onItemSelected("home") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        NavigationDrawerItem(
            label = { Text("Clases") },
            selected = false,
            onClick = { onItemSelected("clases") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        NavigationDrawerItem(
            label = { Text("Perfil") },
            selected = false,
            onClick = { onItemSelected("perfil") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        NavigationDrawerItem(
            label = { Text("Cerrar sesión") },
            selected = false,
            onClick = { onItemSelected("logout") }
        )
    }
}
