package com.example.bailotecaapp.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.ui.components.UsuarioCard
import com.example.bailotecaapp.ui.theme.Magenta
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.example.bailotecaapp.viewmodel.UsuarioViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla que muestra la lista de usuarios disponibles, filtrados por el rol actual.
 */
@Composable
fun UserListScreen(
    navController: NavHostController,
    viewModel: UsuarioViewModel = hiltViewModel(),
    sesionViewModel: SesionViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val usuarios by viewModel.usuarios.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val usuario by sesionViewModel.usuario.collectAsState()
    val usuarioCargado by sesionViewModel.usuarioCargado.collectAsState()
    val versionClases by sesionViewModel.versionClases.collectAsState()

    var usuarioAEliminar by remember { mutableStateOf<Usuario?>(null) }

    /**
     * Reproduce exactamente el patrón funcional de ClaseListScreen.
     */
    LaunchedEffect(versionClases) {
        if (usuario != null && usuarioCargado) {
            Log.d("UserListScreen", "✅ Sesión cargada, rol: ${usuario!!.rol}")

            when (usuario!!.rol) {
                Rol.ADMIN -> {
                    Log.d("UserListScreen", "👑 ADMIN: obteniendo todos los usuarios")
                    viewModel.obtenerTodosLosUsuarios()
                }

                Rol.PROFESOR -> {
                    Log.d("UserListScreen", "📚 PROFESOR: obteniendo alumnos inscritos")
                    usuario!!.id?.let { viewModel.obtenerUsuariosVisiblesParaProfesor(it) }
                }

                else -> {
                    Toast.makeText(context, "No tienes permisos para esta pantalla", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            if (usuario?.rol == Rol.ADMIN) {
                FloatingActionButton(
                    onClick = { /* navegación a crear usuario si lo deseas */ },
                    containerColor = Magenta
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Crear usuario", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                error != null -> Text(
                    text = error ?: "Error desconocido",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )

                !usuarioCargado -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                else -> {
                    val usuariosVisibles = usuarios.filter { it.id != usuario?.id }

                    LazyColumn(
                        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(usuariosVisibles, key = { it.id ?: -1L }) { user ->
                            UsuarioCard(
                                usuario = user,
                                rolActual = usuario?.rol?.name ?: "",
                                navController = navController,
                                onEliminar = { usuarioAEliminar = user },
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