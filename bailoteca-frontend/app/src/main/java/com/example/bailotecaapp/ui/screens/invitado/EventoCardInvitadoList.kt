package com.example.bailotecaapp.ui.screens.invitado.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bailotecaapp.model.Evento
import com.example.bailotecaapp.ui.screens.invitado.EventoCardInvitado

/**
 * Muestra una lista vertical de eventos públicos para modo invitado.
 *
 * @param eventos Lista de eventos públicos del servidor.
 */
@Composable
fun EventoCardInvitadoList(eventos: List<Evento>, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        eventos.forEach { evento ->
            EventoCardInvitado(evento = evento, navController = navController)
        }
    }
}
