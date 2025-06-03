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
import com.google.firebase.auth.FirebaseAuth
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

        FirebaseAuth.getInstance().addIdTokenListener(
            FirebaseAuth.IdTokenListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                user?.getIdToken(true)?.addOnSuccessListener { result ->
                    val nuevoToken = result.token
                    val expiracion = result.expirationTimestamp

                    if (nuevoToken != null && expiracion != null) {
                        Log.d("MainActivity", "🆕 Token renovado por Firebase (expira en $expiracion)")
                        SesionManagerSingleton.guardarTokenConExpiracion(
                            applicationContext,
                            nuevoToken,
                            expiracion * 1000
                        )
                    } else {
                        Log.w("MainActivity", "⚠️ Firebase renovó el token pero sin resultado válido")
                    }
                }?.addOnFailureListener { e ->
                    Log.e("MainActivity", "❌ Error al obtener token renovado: ${e.message}", e)
                }
            }
        )
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