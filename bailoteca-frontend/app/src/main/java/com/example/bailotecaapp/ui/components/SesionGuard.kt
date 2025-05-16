package com.example.bailotecaapp.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.datastore.UsuarioPreferences
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Composable que actúa como protector de sesión.
 * Comprueba si el usuario está autenticado y cargado correctamente desde Firebase + Backend.
 * Si no hay sesión, actúa como invitado. Si hay error, permite cerrar sesión.
 * Redirige al login automáticamente si detecta evento de logout.
 *
 * @param navController El controlador de navegación para redirecciones.
 * @param sesionViewModel ViewModel de sesión inyectado por Hilt.
 * @param content Contenido protegido a renderizar si la sesión está correctamente cargada.
 */
@Composable
fun SesionGuard(
    navController: NavHostController,
    sesionViewModel: SesionViewModel = hiltViewModel(),
    content: @Composable (Usuario) -> Unit
) {
    val usuario by sesionViewModel.usuario.collectAsState()
    val isLoading by sesionViewModel.isLoading.collectAsState()
    val error by sesionViewModel.error.collectAsState()
    val logoutEvent by sesionViewModel.logoutEvent.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var usuarioYaCargado by rememberSaveable { mutableStateOf(false) }

    // Redirección tras logout
    LaunchedEffect(logoutEvent) {
        if (logoutEvent) {
            navController.navigate(Screens.Login.route) {
                popUpTo(0) { inclusive = true }
            }
            sesionViewModel.resetLogoutEvent()
        }
    }

    // Vista según estado
    when {
        isLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        error != null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error: $error", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        scope.launch { sesionViewModel.cerrarSesion() }
                    }) {
                        Text("Cerrar sesión")
                    }
                }
            }
        }

        usuario != null -> {
            content(usuario!!)
        }

        else -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}