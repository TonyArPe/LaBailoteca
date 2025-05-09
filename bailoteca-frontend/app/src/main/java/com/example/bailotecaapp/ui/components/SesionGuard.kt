package com.example.bailotecaapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.example.bailotecaapp.model.Usuario
import com.google.firebase.auth.FirebaseAuth

/**
 * Composable protector que garantiza que el usuario autenticado esté completamente cargado
 * desde Firebase y sincronizado con el backend antes de renderizar contenido sensible.
 *
 * Este componente se utiliza para envolver otras composables que requieren acceso al `Usuario`
 * ya autenticado, como `DrawerContent`, `ProfileScreen`, etc.
 *
 * Internamente, comprueba si `SesionViewModel.usuario` es null y, en caso de que haya un
 * usuario autenticado en Firebase, lanza automáticamente la carga con `obtenerUsuarioActual()`.
 *
 * @param sesionViewModel ViewModel de sesión inyectado por Hilt.
 * @param content Contenido que se renderiza una vez que el usuario ha sido cargado con éxito.
 *
 * @see com.example.bailotecaapp.viewmodel.SesionViewModel
 */
@Composable
fun SesionGuard(
    sesionViewModel: SesionViewModel = hiltViewModel(),
    content: @Composable (Usuario) -> Unit
) {
    val usuario by sesionViewModel.usuario.collectAsState()
    val isLoading by sesionViewModel.isLoading.collectAsState()

    LaunchedEffect(usuario) {
        val authUser = FirebaseAuth.getInstance().currentUser
        if (usuario == null) {
            if (authUser != null) {
                sesionViewModel.obtenerUsuarioActual()
            } else {
                // Si no hay usuario en Firebase, nos da el rol invitado
                sesionViewModel.entrarComoInvitado()
            }
        }
    }

    when {
        isLoading || usuario == null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        else -> {
            content(usuario!!)
        }
    }
}