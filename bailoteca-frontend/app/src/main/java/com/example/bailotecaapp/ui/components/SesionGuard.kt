package com.example.bailotecaapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.example.bailotecaapp.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import kotlinx.coroutines.delay

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
    content: @Composable (Usuario) -> Unit
) {
    val usuario by sesionViewModel.usuario.collectAsState()
    val isLoading by sesionViewModel.isLoading.collectAsState()
    val modoInvitadoForzado by sesionViewModel.modoInvitadoForzado.collectAsState()

    LaunchedEffect(true) {
        val authUser = FirebaseAuth.getInstance().currentUser
        if (usuario == null && !modoInvitadoForzado) {
            if (authUser != null) {
                Log.d("SesionGuard", "Usuario autenticado en Firebase, obteniendo del backend...")
                sesionViewModel.obtenerUsuarioActual()
            } else {
                delay(100) // Previene crash por ViewModelStore
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        usuario != null -> {
            content(usuario!!)
        }
    }
}