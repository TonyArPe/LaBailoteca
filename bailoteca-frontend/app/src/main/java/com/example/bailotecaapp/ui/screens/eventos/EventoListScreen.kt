package com.example.bailotecaapp.ui.screens.eventos

import android.util.Log
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.viewmodel.EventoViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel

/**
 * Pantalla que lista todos los eventos visibles para el usuario actual.
 * Los eventos dependen del rol del usuario:
 * - ADMIN ve todos
 * - PROFESOR ve los creados por él
 * - USUARIO ve los eventos públicos o de sus profesores
 * - INVITADO solo ve los públicos
 *
 * Si el usuario es ADMIN o PROFESOR, se muestra el botón para crear nuevos eventos.
 *
 * @param navController Controlador de navegación
 * @param sesionViewModel ViewModel que gestiona la sesión
 * @param eventoViewModel ViewModel que gestiona los eventos
 * @param usuario Usuario autenticado (puede ser null si es invitado)
 * @param modifier Modificador de layout (opcional)
 */
@Composable
fun EventoListScreen(
    navController: NavHostController,
    sesionViewModel: SesionViewModel = hiltViewModel(),
    eventoViewModel: EventoViewModel = hiltViewModel(),
    usuario: Usuario?,
    modifier: Modifier = Modifier
) {
    val usuario by sesionViewModel.usuario.collectAsState()
    val token by sesionViewModel.token.collectAsState()
    val eventos by eventoViewModel.eventos.collectAsState()

    val scope = rememberCoroutineScope()

    LaunchedEffect(usuario, token) {
        if (usuario == null) {
            Log.i("EventoListScreen", "Usuario no autenticado → Cargando eventos públicos")
            eventoViewModel.obtenerEventosPublicos()
        } else {
            Log.i("EventoListScreen", "Usuario autenticado: ${usuario!!.correo} → Cargando eventos privados")
            eventoViewModel.obtenerEventosPrivados(token ?: "")
        }
    }

    val puedeCrear = usuario?.rol in listOf(Rol.ADMIN, Rol.PROFESOR)

    Scaffold(
        floatingActionButton = {
            if (puedeCrear) {
                FloatingActionButton(
                    onClick = {
                        Log.d("EventoListScreen", "FAB pulsado → Navegando a crear evento")
                        eventoViewModel.limpiarEventoSeleccionado()
                        navController.navigate(Screens.CrearEvento.route)
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Crear evento")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
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
                Log.i("EventoListScreen", "No se encontraron eventos visibles para el usuario actual")
                Text(
                    text = "No hay eventos disponibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
}