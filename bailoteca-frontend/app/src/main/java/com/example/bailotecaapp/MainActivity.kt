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
import androidx.navigation.compose.rememberNavController
import com.example.bailotecaapp.navigation.AppNavigation
import com.example.bailotecaapp.network.session.SesionManagerSingleton
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
        SesionManagerSingleton.restaurarSesionDesdePreferencias(applicationContext)

        setContent {
            val navController = rememberNavController()
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val isDark = themeViewModel.isDarkTheme.collectAsState(initial = false).value

            BailotecaTheme(darkTheme = isDark) {
                val sesionViewModel: SesionViewModel = hiltViewModel()
                LaunchedEffect(Unit) {
                    sesionViewModel.recuperarSesionDesdePreferencias()
                }

                AppNavigation(
                    navController = navController,
                    themeViewModel = themeViewModel
                )
            }
        }
    }
}