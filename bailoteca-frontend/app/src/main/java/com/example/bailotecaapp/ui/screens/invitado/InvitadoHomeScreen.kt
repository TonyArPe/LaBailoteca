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
fun InvitadoHomeScreen() {
    val claseViewModel: ClaseViewModel = hiltViewModel()
    val eventoViewModel: EventoViewModel = hiltViewModel()
    val clases by claseViewModel.clasesPublicas.collectAsState()
    val eventos by eventoViewModel.eventosPublicos.collectAsState()

    val secciones = remember {
        mutableStateListOf(
            SeccionExpandable("Clases públicas", true),
            SeccionExpandable("Eventos públicos", false),
            SeccionExpandable("Redes sociales", false)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Estás en modo invitado. Regístrate para inscribirte a clases y eventos.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        secciones.forEach { seccion ->
            ExpandableCard(seccion = seccion) {
                when (seccion.titulo) {
                    "Clases públicas" -> ClaseCardInvitadoList(clases)
                    "Eventos públicos" -> EventoCardInvitadoList(eventos)
                    "Redes sociales" -> RedesSocialesSection()
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}