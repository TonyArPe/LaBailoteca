package com.example.bailotecaapp.ui.screens.usuarios

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.bailotecaapp.ui.components.InscripcionCard
import com.example.bailotecaapp.ui.theme.Magenta
import com.example.bailotecaapp.viewmodel.UsuarioViewModel
import kotlinx.coroutines.launch

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

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(2.dp, Color(0xFF8CD400)), // lima
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = usuario!!.nombre ?: "Sin nombre",
                                style = MaterialTheme.typography.titleLarge,
                                color = Magenta
                            )

                            Text("Correo: ${usuario!!.correo}")
                            Text("Teléfono: ${usuario!!.telefono ?: "No especificado"}")
                            Text("Rol: ${usuario!!.rol.name}")

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (usuario!!.pagado) "Pagado: Sí ✅" else "Pagado: No ❌",
                                    color = if (usuario!!.pagado) Color(0xFF8CD400) else MaterialTheme.colorScheme.error
                                )

                                Checkbox(
                                    checked = usuario!!.pagado,
                                    onCheckedChange = {
                                        if (puedeEditarPagado) viewModel.togglePagado(usuario!!)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFF8CD400),
                                        uncheckedColor = MaterialTheme.colorScheme.error
                                    )
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (usuario!!.activo) "Activo: Sí ✅" else "Activo: No ❌",
                                    color = if (usuario!!.activo) Color(0xFF8CD400) else MaterialTheme.colorScheme.error
                                )

                                Checkbox(
                                    checked = usuario!!.activo,
                                    onCheckedChange = {
                                        if (puedeEditarActivo) viewModel.toggleActivo(usuario!!)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFF8CD400),
                                        uncheckedColor = MaterialTheme.colorScheme.error
                                    )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }