package com.example.bailotecaapp.ui.components

import android.R.attr.content
import android.R.id.content
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
import kotlinx.serialization.json.JsonNull.content

/**
 * Composable protector que garantiza que el usuario autenticado esté completamente cargado
 * desde Firebase y sincronizado con el backend antes de renderizar contenido sensible.
 *
 * @param navController Controlador de navegación.
 * @param sesionViewModel ViewModel de sesión inyectado por Hilt.
 * @param content Contenido que se renderiza una vez que el usuario ha sido cargado con éxito.
 */
@Composable
fun SesionGuard(
    navController: NavController,
    sesionViewModel: SesionViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val usuario by sesionViewModel.usuario.collectAsState()
    val isLoading by sesionViewModel.isLoading.collectAsState()
    val modoInvitadoForzado by sesionViewModel.modoInvitadoForzado.collectAsState()
    val sesionCerrada by sesionViewModel.sesionCerrada.collectAsState()
    val invitado by sesionViewModel.modoInvitado.collectAsState()

    LaunchedEffect(sesionCerrada) {
        if (sesionCerrada) {
            Log.d("SesionGuard", "Sesión cerrada detectada. Redirigiendo a login...")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
            sesionViewModel.reiniciarEstadoSesion()
        }
    }

    LaunchedEffect(usuario, modoInvitadoForzado) {
        Log.d("SesionGuard", "Evaluando SesionGuard: usuario=$usuario, modoInvitado=$modoInvitadoForzado")

        if (usuario == null && modoInvitadoForzado) {
            Log.d("SesionGuard", "Esperando propagación del usuario invitado...")
            return@LaunchedEffect
        }

        if (usuario == null && !modoInvitadoForzado) {
            val authUser = FirebaseAuth.getInstance().currentUser
            if (authUser != null) {
                Log.d("SesionGuard", "Usuario autenticado en Firebase, obteniendo del backend...")
                sesionViewModel.obtenerUsuarioActual()
            } else {
                Log.d("SesionGuard", "No hay usuario ni sesión forzada, redirigiendo a login.")
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    // Mostrar contenido si ya estamos autenticados o en modo invitado
    if (usuario != null || invitado) {
        content()
        return
    }

    // Mostrar spinner si está cargando
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}