package com.example.bailotecaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.viewmodel.SesionViewModel

@Composable
fun HomeScreen(
    navController: NavHostController,
    sesionViewModel: SesionViewModel = hiltViewModel()
) {
    val usuarioState by sesionViewModel.usuario.collectAsState()
    val isLoading by sesionViewModel.isLoading.collectAsState()
    val error by sesionViewModel.error.collectAsState()

    // Lógica para cargar usuario al entrar
    LaunchedEffect(Unit) {
        if (usuarioState == null && !sesionViewModel.modoInvitadoForzado.value) {
            sesionViewModel.recuperarSesionDesdePreferencias()
        }
    }

    // Mostrar errores si hay
    if (error != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Error: $error")
        }
        return
    }

    // Mostrar loading
    if (isLoading || usuarioState == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val usuario = usuarioState!!

    // Contenido principal
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "¡Bienvenido a La Bailoteca!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Hola, ${usuario.nombre}, ¡bailemos!",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (usuario.rol == Rol.ADMIN || usuario.rol == Rol.PROFESOR) {
            Button(
                onClick = { navController.navigate(Screens.Usuarios.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver usuarios registrados")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate(Screens.Clases.route) }) {
            Text("Ver clases disponibles")
        }
    }
}