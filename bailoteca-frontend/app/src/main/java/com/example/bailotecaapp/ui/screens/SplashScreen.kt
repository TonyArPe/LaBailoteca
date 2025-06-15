package com.example.bailotecaapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.network.FirebaseUrlProvider
import com.example.bailotecaapp.viewmodel.SesionViewModel
import kotlinx.coroutines.delay

/**
 * Pantalla de arranque que realiza configuración inicial y decide navegación.
 *
 * - Ejecuta `fetchAndActivate()` para cargar Remote Config
 * - Restaura sesión desde preferencias
 * - Redirige a Home si hay usuario o a Login si no
 */
@Composable
fun SplashScreen(
    navController: NavController,
    urlProvider: FirebaseUrlProvider,
    sesionViewModel: SesionViewModel = hiltViewModel()
) {
    val usuarioCargado by sesionViewModel.usuarioCargado.collectAsState()
    var fetchCompletado by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            Log.d("SplashScreen", "🟡 Ejecutando fetchAndActivate()...")
            urlProvider.fetchAndActivateSuspend()
            Log.d("SplashScreen", "✅ Configuración remota obtenida")
        } catch (e: Exception) {
            Log.e("SplashScreen", "❌ Error durante fetchAndActivate()", e)
        }

        // Restauramos sesión desde preferencias
        sesionViewModel.recuperarSesionDesdePreferencias()
        // No forzamos sincronizar aquí porque ya lo hace recuperarSesionDesdePreferencias

        // Solo marcamos fetch como completado
        fetchCompletado = true
    }

    // Esperamos a que se complete el fetch Y la sesión esté cargada correctamente
    LaunchedEffect(fetchCompletado, usuarioCargado) {
        if (fetchCompletado && usuarioCargado) {
            Log.d("SplashScreen", "✅ Sesión detectada. Navegando a Home")
            navController.navigate(Screens.Home.route) {
                popUpTo(0) { inclusive = true }
            }
        } else if (fetchCompletado && !usuarioCargado && !sesionViewModel.sesionActiva) {
            Log.d("SplashScreen", "🔐 Sin sesión. Navegando a Login")
            navController.navigate(Screens.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // UI de carga
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Cargando configuración...", style = MaterialTheme.typography.bodyMedium)
        }
    }
}