package com.example.bailotecaapp.ui.screens.eventos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bailotecaapp.model.Evento
import com.example.bailotecaapp.ui.theme.Lima
import com.example.bailotecaapp.viewmodel.EventoViewModel

/**
 * Componente reutilizable que muestra los detalles básicos de un evento en una tarjeta.
 * Al hacer click, selecciona el evento y navega al detalle.
 *
 * @param evento Evento a mostrar
 * @param navController controlador de navegación
 * @param viewModel ViewModel compartido de eventos
 */
@Composable
fun EventoCard(
    evento: Evento,
    navController: NavHostController,
    viewModel: EventoViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                viewModel.seleccionarEvento(evento)
                navController.navigate("evento/${evento.id}")
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = evento.nombre,
                style = MaterialTheme.typography.titleMedium,
                color = Lima
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = evento.descripcion,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "📍 ${evento.lugar}",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = "📅 ${evento.fecha}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}