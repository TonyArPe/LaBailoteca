package com.example.bailotecaapp.ui.screens.usuarios.perfil

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.bailotecaapp.utils.crearMultipartDesdeUri
import com.example.bailotecaapp.ui.theme.Magenta
import com.example.bailotecaapp.ui.theme.Lima

/**
 * Pantalla para editar el perfil del usuario autenticado.
 * Incluye selección de imagen de perfil, datos básicos y actualización en el backend.
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

    // Mostrar loader si aún no hay datos
    if (usuario == null || !usuarioCargado || isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val usuarioActual = usuario!!

    // Estados locales
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var nombre by remember { mutableStateOf(usuarioActual.nombre) }
    var telefono by remember { mutableStateOf(usuarioActual.telefono ?: "") }
    var direccion by remember { mutableStateOf(usuarioActual.direccion ?: "") }
    var fechaNacimiento by remember { mutableStateOf(usuarioActual.fechaNacimiento ?: "") }
    var genero by remember { mutableStateOf(usuarioActual.genero ?: "") }

    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    val imagenSubidaNombre = remember { mutableStateOf<String?>(usuarioActual.fotoPerfil) }

    // URL final de la imagen para mostrar (IP local 10.0.2.2)
    val imagenFinal = imagenUri?.toString()
        ?: usuarioActual.fotoPerfil?.let {
            "http://10.0.2.2:8080/api/media/files/$it?cache=${System.currentTimeMillis()}"
        }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imagenUri = uri
        uri?.let {
            val archivoPart = crearMultipartDesdeUri(context, it)
            sesionViewModel.subirImagenPerfil(archivoPart) { nombreArchivo ->
                imagenSubidaNombre.value = nombreArchivo
            }
        }
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Edita tu información", style = MaterialTheme.typography.titleMedium)

            // Imagen de perfil con forma circular y sombra
            Image(
                painter = rememberAsyncImagePainter(imagenFinal),
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            )

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Magenta),
            ) {
                Text("Seleccionar imagen", color = Color.White)
            }

            // Campos de entrada del formulario
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
                        apellido = usuarioActual.apellido ?: "",
                        correo = usuarioActual.correo,
                        contrasenna = usuarioActual.contrasenna,
                        rol = usuarioActual.rol,
                        telefono = telefono,
                        direccion = direccion,
                        fechaNacimiento = fechaNacimiento,
                        genero = genero,
                        fotoPerfil = imagenSubidaNombre.value,
                        activo = usuarioActual.activo,
                        pagado = usuarioActual.pagado
                    )

                    Log.d("EditProfileScreen", "📤 Enviando actualización: $actualizado")

                    sesionViewModel.actualizarPerfil(
                        usuarioActualizado = actualizado,
                        onSuccess = {
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
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Lima)
            ) {
                Text("Guardar cambios", style = MaterialTheme.typography.labelLarge)
            }

            // Diálogo de error
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