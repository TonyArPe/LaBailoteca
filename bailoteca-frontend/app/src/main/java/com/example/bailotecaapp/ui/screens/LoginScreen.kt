package com.example.bailotecaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bailotecaapp.navigation.Screens

@Composable
fun LoginScreen(navController: NavController) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Título principal usando tipografía personalizada
            Text(
                text = "La Bailoteca",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtítulo o texto guía
            Text(
                text = "Inicia sesión para continuar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de prueba para navegar a Home
            Button(
                onClick = {
                    navController.navigate(Screens.Home.route)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entrar", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
