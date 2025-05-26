package com.example.bailotecaapp.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.viewmodel.SesionViewModel

/**
 * Composable principal que actúa como punto de entrada visual para la app.
 *
 * Este componente encapsula el `Scaffold` con `TopAppBar`, `Drawer` y navegación
 * utilizando la clase segura [SecureScaffold], que se adapta dinámicamente
 * al estado de sesión y rol del usuario (ADMIN, PROFESOR, USUARIO, INVITADO).
 *
 * Esta clase centraliza el comportamiento del UI de alto nivel,
 * reduciendo la duplicación de lógica en múltiples pantallas.
 *
 * @param navController Controlador de navegación principal.
 *
 * @see SecureScaffold para la estructura visual y lógica del scaffold.
 */
@Composable
fun MainScaffold(
    navController: NavHostController
) {
    // Inyección del ViewModel de sesión
    val sesionViewModel: SesionViewModel = hiltViewModel()

    // 🔍 Log para depuración del flujo de entrada al Scaffold principal
    Log.d("MainScaffold", "🧱 Entrando en MainScaffold con navController=${navController.hashCode()}")

    // Delegamos toda la estructura visual a SecureScaffold
    SecureScaffold(
        navController = navController,
        sesionViewModel = sesionViewModel
    )
}