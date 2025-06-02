package com.example.bailotecaapp.ui.screens.perfil

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
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.bailotecaapp.model.dto.UsuarioUpdateRequest
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Pantalla para editar datos del perfil del usuario autenticado.
 * Incluye imagen, nombre, dirección, teléfono y más.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavHostController,
    sesionViewModel: SesionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val usuario by sesionViewModel.usuario.collectAsState()
    val usuarioCargado by sesionViewModel.usuarioCargado.collectAsState()
    val isLoading by sesionViewModel.isLoading.collectAsState()

    Log.d("EditProfileScreen", "🎯 usuario=$usuario, cargado=$usuarioCargado, loading=$isLoading")

    // ⏳ Mostrar loader si aún se está cargando o no hay usuario disponible
    if (usuario == null || !usuarioCargado || isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val usuarioActual = usuario!!

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var nombre by remember { mutableStateOf(usuarioActual.nombre) }
    var telefono by remember { mutableStateOf(usuarioActual.telefono ?: "") }
    var direccion by remember { mutableStateOf(usuarioActual.direccion ?: "") }
    var fechaNacimiento by remember { mutableStateOf(usuarioActual.fechaNacimiento ?: "") }
    var genero by remember { mutableStateOf(usuarioActual.genero ?: "") }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imagenUri = uri
    }

    val scrollState = rememberScrollState()

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

            Image(
                painter = rememberAsyncImagePainter(imagenUri ?: usuarioActual.fotoPerfil),
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
                    val actualizado = UsuarioUpdateRequest(
                        nombre = nombre,
                        telefono = telefono,
                        direccion = direccion,
                        fechaNacimiento = fechaNacimiento,
                        genero = genero,
                        fotoPerfil = imagenUri?.toString() ?: usuarioActual.fotoPerfil
                    )

                    Log.d("EditProfileScreen", "📤 Enviando actualización: $actualizado")

                    sesionViewModel.actualizarPerfil(
                        usuarioActualizado = actualizado,
                        onSuccess = {
                            Log.i("EditProfileScreen", "✅ Perfil actualizado correctamente")

                            scope.launch {
                                val token = Firebase.auth.currentUser?.getIdToken(false)?.await()?.token
                                token?.let {
                                    sesionViewModel.iniciarSesionConTokenYSincronizar(it)
                                }
                                navController.popBackStack()
                            }
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