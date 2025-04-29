package com.example.bailotecaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.viewmodel.ClaseViewModel

/**
 * Pantalla de detalle de una clase.
 *
 * Muestra los datos obtenidos del backend según el ID recibido por navegación.
 *
 * @param navController Controlador de navegación (por si se quiere volver o navegar a otra pantalla).
 * @param claseId ID de la clase a mostrar, pasado desde la lista.
 * @param viewModel ViewModel compartido para acceder al estado de la clase seleccionada.
 */
@Composable
fun ClaseDetailScreen(
    navController: NavHostController,
    claseId: Long,
    viewModel: ClaseViewModel = viewModel()
) {
    // Estado observable que contiene la clase cargada desde el backend
    val clase by viewModel.claseSeleccionada.collectAsState()

    // Cuando se abre la pantalla, cargamos los datos si no se han cargado
    LaunchedEffect(claseId) {
        viewModel.obtenerClasePorId(claseId)
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            clase?.let {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = it.nombre,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = it.descripcion,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Profesor: ${it.profesor.nombre}",
                        style = MaterialTheme.typography.labelLarge
                    )

                    Divider()

                    Text("Horarios:", style = MaterialTheme.typography.titleSmall)

                    (it.horarioClases ?: emptyList()).forEach { horario ->
                        Text("📅 ${horario.diaSemana}: ${horario.horaInicio} - ${horario.horaFin}")
                    }

                    Divider()

                    Text("Inscritos: ${it.inscritos?.size ?: 0}")
                }
            } ?: run {
                // Mientras se carga la clase (o si es null), mostramos loader
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}