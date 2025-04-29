package com.example.bailotecaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.viewmodel.SesionViewModel

@Composable
fun HomeScreen(navController: NavHostController) {
    val sesionViewModel: SesionViewModel = viewModel()
    val usuario by sesionViewModel.usuario.collectAsState()

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
            text = "Hola, ${usuario?.nombre ?: "..."}, ¡bailemos!",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { navController.navigate(Screens.Usuarios.route) }) {
            Text("Ver usuarios registrados")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate(Screens.Clases.route) }) {
            Text("Ver clases disponibles")
        }
    }
}