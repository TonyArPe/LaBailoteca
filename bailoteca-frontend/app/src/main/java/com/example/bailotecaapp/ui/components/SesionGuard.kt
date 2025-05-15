package com.example.bailotecaapp.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
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

    var llamadaIniciada by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Redirige al login si se ha cerrado sesión
    LaunchedEffect(logoutEvent) {
        if (logoutEvent) {
            navController.navigate(Screens.Login.route) {
                popUpTo(0) { inclusive = true }
            }
            sesionViewModel.resetLogoutEvent()
        }
    }

    // Inicializa sesión si es necesario
    LaunchedEffect(Unit) {
        if (!llamadaIniciada && !isLoading && usuario == null && error == null) {
            llamadaIniciada = true

            val firebaseUser = Firebase.auth.currentUser
            if (firebaseUser != null) {
                val token = firebaseUser.getIdToken(true).await().token
                if (!token.isNullOrBlank()) {
                    Log.d("SesionGuard", "Token Firebase válido, obteniendo usuario...")
                    sesionViewModel.obtenerUsuarioActual()
                } else {
                    Log.d("SesionGuard", "Token vacío, entrando como invitado")
                    sesionViewModel.entrarComoInvitado()
                }
            } else {
                Log.d("SesionGuard", "No hay usuario Firebase, entrando como invitado")
                sesionViewModel.entrarComoInvitado()
            }
        }
    }

    when {
        isLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        error != null -> {
            Log.e("SesionGuard", "Error en la sesión: $error")
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error al cargar sesión: $error", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        scope.launch {
                            sesionViewModel.cerrarSesion()
                        }
                    }) {
                        Text("Cerrar sesión")
                    }
                }
            }
        }

        usuario != null -> {
            Log.d("SesionGuard", "Sesión cargada correctamente con: ${usuario!!.correo}")
            content(usuario!!)
        }

        else -> {
            Log.w(
                "SesionGuard",
                "Estado no definido: usuario == null, error == null, isLoading == false"
            )

            // Intentamos detectar si Firebase tiene sesión
            val firebaseUser = Firebase.auth.currentUser
            if (firebaseUser == null) {
                // Firebase cerrado => navegar al Login
                LaunchedEffect(Unit) {
                    navController.navigate(Screens.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                    sesionViewModel.resetLogoutEvent()
                }
            } else {
                // Si tiene usuario en Firebase pero no se ha hecho fetch
                LaunchedEffect(Unit) {
                    Log.d("SesionGuard", "Token Firebase válido, obteniendo usuario...")
                    sesionViewModel.obtenerUsuarioActual()
                }
            }
        }
    }
}