package com.example.bailotecaapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.ui.components.UsuarioCard
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.example.bailotecaapp.viewmodel.UsuarioViewModel
import com.example.bailotecaapp.ui.theme.Magenta
import kotlinx.coroutines.delay

/**
 * Pantalla que muestra la lista de usuarios, adaptada por rol:
 * - ADMIN: visualiza y gestiona todos los usuarios del sistema (menos a sí mismo).
 * - PROFESOR: solo ve usuarios inscritos en sus clases.
 *
 * Aplica los colores y formas definidas en el tema de la app.
 *
 * @param navController controlador de navegación.
 * @param viewModel ViewModel de usuario (inyectado por Hilt).
 */
@Composable
fun UserListScreen(
    navController: NavController,
    sesionViewModel: SesionViewModel,
    viewModel: UsuarioViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val usuarios by viewModel.usuarios.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val usuarioActual by sesionViewModel.usuario.collectAsState()
    Log.d("UserListScreen", "\uD83D\uDCE6 Estado usuarioActual = $usuarioActual")

    var usuarioAEliminar by remember { mutableStateOf<Usuario?>(null) }

    LaunchedEffect(Unit) {
        if (sesionViewModel.usuario.value == null) {
            Log.w("UserListScreen", "\u26A0\uFE0F Usuario no disponible aún. Forzando sincronización.")
            sesionViewModel.sincronizarDesdeSesionManager()
        }
    }

    /**
     * Lógica reactiva que se ejecuta una vez el usuario ha sido cargado.
     * Carga la lista de usuarios en base al rol actual.
     */
    LaunchedEffect(usuarioActual) {
        usuarioActual?.let {
            delay(200) // ⚠️ Esperamos que el token también esté listo
            Log.d("UserListScreen", "👤 Usuario actual: ${it.nombre} (${it.rol.name})")

            // 🧠 Forzamos sincronización dentro del ViewModel
            viewModel.sesionManager.guardarUsuario(it)

            when (it.rol.name) {
                "ADMIN" -> {
                    Log.d("UserListScreen", "🛡️ Cargando todos los usuarios (ADMIN)")
                    viewModel.obtenerTodosLosUsuarios()
                }
                "PROFESOR" -> {
                    Log.d("UserListScreen", "🧑‍🏫 Cargando usuarios visibles para PROFESOR")
                    viewModel.obtenerUsuariosVisiblesParaProfesor(it.id!!)
                }
            }
        }
    }

    Scaffold { innerPadding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Magenta)
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "Error desconocido",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            else -> {
                val usuariosUnicos = usuarios
                    .distinctBy { it.id }
                    .filter { it.id != usuarioActual?.id } // ✅ Excluye a sí mismo

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(usuariosUnicos) { usuario ->
                        UsuarioCard(
                            usuario = usuario,
                            rolActual = usuarioActual?.rol?.name ?: "",
                            navController = navController,
                            onEliminar = { usuarioAEliminar = usuario },
                            onModificarPagado = { viewModel.togglePagado(it) },
                            onModificarActivo = { viewModel.toggleActivo(it) }
                        )
                    }
                }

                usuarioAEliminar?.let { user ->
                    AlertDialog(
                        onDismissRequest = { usuarioAEliminar = null },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.eliminarUsuario(user.id!!)
                                usuarioAEliminar = null
                            }) {
                                Text("Confirmar", color = Magenta)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { usuarioAEliminar = null }) {
                                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        title = {
                            Text("¿Estás seguro?", color = MaterialTheme.colorScheme.onSurface)
                        },
                        text = {
                            Text("Esta acción eliminará a ${user.nombre} permanentemente.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp,
                        shape = MaterialTheme.shapes.large
                    )
                }
            }
        }
    }
}