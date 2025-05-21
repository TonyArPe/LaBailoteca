package com.example.bailotecaapp.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.bailotecaapp.model.Usuario
import kotlinx.coroutines.delay

/**
 * Composable protector que garantiza que el usuario esté completamente cargado
 * desde Firebase y sincronizado con el backend antes de renderizar contenido sensible.
 *
 * Este guardia permite mostrar el contenido si el usuario ya está disponible,
 * o si se está en modo invitado (rol INVITADO).
 *
 * @param navController Controlador de navegación.
 * @param sesionViewModel ViewModel de sesión inyectado por Hilt.
 * @param content Contenido a mostrar si la sesión está validada.
 */
@Composable
fun SesionGuard(
    navController: NavController,
    sesionViewModel: SesionViewModel = hiltViewModel(),
    content: @Composable (Usuario) -> Unit
) {
    val usuario by sesionViewModel.usuario.collectAsState()
    val isLoading by sesionViewModel.isLoading.collectAsState()
    val modoInvitado by sesionViewModel.modoInvitado.collectAsState()
    val modoInvitadoForzado by sesionViewModel.modoInvitadoForzado.collectAsState()
    val sesionCerrada by sesionViewModel.sesionCerrada.collectAsState()

    LaunchedEffect(sesionCerrada) {
        if (sesionCerrada) {
            Log.d("SesionGuard", "Sesión cerrada detectada. Redirigiendo a login...")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
            sesionViewModel.reiniciarEstadoSesion()
        }
    }

    LaunchedEffect(Unit) {
        while (!modoInvitadoForzado && usuario == null) {
            Log.d("SesionGuard", "Esperando propagación de modo invitado...")
            delay(50)
        }
        Log.d("SesionGuard", "Propagación completada. usuario=$usuario, modoInvitadoForzado=$modoInvitadoForzado")

        if (usuario == null && !modoInvitadoForzado) {
            val authUser = FirebaseAuth.getInstance().currentUser
            if (authUser != null) {
                Log.d("SesionGuard", "Usuario Firebase detectado, cargando usuario del backend...")
                sesionViewModel.obtenerUsuarioActual()
            } else {
                Log.d("SesionGuard", "No autenticado y sin invitado. Redirigiendo a login.")
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    if (usuario != null || (modoInvitadoForzado && modoInvitado)) {
        Log.d("SesionGuard", "Renderizando contenido para usuario o invitado")
        usuario?.let { content(it) }
        return
    }

    if (isLoading) {
        Log.d("SesionGuard", "Mostrando spinner de carga...")
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}