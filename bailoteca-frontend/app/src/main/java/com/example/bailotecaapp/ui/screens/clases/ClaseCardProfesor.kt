package com.example.bailotecaapp.ui.screens.clases

import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bailotecaapp.model.Clase
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.viewmodel.ClaseViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel
import kotlinx.coroutines.launch

/**
 * Tarjeta que muestra la información de una clase para PROFESOR,
 * con menú contextual para editar o eliminar si es propietario.
 */
@Composable
fun ClaseCardProfesor(
    clase: Clase,
    usuarioActual: Usuario,
    navController: NavController,
    claseViewModel: ClaseViewModel,
    sesionViewModel: SesionViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate(Screens.ClaseDetail.createRoute(clase.id)) },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    clase.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Opciones")
                }

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text("✏️ Editar") },
                        onClick = {
                            expanded = false
                            navController.navigate("crearEditarClase/${clase.id}")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("🗑️ Eliminar") },
                        onClick = {
                            expanded = false
                            scope.launch {
                                val token = sesionViewModel.token.value
                                if (token.isNullOrBlank()) {
                                    Toast.makeText(context, "Token no disponible", Toast.LENGTH_SHORT).show()
                                    Log.e("ClaseCardProfesor", "❌ Token nulo")
                                    return@launch
                                }

                                val response = claseViewModel.eliminarClase(token, clase.id)

                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Clase eliminada con éxito", Toast.LENGTH_SHORT).show()
                                    sesionViewModel.marcarClasesComoActualizadas()
                                } else {
                                    Toast.makeText(context, "Error al eliminar: ${response.code()}", Toast.LENGTH_SHORT).show()
                                    Log.e("ClaseCardProfesor", "❌ Error HTTP al eliminar: ${response.code()}")
                                }
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(clase.descripcion, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Text("📍 ${clase.ubicacion}", style = MaterialTheme.typography.labelSmall)
            Text("🔥 Dificultad: ${clase.dificultad?.name?.lowercase()?.replaceFirstChar(Char::uppercase)}", style = MaterialTheme.typography.labelSmall)
        }
    }
}