package com.example.bailotecaapp.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.dto.UsuarioDTO
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.ui.components.UsuarioCard
import com.example.bailotecaapp.ui.theme.Magenta
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.example.bailotecaapp.viewmodel.UsuarioViewModel

/**
 * Pantalla que muestra la lista de usuarios visibles filtrados por rol.
 * Admins ven todos, Profesores sólo los alumnos inscritos.
 *
 * @param navController controlador de navegación
 * @param viewModel ViewModel de usuarios
 * @param sesionViewModel ViewModel de sesión para obtener usuario actual
 * @param modifier modificador opcional
 */
@Composable
fun UserListScreen(
    navController: NavHostController,
    viewModel: UsuarioViewModel = hiltViewModel(),
    sesionViewModel: SesionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val usuario by sesionViewModel.usuario.collectAsState()
    val usuarios by viewModel.usuarios.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val usuarioActual by sesionViewModel.usuario.collectAsState()
    val token by sesionViewModel.token.collectAsState()
    val usuarioCargado by sesionViewModel.usuarioCargado.collectAsState()

    var usuarioAEliminar by remember { mutableStateOf<UsuarioDTO?>(null) }

    LaunchedEffect(usuarioCargado, usuarioActual?.id) {
        if (usuarioCargado && usuarioActual != null && !token.isNullOrBlank()) {
            Log.d("UserListScreen", "🔄 Cargando usuarios para rol ${usuarioActual!!.rol}")
            viewModel.obtenerUsuariosSegunRol(
                Usuario(
                    id = usuarioActual!!.id,
                    nombre = usuarioActual!!.nombre,
                    apellido = usuarioActual!!.apellido,
                    correo = usuarioActual!!.correo,
                    contrasenna = "", // no relevante aquí
                    rol = usuarioActual!!.rol
                ),
                token!!
            )
        }
    }

    if (error != null) {
        Toast.makeText(context, error ?: "Error desconocido", Toast.LENGTH_LONG).show()
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                !usuarioCargado -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                else -> {
                    // Filtrar para no mostrar usuario actual en lista
                    val usuariosVisibles = usuarios.filter { it.id != usuarioActual?.id }

                    LazyColumn {
                        items(usuariosVisibles, key = { it.id!! }) { user ->
                            val estados = viewModel.usuarioEstados.collectAsState().value[user.id]

                            usuario?.let {
                                if (estados != null) {
                                    UsuarioCard(
                                        usuario = estados,
                                        rolActual = usuarioActual?.rol?.name ?: "",
                                        navController = navController,
                                        estadoActivo = estados?.activo ?: false,
                                        estadoPagado = estados?.pagado ?: false,
                                        onEliminar = { viewModel.eliminarUsuario(it.id!!) },
                                        onModificarPagado = { viewModel.togglePagado(it) },
                                        onModificarActivo = { viewModel.toggleActivo(it) }
                                    )
                                }
                            }
                        }
                    }

                    usuarioAEliminar?.let { user ->
                        AlertDialog(
                            onDismissRequest = { usuarioAEliminar = null },
                            confirmButton = {
                                TextButton(onClick = {
                                    Log.d("UserListScreen", "🗑️ Confirmado eliminar: ${user.correo}")
                                    user.id?.let { viewModel.eliminarUsuario(it) }
                                    usuarioAEliminar = null
                                }) {
                                    Text("Confirmar", color = Magenta)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { usuarioAEliminar = null }) {
                                    Text("Cancelar")
                                }
                            },
                            title = { Text("¿Eliminar usuario?") },
                            text = { Text("¿Seguro que deseas eliminar a ${user.nombre} ${user.apellido}?") }
                        )
                    }
                }
            }
        }
    }
}