package com.example.bailotecaapp.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.bailotecaapp.viewmodel.SesionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavHostController,
    sesionViewModel: SesionViewModel = viewModel()
) {
    val context = LocalContext.current
    val usuario by sesionViewModel.usuario.collectAsState()

    // Estados locales editables
    var nombre by remember { mutableStateOf(usuario?.nombre ?: "") }
    var telefono by remember { mutableStateOf(usuario?.telefono ?: "") }
    var direccion by remember { mutableStateOf(usuario?.direccion ?: "") }
    var fechaNacimiento by remember { mutableStateOf(usuario?.fechaNacimiento ?: "") }
    var genero by remember { mutableStateOf(usuario?.genero ?: "") }

    var imagenUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imagenUri = uri
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Editar perfil") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Edita tu información", style = MaterialTheme.typography.titleMedium)

            // Imagen actual / seleccionada
            imagenUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Seleccionar imagen")
            }

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") }
            )

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección") }
            )

            OutlinedTextField(
                value = fechaNacimiento,
                onValueChange = { fechaNacimiento = it },
                label = { Text("Fecha de nacimiento (AAAA-MM-DD)") }
            )

            OutlinedTextField(
                value = genero,
                onValueChange = { genero = it },
                label = { Text("Género") }
            )

            Button(
                onClick = {
                    Log.d("EditProfile", "Guardar cambios - aún por implementar")
                    // TODO: Enviar datos al backend y actualizar ViewModel
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar cambios")
            }
        }
    }
}
