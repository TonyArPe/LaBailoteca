package com.example.bailotecaapp

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.bailotecaapp.navigation.AppNavigation
import com.example.bailotecaapp.network.FirebaseUrlProvider
import com.example.bailotecaapp.network.session.SesionManager
import com.example.bailotecaapp.ui.theme.BailotecaTheme
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.example.bailotecaapp.viewmodel.ThemeViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Punto de entrada principal de la aplicación Bailoteca.
 * Se encarga de inicializar Firebase, restaurar sesión, aplicar tema,
 * y esperar a la configuración remota antes de renderizar la UI.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var urlProvider: FirebaseUrlProvider
    @Inject
    lateinit var sesionManager: SesionManager


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "🚀 onCreate llamado, inicializando interfaz")

        // Restauramos sesión previamente guardada
        lifecycleScope.launch {
            sesionManager.restaurarSesionDesdePreferencias()
        }

        // Escuchamos cambios de token y lo actualizamos en memoria + almacenamiento
        FirebaseAuth.getInstance().addIdTokenListener { firebaseAuth: FirebaseAuth ->
            val user = firebaseAuth.currentUser
            user?.getIdToken(true)?.addOnSuccessListener { result ->
                val nuevoToken = result.token
                val expiracion = result.expirationTimestamp

                if (nuevoToken != null && expiracion != null) {
                    Log.d("MainActivity", "🆕 Token renovado por Firebase (expira en $expiracion)")
                    lifecycleScope.launch {
                        sesionManager.guardarToken(nuevoToken)
                        // Si quieres guardar expiración, necesitarás extensión en TokenPreferences (opcional)
                    }
                } else {
                    Log.w("MainActivity", "⚠️ Firebase renovó el token pero sin resultado válido")
                }
            }?.addOnFailureListener { e ->
                Log.e("MainActivity", "❌ Error al obtener token renovado: ${e.message}", e)
            }
        }

        setContent {
            val navController = rememberNavController()
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val isDark = themeViewModel.isDarkTheme.collectAsState(initial = false).value

            var urlLista by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            // Espera fetchAndActivate
            LaunchedEffect(Unit) {
                urlLista = urlProvider.fetchAndAwaitValidUrl()
            }

            if (urlLista) {
                BailotecaTheme(darkTheme = isDark) {
                    val sesionViewModel: SesionViewModel = hiltViewModel()

                    // SINCRONIZACIÓN
                    LaunchedEffect(Unit) {
                        sesionViewModel.recuperarSesionDesdePreferencias()
                        sesionViewModel.sincronizarDesdeSesionManager()
                    }

                    AppNavigation(
                        navController = navController,
                        themeViewModel = themeViewModel,
                        urlProvider = urlProvider
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}