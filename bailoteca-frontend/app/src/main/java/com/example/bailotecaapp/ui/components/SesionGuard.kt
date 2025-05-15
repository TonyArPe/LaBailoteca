package com.example.bailotecaapp.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.firstOrNull
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bailotecaapp.datastore.TokenPreferences
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.example.bailotecaapp.model.Usuario
import kotlinx.coroutines.launch

/**
 * Composable protector que garantiza que el usuario autenticado esté completamente cargado
 * desde Firebase y sincronizado con el backend antes de renderizar contenido sensible.
 *
 * Este componente se utiliza para envolver otras composables que requieren acceso al `Usuario`
 * ya autenticado, como `DrawerContent`, `ProfileScreen`, etc.
 *
 * Internamente, comprueba si `SesionViewModel.usuario` es null y, en caso de que haya un
 * usuario autenticado en Firebase, lanza automáticamente la carga con `obtenerUsuarioActual()`.
 * Si no hay usuario en Firebase, accede en modo invitado.
 *
 * Este código está protegido contra recomposiciones infinitas gracias al uso de una variable
 * `remember` que asegura que la llamada a la API se realice una única vez.
 *
 * @param sesionViewModel ViewModel de sesión inyectado por Hilt.
 * @param content Contenido que se renderiza una vez que el usuario ha sido cargado con éxito.
 */
@Composable
fun SesionGuard(
    sesionViewModel: SesionViewModel = hiltViewModel(),
    content: @Composable (Usuario) -> Unit
) {
    val usuario by sesionViewModel.usuario.collectAsState()
    val isLoading by sesionViewModel.isLoading.collectAsState()
    val error by sesionViewModel.error.collectAsState()
    var llamadaIniciada by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()


    LaunchedEffect(Unit) {
        if (!llamadaIniciada && !isLoading && usuario == null && error == null) {
            llamadaIniciada = true
            sesionViewModel.inicializarSesion()
        }
    }

    when {
        isLoading -> {
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
                        scope.launch {
                            sesionViewModel.cerrarSesion()
                        }
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
        }
    }
}