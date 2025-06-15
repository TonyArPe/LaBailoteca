package com.example.bailotecaapp.ui.screens.eventos

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.viewmodel.EventoViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel

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
    viewModel: EventoViewModel = hiltViewModel(),
    sesionViewModel: SesionViewModel = hiltViewModel(),
    usuario: Usuario,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val eventos = viewModel.eventos.collectAsState().value
    val usuario by sesionViewModel.usuario.collectAsState()
    val usuarioCargado by sesionViewModel.usuarioCargado.collectAsState()

    // ✅ Solo lanzamos la carga cuando el usuario esté definido
    LaunchedEffect(usuario?.id) {
        when (usuario?.rol) {
            Rol.ADMIN -> viewModel.getEventosAdmin()
            Rol.PROFESOR -> usuario!!.id?.let { viewModel.getEventosProfesor(it) }
            else -> usuario?.id?.let { viewModel.getEventosVisibles(it) }
        }
    }


    val showCreateButton = usuario?.rol == Rol.ADMIN || usuario?.rol == Rol.PROFESOR

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(eventos) { evento ->
                EventoCard(evento = evento, navController = navController, viewModel = viewModel)
            }
            if (eventos.isEmpty()) {
                item {
                    Text(
                        "No hay eventos disponibles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }



        if (showCreateButton) {
            FloatingActionButton(onClick = {
                Log.d("EventoListScreen", "🟣 FAB pulsado")
                if (usuarioCargado) {
                    Log.d("EventoListScreen", "✅ Sesión cargada, navegando...")
                    navController.navigate("crear_evento")
                } else {
                    Log.w("EventoListScreen", "⏳ Sesión no cargada aún. Espera...")
                    Toast.makeText(context, "Sesión aún no lista", Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear evento",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}