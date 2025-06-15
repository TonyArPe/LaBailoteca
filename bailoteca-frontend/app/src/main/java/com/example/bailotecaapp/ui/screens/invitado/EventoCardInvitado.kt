package com.example.bailotecaapp.ui.screens.invitado

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bailotecaapp.model.Evento

@Composable
fun EventoCardInvitado(evento: Evento) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(evento.nombre, style = MaterialTheme.typography.titleLarge)
            Text("Fecha: ${evento.fecha}")
            Text("Lugar: ${evento.lugar}")
        }
    }
}
