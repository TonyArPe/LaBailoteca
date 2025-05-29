package com.example.bailotecaapp

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.bailotecaapp.navigation.AppNavigation
import com.example.bailotecaapp.ui.theme.BailotecaTheme
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.example.bailotecaapp.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Punto de entrada principal de la aplicación.
 * Inicializa el NavController global y lo inyecta en la jerarquía de navegación.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "🚀 onCreate llamado, inicializando interfaz")

        setContent {
            val navController = rememberNavController()
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val isDark = themeViewModel.isDarkTheme.collectAsState(initial = false).value

            BailotecaTheme (darkTheme = isDark) {
                val sesionViewModel: SesionViewModel = hiltViewModel()

                // Esto ejecuta una sola vez al abrir la app
                LaunchedEffect(Unit) {
                    sesionViewModel.recuperarSesionDesdePreferencias()
                }

                // Este controlador se pasa a la navegacion
                AppNavigation(
                    navController = navController,
                    themeViewModel = themeViewModel)
            }
        }
    }
}