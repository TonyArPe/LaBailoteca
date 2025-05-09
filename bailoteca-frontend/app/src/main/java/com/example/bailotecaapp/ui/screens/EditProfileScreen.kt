package com.example.bailotecaapp.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.bailotecaapp.model.dto.UsuarioUpdateRequest
import com.example.bailotecaapp.viewmodel.SesionViewModel

/**
 * Pantalla para que el usuario pueda editar sus datos personales.
 * Incluye la selección de imagen desde la galería y campos editables.
 *
 * @param navController Controlador de navegación para volver atrás.
 * @param sesionViewModel ViewModel que contiene al usuario autenticado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavHostController,
    sesionViewModel: SesionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
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

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Estados locales editables
    var nombre by remember { mutableStateOf(usuario!!.nombre) }
    var telefono by remember { mutableStateOf(usuario!!.telefono ?: "") }
    var direccion by remember { mutableStateOf(usuario!!.direccion ?: "") }
    var fechaNacimiento by remember { mutableStateOf(usuario!!.fechaNacimiento ?: "") }
    var genero by remember { mutableStateOf(usuario!!.genero ?: "") }
    // No se ve el boton por lo cual que se pueda scrollear
    val scrollState = rememberScrollState()

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
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Edita tu información", style = MaterialTheme.typography.titleMedium)

            // Imagen actual o nueva
            Image(
                painter = rememberAsyncImagePainter(imagenUri ?: usuario!!.fotoPerfil),
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("Seleccionar imagen")
            }

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = fechaNacimiento,
                onValueChange = { fechaNacimiento = it },
                label = { Text("Fecha de nacimiento (AAAA-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = genero,
                onValueChange = { genero = it },
                label = { Text("Género") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val usuarioActualizado = UsuarioUpdateRequest(
                        nombre = nombre,
                        telefono = telefono,
                        direccion = direccion,
                        fechaNacimiento = fechaNacimiento,
                        genero = genero,
                        fotoPerfil = imagenUri?.toString() ?: usuario!!.fotoPerfil
                    )

                    sesionViewModel.actualizarPerfil(
                        usuarioActualizado = usuarioActualizado,
                        onSuccess = {
                            navController.popBackStack()
                        },
                        onError = { errorMsg ->
                            errorMessage = errorMsg
                            showErrorDialog = true
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Guardar cambios", style = MaterialTheme.typography.labelLarge)
            }

            if (showErrorDialog) {
                AlertDialog(
                    onDismissRequest = { showErrorDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showErrorDialog = false }) {
                            Text("Aceptar")
                        }
                    },
                    title = { Text("Error al guardar perfil") },
                    text = { Text(errorMessage) }
                )
            }
        }
    }
}