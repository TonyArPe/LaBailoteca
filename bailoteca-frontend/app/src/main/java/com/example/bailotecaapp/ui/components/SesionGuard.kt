package com.example.bailotecaapp.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.viewmodel.SesionViewModel

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
    val modoInvitado by sesionViewModel.modoInvitado.collectAsState()
    val modoInvitadoForzado by sesionViewModel.modoInvitadoForzado.collectAsState()
    val isLoading by sesionViewModel.isLoading.collectAsState()
    val usuarioCargado by sesionViewModel.usuarioCargado.collectAsState()
    val sesionCerrada by sesionViewModel.sesionCerrada.collectAsState()

    Log.d("SesionGuard", "🧐 usuario=${usuario?.correo}, modoInvitado=$modoInvitado, forzado=$modoInvitadoForzado, isLoading=$isLoading, yaCargado=$usuarioCargado")

    /**
     * Redirigir a login si la sesión se ha cerrado manualmente.
     */
    LaunchedEffect(sesionCerrada) {
        if (sesionCerrada) {
            Log.d("SesionGuard", "🔒 Sesión cerrada manualmente, redirigiendo a login")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
            sesionViewModel.reiniciarEstadoSesion()
        }
    }

    /**
     * Redirigir a login si no hay usuario ni invitado
     */
    LaunchedEffect(usuario, modoInvitado, modoInvitadoForzado, isLoading) {
        if (usuario == null && !modoInvitado && !modoInvitadoForzado && !isLoading) {
            Log.d("SesionGuard", "❌ Sin sesión ni invitado. Redirigiendo a login")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    /**
     * Restaurar sesión desde preferencias si aún no se ha hecho.
     */
    LaunchedEffect(usuarioCargado) {
        if (!usuarioCargado) {
            Log.d("SesionGuard", "📦 Intentando restaurar sesión desde preferencias...")
            sesionViewModel.recuperarSesionDesdePreferencias()
        }
    }

    /**
     * Intentar sincronizar datos del backend si ya hay usuario pero falta carga
     */
    LaunchedEffect(true) {
        if (!sesionViewModel.usuarioYaCargado()) {
            Log.d("SesionGuard", "📦 Restaurando sesión desde SesionGuard (solo una vez)")
            sesionViewModel.recuperarSesionDesdePreferencias()
        } else {
            Log.d("SesionGuard", "✅ Sesión ya restaurada previamente")
        }
    }

    // Mostrar indicador de carga mientras se procesa sesión
    if ((usuario == null && !modoInvitado && !modoInvitadoForzado) || isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Renderizar contenido autorizado
    usuario?.let { content(it) }
}