package com.example.bailotecaapp.ui.screens.eventos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.ui.theme.Shapes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventoListScreen(
    navController: NavHostController,
    usuario: Usuario,
    modifier: Modifier = Modifier
) {
    val eventosMock = remember {
        listOf(
            EventoMock("Festival de Salsa", "Gran fiesta con orquesta en vivo", "2025-06-10"),
            EventoMock("Taller de Bachata", "Clase intensiva con invitados", "2025-06-18")
        )
    }

    val scope = rememberCoroutineScope()
    val showCreateButton = usuario.rol == Rol.ADMIN || usuario.rol == Rol.PROFESOR

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(eventosMock) { evento ->
                EventoCard(evento)
            }
        }

        if (showCreateButton) {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        // TODO: navegar a CrearEventoScreen cuando esté implementado
                        // navController.navigate(Screens.CrearEvento.route)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                shape = Shapes.medium
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear evento", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun EventoCard(evento: EventoMock) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = Shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = evento.titulo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = evento.descripcion,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "📅 ${evento.fecha}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

data class EventoMock(
    val titulo: String,
    val descripcion: String,
    val fecha: String
)