package com.example.bailotecaapp.ui.screens.invitado

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bailotecaapp.viewmodel.ClaseViewModel
import com.example.bailotecaapp.viewmodel.EventoViewModel

/**
 * Pantalla de inicio para el usuario invitado.
 * Muestra la lista de eventos públicos y clases disponibles
 * sin posibilidad de interacción (más allá de ver detalles o contactar al profesor).
 *
 * @param claseViewModel ViewModel que gestiona las clases públicas.
 * @param eventoViewModel ViewModel que gestiona los eventos públicos.
 */
@Composable
fun InvitadoHomeScreen(
    claseViewModel: ClaseViewModel = hiltViewModel(),
    eventoViewModel: EventoViewModel = hiltViewModel()
) {
    // Observa los estados públicos expuestos por los ViewModels
    val clases by claseViewModel.clases.collectAsState()
    val eventos by eventoViewModel.eventos.collectAsState()

    // Lanza la carga de datos una vez al entrar en pantalla
    LaunchedEffect(Unit) {
        claseViewModel.obtenerClasesPublicas()
        eventoViewModel.obtenerEventosPublicos()
    }

    // Contenido visual para invitados
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text("Eventos", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        eventos.forEach { evento ->
            EventoCardInvitado(evento)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Clases", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        clases.forEach { clase ->
            ClaseCardInvitado(clase)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}