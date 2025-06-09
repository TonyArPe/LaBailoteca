package com.example.bailotecaapp.ui.screens.eventos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.viewmodel.EventoViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.example.bailotecaapp.ui.screens.eventos.EventoCard
import kotlinx.coroutines.launch

/**
 * Pantalla que lista todos los eventos visibles para el usuario actual.
 *
 * @param navController Controlador de navegación
 * @param sesionViewModel ViewModel que gestiona la sesión
 * @param eventoViewModel ViewModel que gestiona los eventos
 */
@Composable
fun EventoListScreen(
    navController: NavHostController,
    sesionViewModel: SesionViewModel = hiltViewModel(),
    eventoViewModel: EventoViewModel = hiltViewModel()
) {
    val usuario by sesionViewModel.usuario.collectAsState()
    val token by sesionViewModel.token.collectAsState()
    val eventos by eventoViewModel.eventos.collectAsState()

    val scope = rememberCoroutineScope()

    LaunchedEffect(usuario, token) {
        if (usuario == null) {
            eventoViewModel.obtenerEventosPublicos()
        } else {
            eventoViewModel.obtenerEventosPrivados(token ?: "")
        }
    }

    val puedeCrear = usuario?.rol in listOf(Rol.ADMIN, Rol.PROFESOR)

    Scaffold(
        floatingActionButton = {
            if (puedeCrear) {
                FloatingActionButton(
                    onClick = {
                        eventoViewModel.limpiarEventoSeleccionado()
                        navController.navigate(Screens.CrearEvento.route)
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Crear evento")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Eventos disponibles",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(eventos) { evento ->
                    EventoCard(
                        evento = evento,
                        usuario = usuario,
                        token = token,
                        eventoViewModel = eventoViewModel
                    )
                }
            }
        }
    }
}