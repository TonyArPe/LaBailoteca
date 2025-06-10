package com.example.bailotecaapp.ui.screens.eventos

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.viewmodel.EventoViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel

/**
 * Pantalla que muestra los eventos disponibles para el usuario autenticado.
 * Los usuarios solo ven los eventos públicos y los privados de profesores en cuyas clases están inscritos.
 * Esta pantalla es utilizada dentro de MainScaffold con el enum MainScreen.EVENTOS.
 *
 * @param navController controlador de navegación.
 * @param modifier modificador visual para adaptarse a paddings de Scaffold.
 */
@Composable
fun EventosUsuarioScreen(
    navController: NavHostController,
    sesionViewModel: SesionViewModel = hiltViewModel(),
    eventoViewModel: EventoViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val usuario by sesionViewModel.usuario.collectAsState()
    val token by sesionViewModel.token.collectAsState()
    val eventos by eventoViewModel.eventos.collectAsState()

    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) {
            Log.d("EventosUsuarioScreen", "🎟️ Cargando eventos privados para usuario autenticado")
            eventoViewModel.obtenerEventosPrivados(token!!)
        } else {
            Log.w("EventosUsuarioScreen", "❌ Token inválido o nulo, no se pueden cargar eventos")
        }
    }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Eventos disponibles",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (eventos.isEmpty()) {
            Log.i("EventosUsuarioScreen", "📭 No hay eventos disponibles")
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay eventos disponibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn {
                items(eventos) { evento ->
                    EventoCard(
                        evento = evento,
                        usuario = usuario,
                        token = token,
                        eventoViewModel = eventoViewModel,
                        navController = navController
                    )
                }
            }
        }
    }
}