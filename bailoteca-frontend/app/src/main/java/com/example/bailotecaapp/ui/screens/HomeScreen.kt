package com.example.bailotecaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import com.example.bailotecaapp.viewmodel.ThemeViewModel
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.viewmodel.SesionViewModel

/**
 * Pantalla principal de bienvenida después del login.
 * Muestra acciones según el rol y da la bienvenida personalizada al usuario.
 */
@Composable
fun HomeScreen(
    navController: NavHostController,
    sesionViewModel: SesionViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val usuarioState by sesionViewModel.usuario.collectAsState()
    val isLoading by sesionViewModel.isLoading.collectAsState()
    val error by sesionViewModel.error.collectAsState()

    // Cargar usuario al iniciar si no se ha hecho aún
    LaunchedEffect(Unit) {
        if (usuarioState == null && !sesionViewModel.modoInvitadoForzado.value) {
            sesionViewModel.recuperarSesionDesdePreferencias()
        }
    }

    // Mostrar error
    if (error != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: $error")
        }
        return
    }

    // Mostrar loading
    if (isLoading || usuarioState == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = { navController.navigate(Screens.Clases.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver clases disponibles")
        }
    }
}