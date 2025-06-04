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

/**
 * Pantalla que muestra el detalle de un usuario inscrito, solo accesible por profesores.
 * Se muestra el nombre, correo, estado de pago y otros datos útiles.
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

    LaunchedEffect(userId) {
        viewModel.cargarUsuarioPorId(userId)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
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
                }
            }
        }
    }
}