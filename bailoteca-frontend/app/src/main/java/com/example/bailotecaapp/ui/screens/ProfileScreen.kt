package com.example.bailotecaapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.example.bailotecaapp.navigation.Screens

/**
 * Pantalla de visualización del perfil de usuario.
 * Muestra la información personal y un botón para acceder a la edicion.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    sesionViewModel: SesionViewModel = viewModel()
) {
    val usuario by sesionViewModel.usuario.collectAsState()

    if (usuario == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mi Perfil") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Foto de perfil
            Image(
                painter = rememberAsyncImagePainter(usuario!!.fotoPerfil),
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )

            Text("Nombre: ${'$'}{usuario!!.nombre}")
            Text("Correo: ${'$'}{usuario!!.correo}")
            usuario!!.telefono?.let { Text("Teléfono: ${'$'}it") }
            usuario!!.direccion?.let { Text("Dirección: ${'$'}it") }
            usuario!!.genero?.let { Text("Género: ${'$'}it") }
            usuario!!.fechaNacimiento?.let { Text("Fecha Nacimiento: ${'$'}it") }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                navController.navigate(Screens.EditProfile.route)
            }) {
                Text("Editar Perfil")
            }
        }
    }
}