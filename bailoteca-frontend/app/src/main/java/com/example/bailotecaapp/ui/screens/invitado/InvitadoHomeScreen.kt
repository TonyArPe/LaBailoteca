package com.example.bailotecaapp.ui.screens.invitado

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bailotecaapp.viewmodel.ClaseViewModel

@Composable
fun InvitadoHomeScreen(
    claseViewModel: ClaseViewModel = hiltViewModel(),
    eventoViewModel: EventoViewModel = hiltViewModel()
) {
    val clases by claseViewModel.clases.collectAsState()
    val eventos by eventoViewModel.eventos.collectAsState()

    // Cargar datos públicos
    LaunchedEffect(Unit) {
        claseViewModel.obtenerClasesPublicas()
        eventoViewModel.obtenerEventosPublicos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text("Eventos", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        eventos.forEach {
            EventoCardInvitado(it)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Clases", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        clases.forEach {
            ClaseCardInvitado(it)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
