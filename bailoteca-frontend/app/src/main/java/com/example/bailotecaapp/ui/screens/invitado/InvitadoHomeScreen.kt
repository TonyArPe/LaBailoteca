package com.example.bailotecaapp.ui.screens.invitado

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bailotecaapp.ui.screens.invitado.components.ClaseCardInvitadoList
import com.example.bailotecaapp.ui.screens.invitado.components.EventoCardInvitadoList
import com.example.bailotecaapp.ui.screens.invitado.components.ExpandibleCard
import com.example.bailotecaapp.ui.screens.invitado.components.RedesSocialesSection
import com.example.bailotecaapp.ui.screens.invitado.components.SeccionExpandable
import com.example.bailotecaapp.viewmodel.ClaseViewModel
import com.example.bailotecaapp.viewmodel.EventoViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel

/**
 * Pantalla de inicio para el usuario invitado.
 * Muestra la lista de eventos públicos y clases disponibles
 * sin posibilidad de interacción (más allá de ver detalles o contactar al profesor).
 *
 * @param claseViewModel ViewModel que gestiona las clases públicas.
 * @param eventoViewModel ViewModel que gestiona los eventos públicos.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InvitadoHomeScreen(
    sesionViewModel: SesionViewModel = hiltViewModel(),
    claseViewModel: ClaseViewModel = hiltViewModel(),
    eventoViewModel: EventoViewModel = hiltViewModel()
) {
    val clases by claseViewModel.clases.collectAsState()
    val eventos by eventoViewModel.eventos.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Estás en modo invitado. Regístrate para inscribirte a clases y eventos.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
        }

        item {
            ExpandibleCard(seccion = SeccionExpandable("Clases públicas")) {
                ClaseCardInvitadoList(clases = clases)
            }
        }

        item {
            ExpandibleCard(seccion = SeccionExpandable("Eventos públicos")) {
                EventoCardInvitadoList(eventos = eventos)
            }
        }

        item {
            ExpandibleCard(seccion = SeccionExpandable("Redes sociales")) {
                RedesSocialesSection()
            }
        }
    }
}