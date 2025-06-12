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
import com.example.bailotecaapp.ui.components.UsuarioCard
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.example.bailotecaapp.viewmodel.UsuarioViewModel

/**
 * Pantalla que muestra la lista de usuarios según el rol:
 * - ADMIN puede ver y gestionar a todos.
 * - PROFESOR solo ve y edita usuarios inscritos en sus clases.
 *
 * @param navController controlador de navegación.
 * @param viewModel ViewModel de usuario (inyectado por Hilt).
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

    LaunchedEffect(usuarioActual) {
        usuarioActual?.let {
            if (it.rol.name == "ADMIN") {
                viewModel.obtenerTodosLosUsuarios()
            } else if (it.rol.name == "PROFESOR") {
                viewModel.obtenerUsuariosVisiblesParaProfesor(it.id!!)
            }
        }
    }

    Scaffold { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = error ?: "Error desconocido", color = MaterialTheme.colorScheme.error)
            }
        } else {
            val usuariosUnicos = usuarios.distinctBy { it.id }

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
                        onEditar = { navController.navigate("editar_usuario/${usuario.id}") },
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
                        }) { Text("Confirmar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { usuarioAEliminar = null }) {
                            Text("Cancelar") }
                    },
                    title = { Text("¿Estás seguro?") },
                    text = { Text("Esta acción eliminará a ${user.nombre} permanentemente.") }
                )
            }
        }
    }
}