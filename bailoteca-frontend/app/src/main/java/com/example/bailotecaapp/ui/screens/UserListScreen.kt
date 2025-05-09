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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bailotecaapp.viewmodel.UsuarioViewModel
import com.example.bailotecaapp.ui.components.UsuarioCard
import com.example.bailotecaapp.viewmodel.SesionViewModel


@Composable
fun UserListScreen(navController: NavController,
                   viewModel: UsuarioViewModel = viewModel()) {

    // Observamos el estado desde el ViewModel
    val usuarios by viewModel.usuarios.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val sesionViewModel: SesionViewModel = hiltViewModel()
    val usuario by sesionViewModel.usuario.collectAsState()


    // Lanzamos la carga solo una vez cuando se abre esta pantalla
    LaunchedEffect(Unit) {
        viewModel.obtenerUsuarios()
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
                    // Cargando
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                error != null -> {
                    // Error
                    Text(
                        text = error ?: "Error desconocido",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    // Lista de usuarios
                    LazyColumn {
                        items(usuarios) { usuario ->
                            UsuarioCard(usuario)
                        }
                    }
                }
            }
        }
    }
}