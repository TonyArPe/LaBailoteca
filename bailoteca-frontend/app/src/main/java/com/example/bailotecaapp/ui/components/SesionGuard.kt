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
import android.util.Log

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
 * También controla si se ha forzado entrar como invitado y evita hacer llamadas innecesarias
 * tras un cierre de sesión o si ya estamos en modo invitado voluntariamente.
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
    val modoInvitadoForzado by sesionViewModel.modoInvitadoForzado.collectAsState()

    LaunchedEffect(usuario, modoInvitadoForzado) {
        val authUser = FirebaseAuth.getInstance().currentUser
        if (usuario == null && !modoInvitadoForzado) {
            if (authUser != null) {
                Log.d("SesionGuard", "Usuario autenticado en Firebase, obteniendo del backend...")
                sesionViewModel.obtenerUsuarioActual()
            } else {
                Log.d("SesionGuard", "Sin usuario Firebase ni invitado forzado -> no se hace nada")
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