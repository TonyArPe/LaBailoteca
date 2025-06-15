package com.example.bailotecaapp.ui.screens.eventos

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.ui.theme.Lima
import com.example.bailotecaapp.viewmodel.EventoViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel

/**
 * Pantalla principal de listado de eventos.
 * Muestra los eventos visibles al usuario según su rol (admin, profesor, alumno).
 * Incluye botón flotante para crear nuevo evento si el usuario tiene permisos.
 *
 * @param navController Controlador de navegación.
 * @param viewModel ViewModel de eventos.
 * @param sesionViewModel ViewModel de sesión.
 * @param usuario Usuario autenticado actualmente.
 * @param modifier Modificador composable.
 */
@Composable
fun EventoListScreen(
    navController: NavHostController,
    viewModel: EventoViewModel = hiltViewModel(),
    sesionViewModel: SesionViewModel = hiltViewModel(),
    usuario: Usuario,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val eventos by viewModel.eventos.collectAsState()
    val usuarioSesion by sesionViewModel.usuario.collectAsState()
    val usuarioCargado by sesionViewModel.usuarioCargado.collectAsState()

    LaunchedEffect(usuarioSesion?.id) {
        when (usuarioSesion?.rol) {
            Rol.ADMIN -> viewModel.getEventosAdmin()
            Rol.PROFESOR -> usuarioSesion?.id?.let { viewModel.getEventosProfesor(it) }
            else -> usuarioSesion?.id?.let { viewModel.getEventosVisibles(it) }
        }
    }

    val showCreateButton = usuarioSesion?.rol == Rol.ADMIN || usuarioSesion?.rol == Rol.PROFESOR

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = "📆 Eventos disponibles",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (eventos.isNotEmpty()) {
                    items(eventos) { evento ->
                        EventoCard(evento = evento, navController = navController, viewModel = viewModel)
                    }
                } else {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "😕 No hay eventos disponibles.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Vuelve más tarde o crea uno nuevo si eres profesor.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }

        if (showCreateButton) {
            FloatingActionButton(
                onClick = {
                    Log.d("EventoListScreen", "🟣 FAB pulsado")
                    if (usuarioCargado) {
                        navController.navigate("crear_evento")
                    } else {
                        Toast
                            .makeText(context, "Sesión aún no lista", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                containerColor = Lima,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear evento",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}