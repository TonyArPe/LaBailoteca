package com.example.bailotecaapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    val error by sesionViewModel.error.collectAsState()

    LaunchedEffect(usuario) {
        val authUser = FirebaseAuth.getInstance().currentUser
        if (usuario == null && authUser != null) {
            sesionViewModel.obtenerUsuarioActual()
        } else if (authUser == null) {
            sesionViewModel.entrarComoInvitado()
        }
    }

    when {
        isLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        error != null -> {
            // Mostramos error
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error al cargar sesión: $error", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        sesionViewModel.cerrarSesion()
                    }) {
                        Text("Cerrar sesión")
                    }
                }
            }
        }
        usuario != null -> {
            content(usuario!!)
        }
    }
}