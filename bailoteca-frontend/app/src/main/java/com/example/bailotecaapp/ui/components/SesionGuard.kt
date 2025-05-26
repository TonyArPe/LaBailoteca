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

    Log.d("SesionGuard", "\uD83E\uDDE0 usuario=${usuario?.correo}, modoInvitado=$modoInvitado, forzado=$modoInvitadoForzado, isLoading=$isLoading")

    /**
     * Efecto lanzado cuando la sesión se cierra manualmente desde el ViewModel.
     * Redirige al login y reinicia el estado interno.
     */
    LaunchedEffect(sesionCerrada) {
        if (sesionCerrada) {
            Log.d("SesionGuard", "\uD83D\uDD12 Sesión cerrada, redirigiendo al login...")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
            sesionViewModel.reiniciarEstadoSesion()
        }
    }

    /**
     * Protección adicional en caso de que todos los datos estén nulos.
     * Fuerza redirección a login de forma segura.
     */
    LaunchedEffect(usuario, modoInvitado, modoInvitadoForzado) {
        if (usuario == null && !modoInvitado && !modoInvitadoForzado && !isLoading) {
            Log.d("SesionGuard", "\u274C Sin sesión ni invitado. Intentando navegar a login...")
            try {
                if (navController.graph.startDestinationRoute != null) {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                } else {
                    Log.e("SesionGuard", "\u26A0\uFE0F navController aún no tiene grafo.")
                }
            } catch (e: Exception) {
                Log.e("SesionGuard", "\uD83D\uDEA8 Error en navegación: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Si el usuario no está seteado y no estamos en modo invitado, intentamos recuperar
     * el usuario desde Firebase automáticamente (solo una vez).
     */
    LaunchedEffect(Unit) {
        if (!modoInvitadoForzado && usuario == null) {
            Log.d("SesionGuard", "\uD83D\uDD04 Intentando recuperar usuario desde Firebase...")
            val authUser = FirebaseAuth.getInstance().currentUser
            if (authUser != null) {
                sesionViewModel.obtenerUsuarioActual()
            }
        }
    }

    /**
     * Indicador de carga si aún no está seteado el usuario ni el modo invitado.
     */
    if ((usuario == null && !modoInvitado && !modoInvitadoForzado) || isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    /**
     * Si el usuario está disponible o el modo invitado está activo, renderizamos el contenido.
     */
    usuario?.let { content(it) }
}