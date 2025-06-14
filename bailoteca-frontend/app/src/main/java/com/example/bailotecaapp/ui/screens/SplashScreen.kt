package com.example.bailotecaapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bailotecaapp.network.FirebaseUrlProvider
import kotlinx.coroutines.delay

/**
 * Pantalla inicial que se muestra mientras se actualiza la configuración remota de Firebase.
 *
 * Esta pantalla:
 * - Ejecuta `fetchAndActivate()` de Remote Config para obtener la base URL actualizada.
 * - Muestra un spinner de carga durante el proceso.
 * - Redirige automáticamente a la pantalla "login" tras finalizar.
 *
 * @param navController controlador de navegación principal
 * @param urlProvider proveedor de la URL base de Firebase
 */
@Composable
fun SplashScreen(
    navController: NavHostController,
    urlProvider: FirebaseUrlProvider
) {
    val context = LocalContext.current
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        Log.d("SplashScreen", "🟡 Ejecutando fetchAndActivate()...")
        urlProvider.fetchAndActivate()
        Log.d("SplashScreen", "✅ Finalizado fetchAndActivate(). Navegando...")

        delay(1000) // Delay opcional para mostrar el Splash mínimo 1 segundo

        cargando = false

        navController.navigate("login") {
            popUpTo("splash") { inclusive = true }
        }
    }

    // UI del Splash
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        if (cargando) {
            CircularProgressIndicator()
        }
    }
}