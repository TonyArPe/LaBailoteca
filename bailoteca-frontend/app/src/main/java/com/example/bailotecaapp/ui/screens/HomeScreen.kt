package com.example.bailotecaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.viewmodel.SesionViewModel

/**
 * Pantalla de bienvenida principal que se muestra al usuario una vez ha iniciado sesión
 * o accede como invitado. Presenta acciones contextuales según el rol del usuario.
 *
 * Aplica los estilos visuales definidos en el tema global (colores, tipografía y formas).
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

    // Recuperar usuario si es necesario
    LaunchedEffect(Unit) {
        if (usuarioState == null && !sesionViewModel.modoInvitadoForzado.value) {
            sesionViewModel.recuperarSesionDesdePreferencias()
        }
    }

    if (error != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Error: $error",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    if (isLoading || usuarioState == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val usuario = usuarioState!!

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Encabezado visual
        Text(
            text = "¡Bienvenido a La Bailoteca!",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Hola, ${usuario.nombre}, ¡bailemos!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Botón para admins y profesores
        if (usuario.rol == Rol.ADMIN || usuario.rol == Rol.PROFESOR) {
            Button(
                onClick = { navController.navigate(Screens.Usuarios.route) },
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Ver usuarios registrados", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Botón común
        Button(
            onClick = { navController.navigate(Screens.Clases.route) },
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Ver clases disponibles", style = MaterialTheme.typography.bodyMedium)
        }
    }
}