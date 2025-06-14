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
import androidx.navigation.NavController
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.network.FirebaseUrlProvider

/**
 * Pantalla de carga que se muestra al iniciar la aplicación.
 * Espera hasta que se haya obtenido la URL dinámica desde Firebase Remote Config
 * y luego navega a la pantalla de login o la pantalla principal según el estado.
 */
@Composable
fun SplashScreen(
    navController: NavController,
    urlProvider: FirebaseUrlProvider
) {
    var cargado by remember { mutableStateOf(false) }

    // ✅ Lanzamos el efecto para esperar a que Remote Config se actualice
    LaunchedEffect(Unit) {
        Log.d("SplashScreen", "🟡 Ejecutando fetchAndActivate()...")
        try {
            val resultado = urlProvider.fetchAndActivateSuspend()
            Log.d("SplashScreen", "✅ Finalizado fetchAndActivate(). Navegando...")
        } catch (e: Exception) {
            Log.e("SplashScreen", "❌ Error durante fetchAndActivate()", e)
        }

        cargado = true
    }

    // Si ha cargado, navegamos a Login
    LaunchedEffect(cargado) {
        if (cargado) {
            navController.navigate(Screens.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // UI mientras se espera
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