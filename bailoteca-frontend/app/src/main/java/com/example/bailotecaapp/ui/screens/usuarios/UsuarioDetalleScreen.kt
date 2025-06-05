package com.example.bailotecaapp.ui.screens.usuarios

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bailotecaapp.viewmodel.UsuarioViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bailotecaapp.ui.components.InscripcionCard
import com.example.bailotecaapp.ui.theme.Magenta
import kotlinx.coroutines.launch

/**
 * Pantalla que muestra el detalle de un usuario inscrito, solo accesible por profesores.
 * Se muestra el nombre, correo, estado de pago y las clases en las que está inscrito.
 *
 * @param userId ID del usuario a mostrar.
 * @param navController Controlador de navegación para volver atrás.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuarioDetalleScreen(
    userId: Long,
    navController: NavController,
    viewModel: UsuarioViewModel = hiltViewModel()
) {
    val usuario by viewModel.usuarioDetalle.collectAsState()
    val usuarioSesion by viewModel.sesionManager.usuario.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val usuarioInscripciones by viewModel.inscripcionesUsuario.collectAsState()

    val inscripcionesVisibles = remember(usuario, usuarioInscripciones) {
        usuarioInscripciones.filter { it.clase.profesor.id == usuarioSesion?.id }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        viewModel.cargarUsuarioPorId(userId)
        viewModel.obtenerInscripcionesDelUsuario(userId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalle del usuario") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            viewModel.cargarUsuarioPorId(userId)
                            viewModel.obtenerInscripcionesDelUsuario(userId)
                            snackbarHostState.showSnackbar("Datos actualizados")
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                error != null -> Text(
                    text = error ?: "Error desconocido",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )

                usuario != null -> {
                    val puedeEditarActivo = usuarioSesion?.rol?.name == "ADMIN"
                    val puedeEditarPagado = usuarioSesion?.rol?.name == "PROFESOR"

                    UsuarioDetalleContent(
                        usuario = usuario!!,
                        onToggleActivo = if (puedeEditarActivo) { { viewModel.toggleActivo(usuario!!) } } else null,
                        onTogglePagado = if (puedeEditarPagado) { { viewModel.togglePagado(usuario!!) } } else null
                    )

                    Text(
                        text = "Clases inscritas",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}