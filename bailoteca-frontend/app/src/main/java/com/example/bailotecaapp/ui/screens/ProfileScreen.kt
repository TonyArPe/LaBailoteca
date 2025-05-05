package com.example.bailotecaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.viewmodel.SesionViewModel

/**
 * Pantalla que muestra el perfil del usuario autenticado.
 *
 * @param navController Controlador de navegación.
 * @param sesionViewModel ViewModel que contiene los datos del usuario actual.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    sesionViewModel: SesionViewModel = viewModel()
) {
    val usuario by sesionViewModel.usuario.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mi perfil") })
        }
    ) { padding ->
        if (usuario == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Datos del usuario", style = MaterialTheme.typography.titleMedium)

                PerfilItem(label = "Nombre", value = usuario!!.nombre)
                PerfilItem(label = "Correo", value = usuario!!.correo)
                PerfilItem(label = "Teléfono", value = usuario!!.telefono ?: "No indicado")
                PerfilItem(label = "Dirección", value = usuario!!.direccion ?: "No indicada")
                PerfilItem(label = "Fecha de nacimiento", value = usuario!!.fechaNacimiento ?: "No indicada")
                PerfilItem(label = "Género", value = usuario!!.genero ?: "No indicado")
                PerfilItem(label = "DNI", value = usuario!!.dni ?: "No indicado")
                PerfilItem(label = "Registrado el", value = usuario!!.fechaRegistro ?: "Desconocido")
            }
        }
    }
}

/**
 * Componente reutilizable para mostrar un campo del perfil.
 */
@Composable
fun PerfilItem(label: String, value: String) {
    Column {
        Text(text = label, fontWeight = FontWeight.Bold)
        Text(text = value)
    }
}
