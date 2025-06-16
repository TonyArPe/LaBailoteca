package com.example.bailotecaapp.ui.screens.usuarios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bailotecaapp.viewmodel.UsuarioViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bailotecaapp.ui.screens.usuarios.perfil.ClaseInscritaCard

/**
 * Pantalla que muestra todas las clases inscritas por un usuario, incluyendo horarios.
 *
 * @param userId ID del usuario.
 * @param navController Para permitir volver a la pantalla anterior.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClasesUsuarioScreen(
    userId: Long,
    navController: NavController,
    viewModel: UsuarioViewModel = hiltViewModel()
) {
    val inscripciones by viewModel.inscripcionesUsuario.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        viewModel.obtenerInscripcionesDelUsuario(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clases inscritas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                inscripciones.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Este usuario no está inscrito en ninguna clase.")
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(inscripciones.distinctBy { it.clase.id }) { inscripcion ->
                            ClaseInscritaCard(clase = inscripcion.clase)
                        }
                    }
                }
            }
        }
    }
}