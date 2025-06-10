package com.example.bailotecaapp.ui.screens.eventos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.ui.theme.Shapes
import com.example.bailotecaapp.viewmodel.EventoViewModel

/**
 * Pantalla que muestra la lista de eventos disponibles.
 * Muestra botón flotante de creación si el usuario es admin o profesor.
 *
 * @param navController controlador de navegación
 * @param usuario usuario autenticado
 * @param viewModel viewmodel de eventos
 * @param modifier modificador de estilo (útil para Scaffold)
 */
@Composable
fun EventoListScreen(
    navController: NavHostController,
    usuario: Usuario,
    viewModel: EventoViewModel,
    modifier: Modifier = Modifier
) {
    val showCreateButton = usuario.rol == Rol.ADMIN || usuario.rol == Rol.PROFESOR
    val eventos = viewModel.eventos.collectAsState().value

    // ✅ Esta llamada asegura que siempre recargamos eventos desde backend al entrar en pantalla
    LaunchedEffect(Unit) {
        when (usuario.rol) {
            Rol.ADMIN -> viewModel.getEventosAdmin()
            Rol.PROFESOR -> usuario.id?.let { viewModel.getEventosProfesor(it) }
            else -> usuario.id?.let { viewModel.getEventosVisibles(it) }
        }
    }


    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(eventos) { evento ->
                EventoCard(evento = evento, navController = navController, viewModel = viewModel)
            }
        }

        if (showCreateButton) {
            FloatingActionButton(
                onClick = { navController.navigate("crear_evento") },
                modifier = Modifier
                    .padding(16.dp)
                    .align(androidx.compose.ui.Alignment.BottomEnd),
                containerColor = MaterialTheme.colorScheme.primary,
                shape = Shapes.medium
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Crear evento",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}