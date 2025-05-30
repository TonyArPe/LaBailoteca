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

/**
 * Pantalla que muestra la lista de usuarios.
 * Solo accesible por usuarios ADMIN o PROFESOR.
 * ADMIN puede ver y gestionar todos los usuarios.
 * PROFESOR solo puede ver y editar el estado 'pagado' de los usuarios de sus clases.
 *
 * @param navController controlador de navegación
 * @param viewModel ViewModel de usuarios inyectado con Hilt
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

    LaunchedEffect(Unit) {
        viewModel.obtenerUsuarios()
    }

    Scaffold { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
        ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                error != null -> Text(error ?: "Error desconocido", color = MaterialTheme.colorScheme.error)
                else -> {
                    LazyColumn {
                        items(usuarios) { usuario ->
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

                    // Confirmación de eliminación
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