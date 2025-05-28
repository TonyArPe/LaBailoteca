package com.example.bailotecaapp

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.navigation.compose.rememberNavController
import com.example.bailotecaapp.navigation.AppNavigation
import com.example.bailotecaapp.ui.theme.BailotecaTheme
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
            BailotecaTheme {
                val navController = rememberNavController()

                // Este controlador se pasa a la navegacion
                AppNavigation(navController = navController)
            }
        }
    }
}