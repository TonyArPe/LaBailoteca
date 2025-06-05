package com.example.bailotecaapp.ui.screens

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
import com.example.bailotecaapp.viewmodel.UsuarioViewModel
import com.example.bailotecaapp.ui.components.UsuarioCard
import com.example.bailotecaapp.viewmodel.SesionViewModel
import android.util.Log

/**
 * Pantalla que muestra la lista de usuarios disponibles según el rol del usuario autenticado.
 *
 * - Un ADMIN verá y podrá editar todos los usuarios.
 * - Un PROFESOR verá y podrá modificar el estado de pago de los alumnos inscritos en sus clases.
 *
 * @param navController Controlador de navegación para transiciones entre pantallas.
 * @param viewModel ViewModel de usuario inyectado con Hilt.
 */
@Composable
fun UserListScreen(
    navController: NavController,
    viewModel: UsuarioViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val usuarios by viewModel.usuarios.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val sesionViewModel: SesionViewModel = hiltViewModel()
    val usuarioActual by sesionViewModel.usuario.collectAsState()

    var usuarioAEliminar by remember { mutableStateOf<Usuario?>(null) }

    /**
     * Al iniciar la pantalla, se obtiene el listado de usuarios visibles al profesor actual.
     * En caso de ser ADMIN, el backend también puede devolver todos los usuarios.
     */
    LaunchedEffect(usuarioActual) {
        val profesorId = usuarioActual?.id ?: return@LaunchedEffect
        Log.d("UserListScreen", "🔍 Cargando usuarios visibles para profesor ID: $profesorId")
        viewModel.obtenerUsuariosVisiblesParaProfesor(profesorId)
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                isLoading -> {
                    Log.d("UserListScreen", "⏳ Cargando usuarios...")
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                error != null -> {
                    Log.e("UserListScreen", "❌ Error cargando usuarios: $error")
                    Text(error ?: "Error desconocido", color = MaterialTheme.colorScheme.error)
                }

                else -> {
                    // Elimina duplicados por ID
                    val usuariosUnicos = usuarios.distinctBy { it.id }
                    Log.d("UserListScreen", "✅ Mostrando ${usuariosUnicos.size} usuarios únicos")

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(usuariosUnicos) { usuario ->
                            UsuarioCard(
                                usuario = usuario,
                                rolActual = usuarioActual?.rol?.name ?: "",
                                navController = navController,
                                onEditar = { user ->
                                    navController.navigate("editar_usuario/${user.id}")
                                },
                                onEliminar = { user ->
                                    usuarioAEliminar = user
                                },
                                onModificarPagado = { user ->
                                    viewModel.togglePagado(user)
                                }
                            )
                        }
                    }

                    // Diálogo de confirmación de eliminación
                    usuarioAEliminar?.let { user ->
                        AlertDialog(
                            onDismissRequest = { usuarioAEliminar = null },
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.eliminarUsuario(user.id!!)
                                    usuarioAEliminar = null
                                }) {
                                    Text("Confirmar")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { usuarioAEliminar = null }) {
                                    Text("Cancelar")
                                }
                            },
                            title = { Text("¿Estás seguro?") },
                            text = { Text("Esta acción eliminará a ${user.nombre} permanentemente.") }
                        )
                    }
                }
            }
        }
    }
}