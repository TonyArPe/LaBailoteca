package com.example.bailotecaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.viewmodel.ClaseViewModel
import kotlinx.coroutines.delay

/**
 * Pantalla de detalle para mostrar toda la información real de una clase.
 * Se basa en los campos del modelo backend sin asumir inscritos.
 */
@Composable
fun ClaseDetailScreen(
    navController: NavHostController,
    claseId: Long,
    viewModel: ClaseViewModel = viewModel()
) {
    val clase by viewModel.claseSeleccionada.collectAsState()
    val uriHandler = LocalUriHandler.current

    // Cargar la clase solo una vez cuando entra
    LaunchedEffect(claseId) {
        // Pequeño delay opcional para UX si usas animaciones
        delay(150)
        viewModel.obtenerClasePorId(claseId)
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            clase?.let { c ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = c.nombre,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = c.descripcion,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Divider()

                    Text("📍 Ubicación: ${c.ubicacion}")
                    Text("🎯 Dificultad: ${c.dificultad.name.lowercase().replaceFirstChar { it.uppercaseChar() }}")
                    Text("👨‍🏫 Profesor: ${c.profesor.nombre}")

                    if (c.videoPresentacion.isNotBlank()) {
                        ClickableText(
                            text = AnnotatedString("🎥 Ver video de presentación"),
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.secondary),
                            onClick = { uriHandler.openUri(c.videoPresentacion) }
                        )
                    }

                    Divider()

                    Text("🕒 Horarios:", style = MaterialTheme.typography.titleSmall)

                    (c.horarioClases ?: emptyList()).forEach { horario ->
                        Text("• ${horario.diaSemana}: ${horario.horaInicio} - ${horario.horaFin}")
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Volver")
                    }
                }
            } ?: run {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}