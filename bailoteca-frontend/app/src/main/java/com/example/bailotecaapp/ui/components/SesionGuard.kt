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
     * Control de cierre de sesión manual.
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
     * Restaurar sesión desde preferencias si no se ha hecho.
     */
    LaunchedEffect(usuarioCargado) {
        if (!usuarioCargado) {
            Log.d("SesionGuard", "📦 Intentando restaurar sesión desde preferencias...")
            sesionViewModel.recuperarSesionDesdePreferencias()
        }
    }

    /**
     * Evitar redirección anticipada mientras se está cargando el usuario.
     * Solo redirige si ya se intentó cargar y aún no hay sesión activa ni modo invitado.
     */
    LaunchedEffect(usuario, modoInvitado, modoInvitadoForzado, isLoading, usuarioCargado) {
        Log.d("SesionGuard", "🧪 Validando redirección: cargado=$usuarioCargado, usuario=$usuario, invitado=$modoInvitado, forzado=$modoInvitadoForzado, loading=$isLoading")
        if (usuarioCargado && usuario == null && !modoInvitado && !modoInvitadoForzado && !isLoading) {
            Log.d("SesionGuard", "❌ Sin sesión ni invitado. Redirigiendo a login")
            if (navController.currentDestination?.route != "login") {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    /**
     * Mostrar loading mientras se prepara el estado.
     */
    if (!usuarioCargado || isLoading || (usuario == null && !modoInvitado && !modoInvitadoForzado)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    /**
     * Renderizar contenido una vez el usuario está disponible.
     */
    usuario?.let {
        Log.d("SesionGuard", "🎯 Renderizando contenido protegido para: ${it.correo}")
        content(it)
    }
}