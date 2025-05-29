package com.example.bailotecaapp.ui.screens.clases

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.ui.components.SesionGuard
import com.example.bailotecaapp.viewmodel.ClaseViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel
import androidx.compose.ui.platform.LocalContext

/**
 * Pantalla exclusiva para profesores donde pueden ver el detalle de sus propias clases,
 * incluyendo la lista de alumnos inscritos y pagados. Si el profesor no es el dueño de la clase,
 * se le muestra un mensaje de acceso denegado.
 */
@Composable
fun ClaseDetailProfesorScreen(
    navController: NavHostController,
    claseId: Long,
    claseViewModel: ClaseViewModel = hiltViewModel(),
    sesionViewModel: SesionViewModel = hiltViewModel()
) {
    SesionGuard(navController = navController, sesionViewModel = sesionViewModel) { usuario ->

        val clase by claseViewModel.claseSeleccionada.collectAsState()
        val alumnos by claseViewModel.alumnosInscritos.collectAsState()
        val claseYaCargada = remember { mutableStateOf(false) }

        val esPropietario = clase?.profesor?.id == usuario.id
        val context = LocalContext.current

        LaunchedEffect(claseId) {
            if (!claseYaCargada.value) {
                claseViewModel.cargarClase(claseId)
                claseViewModel.cargarAlumnosInscritos(claseId)
                claseYaCargada.value = true
            }
        }

        Scaffold { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                if (!esPropietario) {
                    Text("⛔ No tienes acceso a esta clase", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { navController.popBackStack() }) {
                        Text("Volver")
                    }
                } else {
                    clase?.let { c ->
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("${c.nombre}", style = MaterialTheme.typography.headlineSmall)
                            Text("Descripción: ${c.descripcion}")
                            Text("Ubicación: ${c.ubicacion}")

                            Divider()

                            Text("Alumnos inscritos y pagados:", style = MaterialTheme.typography.titleMedium)
                            if (alumnos.isEmpty()) {
                                Text("Ningún alumno inscrito todavía.")
                            } else {
                                alumnos.filter { it.pagado }.forEach {
                                    Text("• ${it.nombre} ${it.apellido} - ${it.correo}")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("Volver")
                            }
                        }
                    } ?: CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
