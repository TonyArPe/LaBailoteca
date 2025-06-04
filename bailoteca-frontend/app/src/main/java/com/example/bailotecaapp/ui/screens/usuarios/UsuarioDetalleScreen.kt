package com.example.bailotecaapp.ui.screens.usuarios

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.bailotecaapp.ui.theme.Lima
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
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val usuarioInscripciones by viewModel.inscripcionesUsuario.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        viewModel.cargarUsuarioPorId(userId)
        viewModel.obtenerInscripcionesDelUsuario(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del usuario") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                error != null -> {
                    Text(
                        text = error ?: "Error desconocido",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }

                usuario != null -> {
                    UsuarioDetalleContent(usuario!!)

                    Text(
                        text = "Clases inscritas",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    if (usuarioInscripciones.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Este alumno no está inscrito en clases tuyas",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Magenta,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        usuarioInscripciones.forEach { inscripcion ->
                            InscripcionCard(
                                inscripcion = inscripcion,
                                esPagado = usuario!!.pagado,
                                onTogglePagado = { viewModel.togglePagado(usuario!!) },
                                onEliminarInscripcion = { viewModel.eliminarInscripcion(inscripcion.id) },
                                scope = coroutineScope
                            )
                        }
                    }
                }
            }
        }
    }
}