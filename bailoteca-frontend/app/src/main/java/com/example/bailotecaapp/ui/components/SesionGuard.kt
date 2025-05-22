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
import com.example.bailotecaapp.model.enums.Rol

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
    val usuarioCargado by sesionViewModel.usuarioCargado.collectAsState()

    LaunchedEffect(sesionCerrada) {
        if (sesionCerrada) {
            Log.d("SesionGuard", "Sesión cerrada detectada. Redirigiendo a login...")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
            sesionViewModel.reiniciarEstadoSesion()
        }
    }

    Log.d("SesionGuard", "❗ usuario=$usuario, modoInvitado=$modoInvitado, modoInvitadoForzado=$modoInvitadoForzado")
    if (!usuarioCargado && !modoInvitadoForzado && usuario?.rol != Rol.INVITADO) {
        Log.d("SesionGuard", "Esperando a que el usuario se cargue completamente...")
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LaunchedEffect(Unit) {
        if (!modoInvitadoForzado && usuario == null) {
            Log.d("SesionGuard", "Usuario no autenticado y sin modo invitado. Intentando cargar usuario...")
            val authUser = FirebaseAuth.getInstance().currentUser
            if (authUser != null) {
                Log.d("SesionGuard", "Usuario Firebase detectado, obteniendo datos del backend...")
                sesionViewModel.obtenerUsuarioActual()
            } else {
                Log.d("SesionGuard", "No hay sesión activa en Firebase. Redirigiendo a login.")
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    Log.d("SesionGuard", "modoInvitado=$modoInvitado, forzado=$modoInvitadoForzado, usuario=${usuario?.correo}")
    if (usuario != null || (modoInvitado || modoInvitadoForzado)) {
        Log.d("SesionGuard", "🟢 Renderizando contenido para usuario o invitado")
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