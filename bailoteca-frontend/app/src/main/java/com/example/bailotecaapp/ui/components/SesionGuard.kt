package com.example.bailotecaapp.ui.components

import android.util.Log
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

    Log.d("SesionGuard", "usuario=$usuario, isLoading=$isLoading, error=$error")

    LaunchedEffect(usuario == null && !isLoading && error == null) {
        val authUser = FirebaseAuth.getInstance().currentUser
        Log.d("SesionGuard", "FirebaseAuth currentUser=$authUser")

        if (authUser != null) {
            Log.d("SesionGuard", "Lanzando obtenerUsuarioActual()")
            sesionViewModel.obtenerUsuarioActual()
        } else {
            Log.d("SesionGuard", "Lanzando entrarComoInvitado()")
            sesionViewModel.entrarComoInvitado()
        }
    }

    when {
        isLoading -> {
            Log.d("SesionGuard", "Cargando sesión...")
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        error != null -> {
            Log.e("SesionGuard", "Error en la sesión: $error")
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
            Log.d("SesionGuard", "Sesión cargada correctamente con: ${usuario!!.correo}")
            content(usuario!!)
        }
        else -> {
            Log.w("SesionGuard", "Estado no esperado: usuario == null, isLoading == false, error == null")

            // Refuerzo: Si currentUser sigue siendo null y aún no se ha llamado explícitamente, forzamos modo invitado
            LaunchedEffect(Unit) {
                val authUser = FirebaseAuth.getInstance().currentUser
                if (authUser == null && usuario == null) {
                    Log.d("SesionGuard", "Forzando entrarComoInvitado por fallback en else")
                    sesionViewModel.entrarComoInvitado()
                }
            }
        }
    }
}